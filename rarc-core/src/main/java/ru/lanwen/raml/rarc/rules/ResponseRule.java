package ru.lanwen.raml.rarc.rules;

import org.raml.model.MimeType;
import ru.lanwen.raml.rarc.rules.RuleFactory.ResourceClassBuilder;
import ru.lanwen.raml.rarc.util.JsonCodegen;

import java.io.IOException;

import static ru.lanwen.raml.rarc.api.ApiResourceClass.packageName;
import static ru.lanwen.raml.rarc.rules.BodyRule.MimeTypeEnum.byMimeType;
import static ru.lanwen.raml.rarc.util.JsonCodegenConfig.jsonCodegenConfig;

/**
 * Created by stassiak
 */
public class ResponseRule implements Rule<MimeType, ResourceClassBuilder>{
    RuleFactory ruleFactory;

    public ResponseRule(RuleFactory ruleFactory) {
        this.ruleFactory = ruleFactory;
    }

    @Override
    public void apply(MimeType mimeType, ResourceClassBuilder resourceClassBuilder) {
        switch (byMimeType(mimeType)){
            case JSON:
                if (mimeType.getCompiledSchema() != null) {
                    try {
                        String respClass = new JsonCodegen(
                                jsonCodegenConfig()
                                        .withJsonSchemaPath(mimeType.getCompiledSchema().toString())
                                        .withPackageName(ruleFactory.getCodegenConfig().getBasePackage() + "."
                                                + packageName(resourceClassBuilder.getResource()) + ".responses")
                                        .withInputPath(ruleFactory.getCodegenConfig().getInputPath().getParent())
                                        .withOutputPath(ruleFactory.getCodegenConfig().getOutputPath())
                        ).generate();
                        if(!resourceClassBuilder.getResponseParserClass().containsParser(respClass)) {
                            resourceClassBuilder.getResponseParserClass().addParser(respClass);
                        }

                    } catch (IOException e) {
                        throw new RuntimeException("Can't generate code for response: "
                                + resourceClassBuilder.getResource().getUri(), e);
                    }

                }
        }
    }
}
