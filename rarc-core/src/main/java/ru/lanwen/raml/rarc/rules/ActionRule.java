package ru.lanwen.raml.rarc.rules;

import org.raml.model.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lanwen.raml.rarc.api.ApiResourceClass;
import ru.lanwen.raml.rarc.api.ra.ActionMethod;
import ru.lanwen.raml.rarc.rules.RuleFactory.ResourceClassBuilder;

/**
 * Created by stassiak
 */
public class ActionRule implements Rule<Action, ResourceClassBuilder>{
    private final Logger LOG = LoggerFactory.getLogger(Action.class);
    RuleFactory ruleFactory;

    public ActionRule(RuleFactory ruleFactory) {
        this.ruleFactory = ruleFactory;
    }

    @Override
    public void apply(Action action, ResourceClassBuilder resourceClassBuilder) {
        //TODO выносить имена параметров в константы
        // TODO булевые и интежер типы
        LOG.info("Process action " + action.toString());
        ApiResourceClass apiClass = resourceClassBuilder.getApiClass();

        apiClass.withMethod(
                new ActionMethod(ruleFactory.getReq(), ruleFactory.getResp(), resourceClassBuilder.getUri(), action));

        action.getQueryParameters().forEach((name, param) -> {
            param.setDisplayName(name);
            ruleFactory.getParameterRule().apply(param, resourceClassBuilder);
        });

        action.getHeaders().forEach((name, header) -> {
            header.setDisplayName(name);
            ruleFactory.getHeaderRule().apply(header, resourceClassBuilder);
        });

        if (action.getBody() != null) {
            action.getBody().values().forEach(body -> {
                ruleFactory.getBodyRule().apply(body, resourceClassBuilder);
            });
        }

        action.getResponses().values().forEach(response -> {
            if(response.hasBody()){
                response.getBody().values().forEach(mimeType -> {
                    ruleFactory.getResponseRule().apply(mimeType, resourceClassBuilder);
                });
            }
        });
    }
}
