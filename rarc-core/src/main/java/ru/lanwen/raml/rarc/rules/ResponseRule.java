package ru.lanwen.raml.rarc.rules;

import org.apache.commons.io.FileUtils;
import org.raml.model.MimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lanwen.raml.rarc.util.JsonCodegen;
import ru.lanwen.raml.rarc.util.ResponseCodegenConfig;
import ru.lanwen.raml.rarc.util.XmlCodegen;

import java.io.File;
import java.nio.file.Paths;

import static ru.lanwen.raml.rarc.CodegenConfig.getObjectPackage;
import static ru.lanwen.raml.rarc.rules.BodyRule.MimeTypeEnum.byMimeType;

/**
 * Created by stassiak
 */
public class ResponseRule implements Rule<MimeType> {
    private final Logger LOG = LoggerFactory.getLogger(ResponseRule.class);

    @Override
    public void apply(MimeType mimeType, ResourceClassBuilder resourceClassBuilder) {
        if (mimeType.getCompiledSchema() == null) {
            return;
        }
        LOG.info("Process {}", mimeType.toString());
        ResponseCodegenConfig responseCodegenConfig = ResponseCodegenConfig.config()
                .withOutputPath(resourceClassBuilder.getCodegenConfig().getOutputPath());

        String respClass = null;
        try {
            switch (byMimeType(mimeType)) {
                case XML:
                    File xsd = File.createTempFile("schema", "xsd");
                    FileUtils.write(xsd, mimeType.getSchema());
                    respClass = new XmlCodegen(responseCodegenConfig
                            .withPackageName(resourceClassBuilder.getCodegenConfig().getBaseXmlObjectsPackage() +
                                    "." + getObjectPackage(xsd.getName()))
                            .withSchemaPath(xsd.getName())
                            .withInputPath(Paths.get(xsd.getParent()))).generate();
                    xsd.delete();
                    break;
                case JSON:
                    respClass = new JsonCodegen(
                            responseCodegenConfig
                                    .withPackageName(resourceClassBuilder.getCodegenConfig()
                                            .getBaseJsonObjectsPackage() +
                                            "." + getObjectPackage(mimeType.getCompiledSchema().toString()))
                                    .withSchemaPath(mimeType.getCompiledSchema().toString())
                                    .withInputPath(resourceClassBuilder.getCodegenConfig().getInputPath().getParent())
                    ).generate();
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not generate sources for response: " + e.toString());
        }

        if (respClass != null && !resourceClassBuilder.getResponseParser().containsParser(respClass)) {
            resourceClassBuilder.getResponseParser().addParser(respClass, byMimeType(mimeType),
                    resourceClassBuilder.getCodegenConfig().getBaseJsonObjectsPackage() +
                            "." + getObjectPackage(mimeType.getCompiledSchema().toString())
            );
        }
    }
}
