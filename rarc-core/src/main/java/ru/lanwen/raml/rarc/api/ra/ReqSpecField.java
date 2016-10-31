package ru.lanwen.raml.rarc.api.ra;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import io.restassured.builder.RequestSpecBuilder;
import ru.lanwen.raml.rarc.api.Field;

import javax.lang.model.element.Modifier;

/**
 * @author lanwen (Merkushev Kirill)
 */
public class ReqSpecField implements Field {
    @Override
    public String name() {
        return "reqSpec";
    }

    @Override
    public FieldSpec fieldSpec() {
        return FieldSpec.builder(ClassName.get(RequestSpecBuilder.class), name(), Modifier.PRIVATE).build();
    }
}
