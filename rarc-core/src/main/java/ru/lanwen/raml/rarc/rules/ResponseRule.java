package ru.lanwen.raml.rarc.rules;

import org.apache.commons.io.FileUtils;
import org.raml.model.MimeType;
import ru.lanwen.raml.rarc.util.JsonCodegen;
import ru.lanwen.raml.rarc.util.ResponseCodegenConfig;
import ru.lanwen.raml.rarc.util.XmlCodegen;

import java.io.File;
import java.nio.file.Paths;

import static ru.lanwen.raml.rarc.api.ApiResourceClass.packageName;
import static ru.lanwen.raml.rarc.rules.BodyRule.MimeTypeEnum.byMimeType;

/**
 * Created by stassiak
 */
public class ResponseRule implements Rule<MimeType> {
    RuleFactory ruleFactory;

    public ResponseRule(RuleFactory ruleFactory) {
        this.ruleFactory = ruleFactory;
    }

    @Override
    public void apply(MimeType mimeType, ResourceClassBuilder resourceClassBuilder) {
        if (mimeType.getCompiledSchema() == null) {
            return;
        }

        ResponseCodegenConfig responseCodegenConfig = ResponseCodegenConfig.config()
                .withPackageName(ruleFactory.getCodegenConfig().getBasePackage() + "."
                        + packageName(resourceClassBuilder.getResource()) + ".responses")
                .withOutputPath(ruleFactory.getCodegenConfig().getOutputPath());

        try {
            switch (byMimeType(mimeType)) {
                case XML:
                    File xsd = File.createTempFile("schema", "xsd");
                    FileUtils.write(xsd, mimeType.getSchema());
                    new XmlCodegen(responseCodegenConfig
                            .withSchemaPath(xsd.getName())
                            .withInputPath(Paths.get(xsd.getParent()))).generate();
                    xsd.delete();
                    break;
                case JSON:
                    String respClass = new JsonCodegen(
                            responseCodegenConfig
                                    .withSchemaPath(mimeType.getCompiledSchema().toString())
                                    .withInputPath(ruleFactory.getCodegenConfig().getInputPath().getParent())
                    ).generate();
                    if (!resourceClassBuilder.getResponseParser().containsParser(respClass)) {
                        resourceClassBuilder.getResponseParser().addParser(respClass);
                    }
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not generate sources for response: " + e.toString());
        }


    }
}
