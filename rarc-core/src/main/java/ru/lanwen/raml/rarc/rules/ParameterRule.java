package ru.lanwen.raml.rarc.rules;

import org.raml.model.parameter.AbstractParam;
import org.raml.model.parameter.FormParameter;
import org.raml.model.parameter.QueryParameter;
import org.raml.model.parameter.UriParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lanwen.raml.rarc.api.ra.DefaultsMethod;

/**
 * Created by stassiak
 */
public class ParameterRule implements Rule<AbstractParam>{
    private final Logger LOG = LoggerFactory.getLogger(ParameterRule.class);
    RuleFactory ruleFactory;

    public ParameterRule(RuleFactory ruleFactory) {
        this.ruleFactory = ruleFactory;
    }

    @Override
    public void apply(AbstractParam param, ResourceClassBuilder resourceClassBuilder) {
        LOG.info(String.format("Process %s param with name \"%s\"",
                DefaultsMethod.Param.byClass(param.getClass()), param.getDisplayName()));
        switch (DefaultsMethod.Param.byClass(param.getClass())) {
            case URI_PARAM:
                ruleFactory.getUriParamRule().apply((UriParameter) param, resourceClassBuilder);
                break;
            case QUERY:
                ruleFactory.getQueryParamRule().apply((QueryParameter) param, resourceClassBuilder);
                break;
            case FORM:
                ruleFactory.getFormParamRule().apply((FormParameter) param, resourceClassBuilder);

        }
    }
}
