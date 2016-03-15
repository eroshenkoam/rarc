package ru.lanwen.raml.rarc.util;

import com.google.gson.Gson;
import com.jayway.restassured.response.Response;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.raml.model.Resource;
import ru.lanwen.raml.rarc.api.ApiResourceClass;

import javax.lang.model.element.Modifier;
import java.util.HashMap;

/**
 * Created by stassiak
 */
public class ResponseParserClass {
    private String packageName;
    private String className;
    private Resource resource;
    private HashMap<String, MethodSpec> parserMethods = new HashMap<>();

    public static ResponseParserClass respParserForResource(Resource resource) {
        ResponseParserClass apiClass = new ResponseParserClass();
        apiClass.resource = resource;
        apiClass.packageName = ApiResourceClass.packageName(resource) + ".responses";
        apiClass.className = ApiResourceClass.className(resource) + "ResponseParser";
        return apiClass;
    }

    public JavaFile javaFile(String basePackage) {
        TypeSpec.Builder apiClass = TypeSpec.classBuilder(className)
                //// TODO: 15.03.16  
                //.addJavadoc("$L\n", trimToEmpty(resource.getDescription()))
                .addModifiers(Modifier.PUBLIC)
                .addMethods(parserMethods.values());

        return JavaFile.builder(basePackage + "." + packageName, apiClass.build()).build();
    }

    public ResponseParserClass addParser(String respClass) {
        MethodSpec parser = MethodSpec.methodBuilder("parse" + respClass)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ClassName.bestGuess(respClass))
                .addParameter(Response.class, "response")
                .addStatement("return new $T().fromJson(response.asString(), $L.class)", Gson.class, respClass)
                .build();
        parserMethods.put(respClass, parser);
        return this;
    }

    public boolean containsParser(String respClass) {
        return parserMethods.containsKey(respClass);
    }

    public boolean isEmpty() {
        return parserMethods.isEmpty();
    }
}
