package ru.lanwen.raml.rarc.api.ra;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import org.raml.model.parameter.AbstractParam;
import ru.lanwen.raml.rarc.api.AddParamMethod;
import ru.lanwen.raml.rarc.api.ApiResourceClass;

import javax.lang.model.element.Modifier;

import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static ru.lanwen.raml.rarc.api.ApiResourceClass.sanitizeParamName;

/**
 * @author lanwen (Merkushev Kirill)
 */
public class AddAnyParamMethod implements AddParamMethod {
    private AbstractParam param;
    private String name;
    private ReqSpecField req;
    private ApiResourceClass apiClass;

    public AddAnyParamMethod(AbstractParam param, String name, ReqSpecField req, ApiResourceClass apiClass) {
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
        String sanitized = sanitizeParamName(name);
        return MethodSpec.methodBuilder(sanitizeParamName("with" + capitalize(sanitized)))
                .addJavadoc("required: $L\n", param.isRequired())
                .addJavadoc("$L\n", isNotEmpty(param.getExample()) ? "example: " + param.getExample() : "")
                .addJavadoc("@param $L $L\n", sanitized, trimToEmpty(param.getDescription()))
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.bestGuess(apiClass.name()))
                .addParameter(ClassName.get(Object.class), sanitized)
                .addStatement("$L.addParam($S, $L)", req.name(), name, sanitized)
                .addStatement("return this", req.name())
                .build();
    }
}
