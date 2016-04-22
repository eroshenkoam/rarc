package ru.lanwen.raml.rarc.rules;

import org.raml.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lanwen.raml.rarc.CodegenConfig;
import ru.lanwen.raml.rarc.api.ra.ReqSpecField;
import ru.lanwen.raml.rarc.api.ra.RespSpecField;

import static ru.lanwen.raml.rarc.api.ra.ChangeSpecsMethods.changeReq;
import static ru.lanwen.raml.rarc.api.ra.ChangeSpecsMethods.changeResp;
import static ru.lanwen.raml.rarc.api.ra.Constructors.defaultConstructor;
import static ru.lanwen.raml.rarc.api.ra.Constructors.specsConstructor;
import static ru.lanwen.raml.rarc.api.ra.NextResourceMethods.childResource;

/**
 * Created by stassiak
 */
public class ResourseRule implements Rule<Resource>{
    private final Logger LOG = LoggerFactory.getLogger(ResourseRule.class);

    @Override
    public void apply(Resource resource, ResourceClassBuilder resourceClassBuilder) {
        LOG.info("Process resource {}", resource.getUri());
        ReqSpecField req = resourceClassBuilder.getReq();
        RespSpecField resp = resourceClassBuilder.getResp();
        CodegenConfig config = resourceClassBuilder.getCodegenConfig();

        resource.getUriParameters().entrySet().stream().forEach(resourceClassBuilder.applyUriParamRule);

        resource.getActions().entrySet().stream().forEach(resourceClassBuilder.applyActionRule);

        // TODO: default как название параметра
        resourceClassBuilder.getApiClass().withMethod(defaultConstructor(req, resp))
                .withMethod(specsConstructor(req, resp))
                .withMethod(resourceClassBuilder.getDefaultsMethod())
                .withMethod(changeReq(req, resourceClassBuilder.getApiClass()))
                .withMethod(changeResp(resp, resourceClassBuilder.getApiClass()));

        resource.getResources().values().stream()
                .forEach(child -> resourceClassBuilder.getApiClass().withMethod(
                        () -> childResource(child, config.getBasePackage(), req.name())
                ));

        if(!resourceClassBuilder.getResponseParser().isEmpty()){
            resourceClassBuilder.getJavaFiles()
                    .add(resourceClassBuilder.getResponseParser().javaFile(config.getBasePackage()));
        }

        resourceClassBuilder.getJavaFiles()
                .add(resourceClassBuilder.getApiClass().javaFile(config.getBasePackage()));
    }


}
