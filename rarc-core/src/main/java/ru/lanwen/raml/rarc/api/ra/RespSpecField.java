package ru.lanwen.raml.rarc.api.ra;

import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import ru.lanwen.raml.rarc.api.Field;

import javax.lang.model.element.Modifier;

/**
 * @author lanwen (Merkushev Kirill)
 */
public class RespSpecField implements Field {
    @Override
    public String name() {
        return "respSpec";
    }

    @Override
    public FieldSpec fieldSpec() {
        return FieldSpec.builder(ClassName.get(ResponseSpecBuilder.class), name(), Modifier.PRIVATE).build();
    }
}
