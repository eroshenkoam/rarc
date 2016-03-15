package ru.lanwen.raml.rarc.util;

import java.nio.file.Path;

/**
 * Created by stassiak
 */
public class JsonCodegenConfig {
    private Path inputPath;
    private Path outputPath;
    private String packageName;
    private String jsonSchemaPath;

    public static JsonCodegenConfig jsonCodegenConfig() {
        return new JsonCodegenConfig();
    }

    public JsonCodegenConfig withInputPath(Path inputPath) {
        this.inputPath = inputPath;
        return this;
    }

    public JsonCodegenConfig withOutputPath(Path outputPath) {
        this.outputPath = outputPath;
        return this;
    }

    public JsonCodegenConfig withPackageName(String packageName) {
        this.packageName = packageName;
        return this;
    }

    public JsonCodegenConfig withJsonSchemaPath(String jsonSchemaPath) {
        this.jsonSchemaPath = jsonSchemaPath;
        return this;
    }

    public String getInputPath() {
        return inputPath.toString() + "/" + jsonSchemaPath;
    }

    public String getOutputPath() {
        return outputPath.toString();
    }

    public String getPackageName() {
        return packageName;
    }

    public String getJsonSchemaPath() {
        return jsonSchemaPath;
    }
}
