package ru.lanwen.raml.rarc.util;

import com.jayway.restassured.path.json.config.JsonParserType;
import com.jayway.restassured.path.json.config.JsonPathConfig;
import com.jayway.restassured.response.Response;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.raml.model.Resource;
import ru.lanwen.raml.rarc.api.ApiResourceClass;
import ru.lanwen.raml.rarc.rules.BodyRule;

import javax.lang.model.element.Modifier;
import java.util.HashMap;

import static ru.lanwen.raml.rarc.rules.BodyRule.MimeTypeEnum.getRaPathClass;

/**
 * Created by stassiak
 */
public class ResponseParserClass {
    private String packageName;
    private String className;
    private HashMap<String, MethodSpec> parserMethods = new HashMap<>();

    public static ResponseParserClass respParserForResource(Resource resource) {
        ResponseParserClass apiClass = new ResponseParserClass();
        apiClass.packageName = ApiResourceClass.packageName(resource) + ".responses";
        apiClass.className = ApiResourceClass.className(resource) + "ResponseParser";
        return apiClass;
    }

    public JavaFile javaFile(String basePackage) {
        TypeSpec.Builder apiClass = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addMethods(parserMethods.values());

        return JavaFile.builder(basePackage + "." + packageName, apiClass.build()).build();
    }

    public ResponseParserClass addParser(String respClass, BodyRule.MimeTypeEnum mimeType) {
        MethodSpec.Builder methodBilder = MethodSpec.methodBuilder("parse" + respClass)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ClassName.bestGuess(respClass))
                .addParameter(Response.class, "response");
        Class raPathClass = getRaPathClass(mimeType);
        if( mimeType.equals(BodyRule.MimeTypeEnum.JSON)) {
            methodBilder.addStatement("$T.config = new $T().defaultParserType($T.GSON)",
                    raPathClass, JsonPathConfig.class, JsonParserType.class);
        }
        methodBilder.addStatement("return $T.from(response.asInputStream()).getObject(\".\", $L.class)",
                        raPathClass, respClass);
        parserMethods.put(respClass, methodBilder.build());
        return this;
    }

    public boolean containsParser(String respClass) {
        return parserMethods.containsKey(respClass);
    }

    public boolean isEmpty() {
        return parserMethods.isEmpty();
    }
}
