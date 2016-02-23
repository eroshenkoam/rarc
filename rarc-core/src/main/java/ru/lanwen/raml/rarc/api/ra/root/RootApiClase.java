package ru.lanwen.raml.rarc.api.ra.root;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.raml.model.Raml;

import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.apache.commons.lang3.StringUtils.lowerCase;
import static ru.lanwen.raml.rarc.api.ApiResourceClass.sanitize;
import static ru.lanwen.raml.rarc.api.ra.NextResourceMethods.baseResource;

/**
 * @author lanwen (Merkushev Kirill)
 */
public class RootApiClase {

    private NestedConfigClass configClass;
    private ConfigField configField = new ConfigField();

    public RootApiClase(NestedConfigClass configClass) {
        this.configClass = configClass;
    }

    public JavaFile javaFile(Raml raml, String basePackage) {

        TypeSpec.Builder api = TypeSpec.classBuilder("Api" + capitalize(sanitize(raml.getTitle())))
                .addJavadoc("$L api client\n", raml.getTitle())
                .addJavadoc("Base URI pattern: $L\n", raml.getBaseUri())
                .addField(configField.fieldSpec())
                .addType(configClass.nestedConfSpec())
                .addMethod(MethodSpec.constructorBuilder().addParameter(ClassName.bestGuess(NestedConfigClass.CONFIG_NESTED_STATIC_CLASS_NAME), configField.name())
                        .addStatement("this.$N = $N", configField.name(), configField.name())
                        .addModifiers(PRIVATE)
                        .build())
                .addModifiers(PUBLIC);

        api.addMethod(MethodSpec.methodBuilder(lowerCase(sanitize(raml.getTitle())))
                .addParameter(ClassName.bestGuess(NestedConfigClass.CONFIG_NESTED_STATIC_CLASS_NAME), configField.name())
                .returns(ClassName.bestGuess(api.build().name))
                .addStatement("return new $N($N)", api.build().name, configField.name())
                .addModifiers(PUBLIC, STATIC)
                .build());

        raml.getResources().values().stream().forEach(resource ->
                api.addMethod(
                        baseResource(resource, basePackage, configField.name(), configClass.getBaseSupplField().name())
                ));
        
        return JavaFile.builder(basePackage, api.build()).build();
    }
}
