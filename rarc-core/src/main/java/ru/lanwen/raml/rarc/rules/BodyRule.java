package ru.lanwen.raml.rarc.rules;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.path.xml.XmlPath;
import org.raml.model.MimeType;

import java.util.stream.Stream;

import static ru.lanwen.raml.rarc.api.ApiResourceClass.packageName;
import static ru.lanwen.raml.rarc.api.ra.AddJsonBodyMethod.bodyMethod;
import static ru.lanwen.raml.rarc.rules.BodyRule.MimeTypeEnum.byMimeType;

/**
 * Created by stassiak
 */
public class BodyRule implements Rule<MimeType> {
    @Override
    public void apply(MimeType body, ResourceClassBuilder resourceClassBuilder) {
        switch (byMimeType(body)) {
            case FORM:
                body.getFormParameters().forEach(resourceClassBuilder.applyFormParamsRule);
                break;
            case JSON:
                resourceClassBuilder.getApiClass().withMethod(
                        bodyMethod()
                                .withShema(body.getCompiledSchema())
                                .withExample(body.getExample())
                                .withReqName(resourceClassBuilder.getReq().name())
                                .withInputPathForJsonGen(resourceClassBuilder.getCodegenConfig().getInputPath().getParent())
                                .withOutputPathForJsonGen(resourceClassBuilder.getCodegenConfig().getOutputPath())
                                .withPackageForJsonGen(resourceClassBuilder.getCodegenConfig().getBasePackage() + "."
                                        + packageName(resourceClassBuilder.getResource()))
                                .returns(resourceClassBuilder.getApiClass().name()));
                break;
        }

    }

    public enum MimeTypeEnum {
        FORM("application/x-www-form-urlencoded"),
        JSON("application/json"),
        XML("text/xml");

        private String mimeType;

        MimeTypeEnum(String mimeType) {
            this.mimeType = mimeType;
        }

        public static MimeTypeEnum byMimeType(MimeType mimeTypeObj) {
            return Stream.of(values()).filter(type -> type.mimeType.equals(mimeTypeObj.getType())).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No mimetype for " + mimeTypeObj.getType()));
        }

        public static Class getRaPathClass(MimeTypeEnum type) {
            switch (type) {
                case XML:
                    return XmlPath.class;
                case JSON:
                    return JsonPath.class;
            }
            return null;
        }
    }
}
