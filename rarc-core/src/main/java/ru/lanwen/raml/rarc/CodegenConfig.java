package ru.lanwen.raml.rarc;

import java.nio.file.Path;

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

    public String getBasePackage() {
        return basePackage;
    }

    public String getBaseObjectsPackage() {
        return String.format("%s.%s", basePackage, "objects");
    }

    public Path getInputPath() {
        return inputPath;
    }

    public Path getOutputPath() {
        return outputPath;
    }
}
