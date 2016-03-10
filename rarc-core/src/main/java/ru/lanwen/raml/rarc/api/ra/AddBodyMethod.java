package ru.lanwen.raml.rarc.api.ra;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import org.raml.model.MimeType;
import ru.lanwen.raml.rarc.api.ApiResourceClass;
import ru.lanwen.raml.rarc.api.Method;

import javax.lang.model.element.Modifier;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Created by stassiak
 */
public class AddBodyMethod implements Method {
    private MimeType mimeType;
    private ReqSpecField req;
    private ApiResourceClass apiClass;

    public AddBodyMethod(MimeType mimeType, ReqSpecField req, ApiResourceClass apiClass) {
        this.mimeType = mimeType;
        this.req = req;
        this.apiClass = apiClass;
    }

    @Override
    public MethodSpec methodSpec() {
        return MethodSpec.methodBuilder("withBody")
                .addJavadoc("$L\n", isNotEmpty(mimeType.getExample()) ? "example: " + mimeType.getExample() : "")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ArrayTypeName.of(byte.class), "body")
                .returns(ClassName.bestGuess(apiClass.name()))
                .addStatement("$L.addHeader(\"Content-Type\", $S)", req.name(), mimeType.getType())
                .addStatement("$L.setBody(body)", req.name())
                .addStatement("return this", req.name())
                .build();
    }
}
