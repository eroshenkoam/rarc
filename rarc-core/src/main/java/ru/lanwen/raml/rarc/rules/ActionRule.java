package ru.lanwen.raml.rarc.rules;

import org.raml.model.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lanwen.raml.rarc.api.ApiResourceClass;
import ru.lanwen.raml.rarc.api.ra.ActionMethod;

/**
 * Created by stassiak
 */
public class ActionRule implements Rule<Action>{
    private final Logger LOG = LoggerFactory.getLogger(Action.class);
    RuleFactory ruleFactory;

    public ActionRule(RuleFactory ruleFactory) {
        this.ruleFactory = ruleFactory;
    }

    @Override
    public void apply(Action action, ResourceClassBuilder resourceClassBuilder) {
        LOG.info("Process action " + action.toString());

        ApiResourceClass apiClass = resourceClassBuilder.getApiClass();

        apiClass.withMethod(
                new ActionMethod(ruleFactory.getReq(), ruleFactory.getResp(), resourceClassBuilder.getUri(), action));

        action.getQueryParameters().entrySet().stream().forEach(resourceClassBuilder.applyQueryParamRule);
        action.getHeaders().entrySet().stream().forEach(resourceClassBuilder.applyHeaderRule);
        if (action.getBody() != null) {
            action.getBody().values().stream().forEach(resourceClassBuilder.applyBodyRule);
        }
        action.getResponses().values().stream().forEach(resourceClassBuilder.applyResponseRule);
    }
}
