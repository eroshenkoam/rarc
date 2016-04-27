package ru.lanwen.raml.rarc.rules;

import org.raml.model.parameter.*;
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
        LOG.info("Process {} param with name \"{}\"",
                DefaultsMethod.Param.byClass(param.getClass()), param.getDisplayName());
        switch (DefaultsMethod.Param.byClass(param.getClass())) {
            case URI_PARAM:
                new UriParamRule().apply((UriParameter) param, resourceClassBuilder);
                break;
            case QUERY:
                new QueryParamRule().apply((QueryParameter) param, resourceClassBuilder);
                new EnumRule().apply(param, resourceClassBuilder);
                break;
            case FORM:
                new FormParamRule().apply((FormParameter) param, resourceClassBuilder);
                new EnumRule().apply(param, resourceClassBuilder);
                break;
            case HEADER:
                new HeaderRule().apply((Header) param, resourceClassBuilder);
        }
    }
}
