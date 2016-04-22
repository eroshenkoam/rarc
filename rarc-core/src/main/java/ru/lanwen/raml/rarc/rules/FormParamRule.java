package ru.lanwen.raml.rarc.rules;

import org.raml.model.parameter.FormParameter;
import ru.lanwen.raml.rarc.api.ra.AddFormParamMethod;

/**
 * Created by stassiak
 */
public class FormParamRule implements Rule<FormParameter>{
    @Override
    public void apply(FormParameter param, ResourceClassBuilder resourceClassBuilder) {
        resourceClassBuilder.getApiClass()
                .withMethod(new AddFormParamMethod(param, param.getDisplayName(),
                        resourceClassBuilder.getReq(), resourceClassBuilder.getApiClass()));

        resourceClassBuilder.getDefaultsMethod().forParamDefaults(param.getDisplayName(), param);
    }
}
