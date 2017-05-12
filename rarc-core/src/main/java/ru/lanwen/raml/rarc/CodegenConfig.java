package ru.lanwen.raml.rarc;

import org.apache.commons.io.FilenameUtils;

import java.nio.file.Path;

import static ru.lanwen.raml.rarc.api.ApiResourceClass.packageName;

/**
 * @author lanwen (Merkushev Kirill)
 */
public class CodegenConfig {

    private String basePackage;
    private Path inputPath;
    private Path outputPath;

    private CodegenConfig() {
    }

    public static CodegenConfig codegenConf() {
        return new CodegenConfig();
    }

    public CodegenConfig withBasePackage(String basePackage) {
        this.basePackage = basePackage;
        return this;
    }

    public CodegenConfig withInputPath(Path inputPath) {
        this.inputPath = inputPath;
        return this;
    }

    public CodegenConfig withOutputPath(Path outputPath) {
        this.outputPath = outputPath;
        return this;
    }

    public static String getObjectPackage(String uri) {
        return packageName(FilenameUtils.getFullPathNoEndSeparator(uri));
    }

    public String getBasePackage() {
        return basePackage;
    }

    public String getBaseJsonObjectsPackage() {
        return getBaseObjectsPackage("jsonObjects");
    }

    public String getBaseXmlObjectsPackage() {
        return getBaseObjectsPackage("xmlObjects");
    }

    private String getBaseObjectsPackage(String type) {
        return String.format("%s.%s", basePackage, type);
    }

    public Path getInputPath() {
        return inputPath;
    }

    public Path getOutputPath() {
        return outputPath;
    }
}
