package ru.lanwen.raml.rarc.api.ra.root;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import io.restassured.builder.RequestSpecBuilder;
import ru.lanwen.raml.rarc.api.Field;

import java.util.function.Supplier;

import static javax.lang.model.element.Modifier.PRIVATE;

/**
 * @author lanwen (Merkushev Kirill)
 */
public class ReqSpecSupplField implements Field {
    @Override
    public String name() {
        return "baseReqSpec";
    }

    @Override
    public FieldSpec fieldSpec() {
        return FieldSpec.builder(ParameterizedTypeName.get(
                ClassName.get(Supplier.class), 
                ClassName.get(RequestSpecBuilder.class)
        ), name(), PRIVATE).build();
    }
}
