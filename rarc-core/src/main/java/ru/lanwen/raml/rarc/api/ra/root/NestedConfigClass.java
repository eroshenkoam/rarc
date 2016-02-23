package ru.lanwen.raml.rarc.api.ra.root;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import ru.lanwen.raml.rarc.api.ra.ReqSpecField;

import java.util.function.Supplier;

import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static org.apache.commons.lang3.StringUtils.lowerCase;
import static ru.lanwen.raml.rarc.api.ApiResourceClass.sanitize;

/**
 * @author lanwen (Merkushev Kirill)
 */
public class NestedConfigClass {

    public static final String CONFIG_NESTED_STATIC_CLASS_NAME = "Config";
    private String apiTitle;
    private ReqSpecSupplField baseSupplField;
    private ReqSpecField reqField;

    public NestedConfigClass(String apiTitle, ReqSpecSupplField baseSupplField, ReqSpecField reqField) {
        this.apiTitle = apiTitle;
        this.baseSupplField = baseSupplField;
        this.reqField = reqField;
    }

    public ReqSpecSupplField getBaseSupplField() {
        return baseSupplField;
    }

    public TypeSpec nestedConfSpec() {
        return TypeSpec.classBuilder(CONFIG_NESTED_STATIC_CLASS_NAME)
                .addField(baseSupplField.fieldSpec())
                .addMethod(MethodSpec.methodBuilder("withReqSpecSupplier").addModifiers(PUBLIC)
                        .returns(ClassName.bestGuess(CONFIG_NESTED_STATIC_CLASS_NAME))
                        .addParameter(ParameterizedTypeName.get(ClassName.get(Supplier.class), reqField.fieldSpec().type), "supplier")
                        .addStatement("this.$N = $N", baseSupplField.name(), "supplier")
                        .addStatement("return this")
                        .build())
                .addMethod(MethodSpec.methodBuilder(lowerCase(sanitize(apiTitle)) + CONFIG_NESTED_STATIC_CLASS_NAME)
                        .returns(ClassName.bestGuess(CONFIG_NESTED_STATIC_CLASS_NAME))
                        .addModifiers(PUBLIC, STATIC)
                        .addStatement("return new $N()", CONFIG_NESTED_STATIC_CLASS_NAME)
                        .build())
                .addModifiers(PUBLIC, STATIC)
                .build();
    }
}
