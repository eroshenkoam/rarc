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

    @Override
    public void apply(AbstractParam param, ResourceClassBuilder resourceClassBuilder) {
        LOG.info(String.format("Process %s param with name \"%s\"",
                DefaultsMethod.Param.byClass(param.getClass()), param.getDisplayName()));
        switch (DefaultsMethod.Param.byClass(param.getClass())) {
            case URI_PARAM:
                new UriParamRule().apply((UriParameter) param, resourceClassBuilder);
                break;
            case QUERY:
                new QueryParamRule().apply((QueryParameter) param, resourceClassBuilder);
                break;
            case FORM:
                new FormParamRule().apply((FormParameter) param, resourceClassBuilder);

        }
    }
}
