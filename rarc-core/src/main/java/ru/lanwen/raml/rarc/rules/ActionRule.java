package ru.lanwen.raml.rarc.rules;

import org.raml.model.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lanwen.raml.rarc.api.ra.ActionMethod;

/**
 * Created by stassiak
 */
public class ActionRule implements Rule<Action>{
    private final Logger LOG = LoggerFactory.getLogger(ActionRule.class);

    @Override
    public void apply(Action action, ResourceClassBuilder resourceClassBuilder) {
        LOG.info("Process action {}", action);

        resourceClassBuilder.getApiClass().withMethod(
                new ActionMethod(resourceClassBuilder.getReq(),
                        resourceClassBuilder.getResp(),
                        resourceClassBuilder.getUri(),
                        action));

        action.getQueryParameters().forEach(resourceClassBuilder.applyParamRule);
        action.getHeaders().forEach(resourceClassBuilder.applyParamRule);
        if (action.getBody() != null) {
            action.getBody().values().forEach(resourceClassBuilder.applyBodyRule);
        }
        action.getResponses().values().forEach(resourceClassBuilder.applyResponseRule);
    }
}
