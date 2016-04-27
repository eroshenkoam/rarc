package ru.lanwen.raml.rarc.util;

import java.nio.file.Path;
import java.util.Properties;

/**
 * Created by stassiak
 */
public class ResponseCodegenConfig {
    protected Path inputPath;
    protected Path outputPath;
    protected String packageName;
    protected String schemaPath;

    public static ResponseCodegenConfig config() {
        return new ResponseCodegenConfig();
    }

    public ResponseCodegenConfig withInputPath(Path inputPath) {
        this.inputPath = inputPath;
        return this;
    }

    public ResponseCodegenConfig withOutputPath(Path outputPath) {
        this.outputPath = outputPath;
        return this;
    }

    public ResponseCodegenConfig withPackageName(String packageName) {
        this.packageName = packageName;
        return this;
    }

    public ResponseCodegenConfig withSchemaPath(String schemaPath) {
        this.schemaPath = schemaPath;
        return this;
    }

    public Properties asJaxb2Properties() {
        Properties properties = new Properties();
        properties.setProperty("jaxb2.output.dir",outputPath.toString());
        properties.setProperty("jaxb2.packagename", packageName);
        properties.setProperty("jaxb2.source", getInputPath());

        return properties;
    }

    public String getInputPath() {
        return inputPath.toString() + "/" + schemaPath;
    }

    public String getOutputPath() {
        return outputPath.toString();
    }

    public String getPackageName() {
        return packageName;
    }

    public String getJsonSchemaPath() {
        return schemaPath;
    }
}
