package ru.lanwen.raml.rarc.api.ra;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeVariableName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.raml.model.Action;
import ru.lanwen.raml.rarc.api.Method;

import javax.lang.model.element.Modifier;
import java.util.function.Function;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

/**
 * @author lanwen (Merkushev Kirill)
 */
public class ActionMethod implements Method {

    private String reqFieldName;
    private String respFieldName;
    private String httpMethod;
    private String uriConst;
    private String name;
    
    private Action action;

    public ActionMethod(ReqSpecField reqFieldName, RespSpecField respFieldName, UriConst uriConst, Action action) {
        this.reqFieldName = reqFieldName.name();
        this.respFieldName = respFieldName.name();
        this.uriConst = uriConst.name();
        this.action = action;
        
        this.httpMethod = action.getType().name().toLowerCase();
        this.name = defaultIfEmpty(action.getDisplayName(), httpMethod);
    }
    
    @Override
    public MethodSpec methodSpec() {
        ParameterSpec handler = ParameterSpec.builder(
                ParameterizedTypeName.get(ClassName.get(Function.class), 
                        ClassName.get(Response.class), ClassName.bestGuess("T")), "handler").build();
        
        return MethodSpec.methodBuilder(isNotEmpty(name) ? name : httpMethod)
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(TypeVariableName.get("T"))
                .returns(ClassName.bestGuess("T"))
                .addJavadoc("$L\n", trimToEmpty(action.getDescription()))
                .addParameter(handler)
                .addStatement("return $L.apply($T.given().spec($L.build()).expect().spec($L.build()).when().$L($L))",
                        handler.name,
                        RestAssured.class,
                        reqFieldName,
                        respFieldName,
                        httpMethod,
                        uriConst
                ).build();
    }
}
