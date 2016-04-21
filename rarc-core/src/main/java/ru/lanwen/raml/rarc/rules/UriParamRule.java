package ru.lanwen.raml.rarc.rules;

import org.raml.model.parameter.UriParameter;
import ru.lanwen.raml.rarc.api.ra.AddPathParamMethod;

/**
 * Created by stassiak
 */
public class UriParamRule implements Rule<UriParameter>{
    RuleFactory ruleFactory;

    public UriParamRule(RuleFactory ruleFactory) {
        this.ruleFactory = ruleFactory;
    }

    @Override
    public void apply(UriParameter param, ResourceClassBuilder resourceClassBuilder) {
        resourceClassBuilder.getApiClass().withMethod(
                new AddPathParamMethod(param, param.getDisplayName(), ruleFactory.getReq(),
                        resourceClassBuilder.getApiClass()));
        resourceClassBuilder.getDefaultsMethod().forParamDefaults(param.getDisplayName(), param);
    }
}
