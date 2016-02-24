package ru.lanwen.raml.rarc.api.ra;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import org.raml.model.parameter.UriParameter;
import ru.lanwen.raml.rarc.api.AddParamMethod;
import ru.lanwen.raml.rarc.api.ApiResourceClass;
import ru.lanwen.raml.rarc.api.Method;

import javax.lang.model.element.Modifier;

import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static ru.lanwen.raml.rarc.api.ApiResourceClass.sanitize;

/**
 * @author lanwen (Merkushev Kirill)
 */
public class AddPathParamMethod implements AddParamMethod {
    private UriParameter param;
    private String name;
    private ReqSpecField req;
    private ApiResourceClass apiClass;

    public AddPathParamMethod(UriParameter param, String name, ReqSpecField req, ApiResourceClass apiClass) {
        this.param = param;
        this.name = name;
        this.req = req;
        this.apiClass = apiClass;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public MethodSpec methodSpec() {
        String sanitized = sanitize(name);
        return MethodSpec.methodBuilder("with" + capitalize(sanitized))
                .addJavadoc("required: $L\n", param.isRequired())
                .addJavadoc("$L\n", isNotEmpty(param.getExample()) ? "example: " + param.getExample() : "")
                .addJavadoc("@param $L $L\n", sanitized, trimToEmpty(param.getDescription()))
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.bestGuess(apiClass.name()))
                .addParameter(ClassName.get(String.class), sanitized)
                .addStatement("$L.addPathParam($S, $L)", req.name(), name, sanitized)
                .addStatement("return this", req.name())
                .build();
    }
}
