package ru.lanwen.raml.rarc.api.ra;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import org.raml.model.parameter.FormParameter;
import ru.lanwen.raml.rarc.api.AddParamMethod;
import ru.lanwen.raml.rarc.api.ApiResourceClass;

import javax.lang.model.element.Modifier;

import static org.apache.commons.lang3.StringUtils.*;
import static ru.lanwen.raml.rarc.api.ApiResourceClass.sanitizeParamName;

/**
 * @author lanwen (Merkushev Kirill)
 */
public class AddFormParamMethod implements AddParamMethod {
    private FormParameter param;
    private String name;
    private ReqSpecField req;
    private ApiResourceClass apiClass;
    private String suffix;

    public AddFormParamMethod(FormParameter param, String name, ReqSpecField req, ApiResourceClass apiClass, String suffix) {
        this.param = param;
        this.name = name;
        this.req = req;
        this.apiClass = apiClass;
        this.suffix = suffix;
    }

    public AddFormParamMethod(FormParameter param, String name, ReqSpecField req, ApiResourceClass apiClass) {
        this(param, name, req, apiClass, EMPTY);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public MethodSpec methodSpec() {
        String sanitized = sanitizeParamName(name);
        return MethodSpec.methodBuilder(sanitizeParamName("with" + capitalize(sanitized) + suffix))
                .addJavadoc("required: $L\n", param.isRequired())
                .addJavadoc("$L\n", isNotEmpty(param.getExample()) ? "example: " + param.getExample() : "")
                .addJavadoc("@param $L $L\n", sanitized, trimToEmpty(param.getDescription()))
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.bestGuess(apiClass.name()))
                .varargs(param.isRepeat())
                .addParameter(param.isRepeat() ? ArrayTypeName.of(ClassName.get(String.class)) : ClassName.get(String.class), sanitized)
                .addStatement("$L.addFormParam($S, $L)", req.name(), name, sanitized)
                .addStatement("return this", req.name())
                .build();
    }

    public FormParameter getParam() {
        return param;
    }

    public ReqSpecField getReq() {
        return req;
    }
}
