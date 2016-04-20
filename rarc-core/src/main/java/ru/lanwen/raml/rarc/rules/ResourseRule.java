package ru.lanwen.raml.rarc.rules;

import com.squareup.javapoet.JavaFile;
import org.raml.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lanwen.raml.rarc.CodegenConfig;
import ru.lanwen.raml.rarc.api.ra.ReqSpecField;
import ru.lanwen.raml.rarc.api.ra.RespSpecField;
import ru.lanwen.raml.rarc.rules.RuleFactory.ResourceClassBuilder;

import java.util.List;

import static ru.lanwen.raml.rarc.api.ra.ChangeSpecsMethods.changeReq;
import static ru.lanwen.raml.rarc.api.ra.ChangeSpecsMethods.changeResp;
import static ru.lanwen.raml.rarc.api.ra.Constructors.defaultConstructor;
import static ru.lanwen.raml.rarc.api.ra.Constructors.specsConstructor;
import static ru.lanwen.raml.rarc.api.ra.NextResourceMethods.childResource;

/**
 * Created by stassiak
 */
public class ResourseRule implements Rule<Resource, List<JavaFile>>{
    private final Logger LOG = LoggerFactory.getLogger(ResourseRule.class);
    private RuleFactory ruleFactory;
    ReqSpecField req;
    RespSpecField resp;
    CodegenConfig config;

    public ResourseRule(RuleFactory ruleFactory) {
        this.ruleFactory = ruleFactory;
        this.req = ruleFactory.getReq();
        this.resp = ruleFactory.getResp();
        this.config = ruleFactory.getCodegenConfig();
    }

    @Override
    public void apply(Resource resource, List<JavaFile> javaFiles) {
        LOG.info("Process resource {}", resource.getUri());
        ResourceClassBuilder resourseClass = ruleFactory.getResourceClass(resource);

        resource.getUriParameters().forEach((name, uriParameter) -> {
            uriParameter.setDisplayName(name);
            ruleFactory.getParameterRule().apply(uriParameter, resourseClass);
        });

        resource.getActions().forEach((type, action) -> {
            action.setType(type);
            ruleFactory.getActionRule().apply(action, resourseClass);
        });

        // TODO: default как название параметра
        resourseClass.getApiClass().withMethod(defaultConstructor(req, resp))
                .withMethod(specsConstructor(req, resp))
                .withMethod(resourseClass.getDefaultsMethod())
                .withMethod(changeReq(req, resourseClass.getApiClass()))
                .withMethod(changeResp(resp, resourseClass.getApiClass()));

        resource.getResources().values().stream()
                .forEach(child -> resourseClass.getApiClass().withMethod(
                        () -> childResource(child, this.config.getBasePackage(), req.name())
                ));

        if(!resourseClass.getResponseParserClass().isEmpty()){
            javaFiles.add(resourseClass.getResponseParserClass().javaFile(config.getBasePackage()));
        }

        javaFiles.add(resourseClass.getApiClass().javaFile(this.config.getBasePackage()));
    }


}
