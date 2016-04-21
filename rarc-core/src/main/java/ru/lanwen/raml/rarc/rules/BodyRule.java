package ru.lanwen.raml.rarc.rules;

import org.raml.model.MimeType;

import java.util.stream.Stream;

import static ru.lanwen.raml.rarc.api.ApiResourceClass.packageName;
import static ru.lanwen.raml.rarc.api.ra.AddJsonBodyMethod.bodyMethod;
import static ru.lanwen.raml.rarc.rules.BodyRule.MimeTypeEnum.byMimeType;

/**
 * Created by stassiak
 */
public class BodyRule implements Rule<MimeType> {
    RuleFactory ruleFactory;

    public BodyRule(RuleFactory ruleFactory) {
        this.ruleFactory = ruleFactory;
    }

    @Override
    public void apply(MimeType body, ResourceClassBuilder resourceClassBuilder) {
        switch (byMimeType(body)) {
            case FORM:
                body.getFormParameters().entrySet().stream().forEach(resourceClassBuilder.applyFormParamsRule);
                break;
            case JSON:
                resourceClassBuilder.getApiClass().withMethod(
                        bodyMethod()
                                .withShema(body.getCompiledSchema())
                                .withExample(body.getExample())
                                .withReqName(ruleFactory.getReq().name())
                                .withInputPathForJsonGen(ruleFactory.getCodegenConfig().getInputPath().getParent())
                                .withOutputPathForJsonGen(ruleFactory.getCodegenConfig().getOutputPath())
                                .withPackageForJsonGen(ruleFactory.getCodegenConfig().getBasePackage() + "."
                                        + packageName(resourceClassBuilder.getResource()))
                                .returns(resourceClassBuilder.getApiClass().name()));
                break;
        }

    }

    enum MimeTypeEnum {
        FORM("application/x-www-form-urlencoded"),
        JSON("application/json"),
        XML("application/xml");

        private String mimeType;

        MimeTypeEnum(String mimeType) {
            this.mimeType = mimeType;
        }

        public static MimeTypeEnum byMimeType(MimeType mimeTypeObj) {
            return Stream.of(values()).filter(type -> type.mimeType.equals(mimeTypeObj.getType())).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No mimetype for " + mimeTypeObj.getType()));
        }
    }
}
