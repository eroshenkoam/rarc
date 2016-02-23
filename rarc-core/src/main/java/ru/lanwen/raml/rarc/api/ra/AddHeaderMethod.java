package ru.lanwen.raml.rarc.api.ra;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import org.raml.model.parameter.Header;
import ru.lanwen.raml.rarc.api.ApiResourceClass;
import ru.lanwen.raml.rarc.api.Method;

import javax.lang.model.element.Modifier;

import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.apache.commons.lang3.StringUtils.uncapitalize;
import static ru.lanwen.raml.rarc.api.ApiResourceClass.sanitize;

/**
 * @author lanwen (Merkushev Kirill)
 */
public class AddHeaderMethod implements Method {
    private Header header;
    private String name;
    private ReqSpecField req;
    private ApiResourceClass apiClass;

    public AddHeaderMethod(Header header, String name, ReqSpecField req, ApiResourceClass apiClass) {
        this.header = header;
        this.name = name;
        this.req = req;
        this.apiClass = apiClass;
    }

    @Override
    public MethodSpec methodSpec() {
        String replace = capitalize(sanitize(name));
        return MethodSpec.methodBuilder("with" + replace + "Header")
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc("required: $L\n", header.isRequired())
                .addJavadoc("$L\n", isNotEmpty(header.getExample()) ? header.getExample() : "")
                .addJavadoc("@param $L $L\n", uncapitalize(replace), trimToEmpty(header.getDescription()))
                .returns(ClassName.bestGuess(apiClass.name()))
                .addParameter(ClassName.get(String.class), uncapitalize(replace))
                .addStatement("$L.addHeader($S, $L)", req.name(), name, uncapitalize(replace))
                .addStatement("return this", req.name())
                .build();
    }
}
