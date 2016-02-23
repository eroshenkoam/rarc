package ru.lanwen.raml.rarc.api.ra;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import ru.lanwen.raml.rarc.api.ApiResourceClass;
import ru.lanwen.raml.rarc.api.Method;

import javax.lang.model.element.Modifier;
import java.util.function.Consumer;

/**
 * @author lanwen (Merkushev Kirill)
 */
public class ChangeSpecsMethods {

    public static Method changeReq(ReqSpecField req, ApiResourceClass apiClass) {
        return () -> {
            ParameterSpec consumer = ParameterSpec.builder(
                    ParameterizedTypeName.get(
                            ClassName.get(Consumer.class), req.fieldSpec().type), "consumer").build();

            return MethodSpec.methodBuilder("withReq")
                    .returns(ClassName.bestGuess(apiClass.name()))
                    .addParameter(consumer)
                    .addStatement("$N.accept($N)", consumer.name, req.name())
                    .addStatement("return this")
                    .addModifiers(Modifier.PUBLIC).build();
        };
    }

    public static Method changeResp(RespSpecField resp, ApiResourceClass apiClass) {
        return () -> {
            ParameterSpec consumer = ParameterSpec.builder(
                    ParameterizedTypeName.get(
                            ClassName.get(Consumer.class), resp.fieldSpec().type), "consumer").build();

            return MethodSpec.methodBuilder("withResp")
                    .returns(ClassName.bestGuess(apiClass.name()))
                    .addParameter(consumer)
                    .addStatement("$N.accept($N)", consumer.name, resp.name())
                    .addStatement("return this")
                    .addModifiers(Modifier.PUBLIC).build();
        };
    }
}
