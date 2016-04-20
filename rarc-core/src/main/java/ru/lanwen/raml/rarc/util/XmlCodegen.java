package ru.lanwen.raml.rarc.util;

import org.exolab.castor.builder.SourceGenerator;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.capitalize;
import static ru.lanwen.raml.rarc.api.ApiResourceClass.sanitize;

/**
 * Created by stassiak
 */
public class XmlCodegen {
    private final JsonCodegenConfig config;

    public XmlCodegen(JsonCodegenConfig config) {
        this.config = config;
    }

    public String generate() throws IOException {
        SourceGenerator generator = new SourceGenerator();
        String xmlSchema = config.getInputPath();
        InputSource inputSource = new InputSource(xmlSchema);
        generator.setDestDir(config.getOutputPath());
        generator.setGenerateImportedSchemas(true);
        generator.setClassDescFieldNames(true);
        generator.setPrimitiveWrapper(true);
        generator.setUseEnumeratedTypeInterface(true);
        generator.setSAX1(true);

        // uncomment to set a resource destination directory
        // generator.setResourceDestinationDirectory("./target/codegen/src/test/resources");

        generator.setSuppressNonFatalWarnings(true);

        // uncomment to have JDO-specific class descriptors created
        generator.setJdoDescriptorCreation(true);

        // uncomment to use Velocity for code generation
        //generator.setJClassPrinterType("velocity");

        // uncomment the next line to set a binding file for source generation
        // generator.setBinding(new
        // InputSource(getClass().getResource("binding.xml").toExternalForm()));

        // uncomment the next lines to set custom properties for source generation
        // Properties properties = new Properties();
        // properties.load(getClass().getResource("builder.properties").openStream());
        // generator.setDefaultProperties(properties);
        generator.setVerbose(true);
        generator.generateSource(inputSource, config.getPackageName());
        return getClassName();
    }

    private String getClassName() {
        Matcher matcher = Pattern.compile("\\/([\\w]+).xsd").matcher(config.getJsonSchemaPath());
        matcher.find();

        return capitalize(sanitize(matcher.group(1)));
    }
}
