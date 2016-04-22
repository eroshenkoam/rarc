package ru.lanwen.raml.rarc.rules;

import org.raml.model.parameter.QueryParameter;
import ru.lanwen.raml.rarc.api.ra.AddQueryParamMethod;

/**
 * Created by stassiak
 */
public class QueryParamRule implements Rule<QueryParameter> {
    @Override
    public void apply(QueryParameter param, ResourceClassBuilder resourceClassBuilder) {
        resourceClassBuilder.getApiClass().withMethod(
                new AddQueryParamMethod(param, param.getDisplayName(), resourceClassBuilder.getReq(),
                        resourceClassBuilder.getApiClass()));

        resourceClassBuilder.getDefaultsMethod().forParamDefaults(param.getDisplayName(), param);
    }
}
