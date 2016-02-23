package ru.lanwen.raml.rarc.api.ra;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import ru.lanwen.raml.rarc.api.Method;

import javax.lang.model.element.Modifier;

/**
 * @author lanwen (Merkushev Kirill)
 */
public class Constructors {

    public static Method defaultConstructor(ReqSpecField req, RespSpecField resp) {
        return () -> MethodSpec.constructorBuilder()
                .addStatement("this.$N = new $T()", req.name(), req.fieldSpec().type)
                .addStatement("this.$N = new $T()", resp.name(), resp.fieldSpec().type)
                .addModifiers(Modifier.PUBLIC).build();
    }

    public static Method specsConstructor(ReqSpecField req, RespSpecField resp) {
        return () -> MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(req.fieldSpec().type, req.name()).build())
                .addStatement("this.$N = $N", req.name(), req.name())
                .addStatement("this.$N = new $T()", resp.name(), resp.fieldSpec().type)
                .build();
    }
}
