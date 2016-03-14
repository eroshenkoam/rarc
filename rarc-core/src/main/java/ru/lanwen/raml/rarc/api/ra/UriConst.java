package ru.lanwen.raml.rarc.api.ra;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import ru.lanwen.raml.rarc.api.Field;

import javax.lang.model.element.Modifier;

/**
 * @author lanwen (Merkushev Kirill)
 */
public class UriConst implements Field {
    private String uri;

    public UriConst(String uri) {
        this.uri = uri;
    }

    @Override
    public String name() {
        return "REQ_URI";
    }

    @Override
    public FieldSpec fieldSpec() {
        return FieldSpec.builder(ClassName.get(String.class), name(), Modifier.FINAL, Modifier.STATIC, Modifier.PUBLIC)
                .initializer("$S", uri)
                .build();
    }
}
