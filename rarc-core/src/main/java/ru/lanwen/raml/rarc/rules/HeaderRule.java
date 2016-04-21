package ru.lanwen.raml.rarc.rules;

import org.raml.model.parameter.Header;
import ru.lanwen.raml.rarc.api.ra.AddHeaderMethod;

/**
 * Created by stassiak
 */
public class HeaderRule implements Rule<Header>{
    RuleFactory ruleFactory;

    public HeaderRule(RuleFactory ruleFactory) {
        this.ruleFactory = ruleFactory;
    }

    @Override
    public void apply(Header header, ResourceClassBuilder resourceClassBuilder) {
        resourceClassBuilder.getApiClass()
                .withMethod(new AddHeaderMethod(header, header.getDisplayName(), ruleFactory.getReq(),
                        resourceClassBuilder.getApiClass()));
        resourceClassBuilder.getDefaultsMethod().forParamDefaults(header.getDisplayName(), header);
    }
}
