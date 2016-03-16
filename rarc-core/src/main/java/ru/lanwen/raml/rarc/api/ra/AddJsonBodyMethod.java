package ru.lanwen.raml.rarc.api.ra;

import com.google.gson.Gson;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import org.apache.commons.io.FileUtils;
import ru.lanwen.raml.rarc.api.Method;
import ru.lanwen.raml.rarc.util.JsonCodegen;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static ru.lanwen.raml.rarc.util.JsonCodegenConfig.jsonCodegenConfig;

/**
 * Created by stassiak
 */
public class AddJsonBodyMethod implements Method {
    private String reqName;
    private String returnClassName;
    private Path inputPathForJsonGen;
    private Path outputPathForJsonGen;
    private String packageForJsonGen;
    private String jsonSchemaPath;
    private String example;

    public static AddJsonBodyMethod bodyMethod() {
        return new AddJsonBodyMethod();
    }

    public AddJsonBodyMethod withReqName(String reqName) {
        this.reqName = reqName;
        return this;
    }

    public AddJsonBodyMethod returns(String returnClassName) {
        this.returnClassName = returnClassName;
        return this;
    }

    public AddJsonBodyMethod withInputPathForJsonGen(Path inputPathForJsonGen) {
        this.inputPathForJsonGen = inputPathForJsonGen;
        return this;
    }

    public AddJsonBodyMethod withOutputPathForJsonGen(Path outputPathForJsonGen) {
        this.outputPathForJsonGen = outputPathForJsonGen;
        return this;
    }

    public AddJsonBodyMethod withPackageForJsonGen(String packageForJsonGen) {
        this.packageForJsonGen = packageForJsonGen;
        return this;
    }

    public AddJsonBodyMethod withShema(Object jsonSchema) {
        this.jsonSchemaPath = jsonSchema.toString();
        return this;
    }

    public AddJsonBodyMethod withExample(String example) {
        this.example = example;
        return this;
    }

    @Override
    public MethodSpec methodSpec() {
        String bodyClassName = null;
        try {
            bodyClassName = new JsonCodegen(
                    jsonCodegenConfig()
                            .withInputPath(inputPathForJsonGen)
                            .withOutputPath(outputPathForJsonGen)
                            .withJsonSchemaPath(jsonSchemaPath)
                            .withPackageName(packageForJsonGen)).generate();
            example = isNotEmpty(example) ?
                    "example: " + example :
                    FileUtils.readFileToString(new File(inputPathForJsonGen.toString() + "/" + jsonSchemaPath));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return MethodSpec.methodBuilder("with" + bodyClassName)
                .addJavadoc("$L\n", example)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.bestGuess(bodyClassName), "body")
                .returns(ClassName.bestGuess(returnClassName))
                .addStatement("$L.addHeader($S, $S)", reqName, "Content-Type", "application/json")
                .addStatement("$L.setBody(new $T().toJson(body))", reqName, Gson.class)
                .addStatement("return this", reqName)
                .build();
    }
}
