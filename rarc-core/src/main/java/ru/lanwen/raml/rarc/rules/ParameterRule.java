package ru.lanwen.raml.rarc.rules;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.apache.commons.lang3.StringUtils;
import org.raml.model.parameter.AbstractParam;
import org.raml.model.parameter.FormParameter;
import org.raml.model.parameter.QueryParameter;
import org.raml.model.parameter.UriParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lanwen.raml.rarc.api.ra.AddFormParamMethod;
import ru.lanwen.raml.rarc.api.ra.AddPathParamMethod;
import ru.lanwen.raml.rarc.api.ra.DefaultsMethod;

import javax.lang.model.element.Modifier;

import static org.apache.commons.lang3.StringUtils.*;
import static ru.lanwen.raml.rarc.api.ApiResourceClass.sanitize;

/**
 * Created by stassiak
 */
public class ParameterRule implements Rule<AbstractParam, RuleFactory.ResourceClassBuilder>{
    private final Logger LOG = LoggerFactory.getLogger(ParameterRule.class);
    RuleFactory ruleFactory;

    public ParameterRule(RuleFactory ruleFactory) {
        this.ruleFactory = ruleFactory;
    }

    @Override
    public void apply(AbstractParam param, RuleFactory.ResourceClassBuilder resourceClassBuilder) {
        LOG.info(String.format("Process %s param with name \"%s\"",
                DefaultsMethod.Param.byClass(param.getClass()), param.getDisplayName()));
        switch (DefaultsMethod.Param.byClass(param.getClass())) {
            case URI_PARAM:
                LOG.info("URI params for {}", param.getDisplayName());
                resourceClassBuilder.getApiClass().withMethod(
                        new AddPathParamMethod((UriParameter) param, param.getDisplayName(), ruleFactory.getReq(),
                                resourceClassBuilder.getApiClass()));
                resourceClassBuilder.getDefaultsMethod().forParamDefaults(param.getDisplayName(), param);
                break;
            case QUERY:
                ruleFactory.getQueryParamRule().apply((QueryParameter) param, resourceClassBuilder);
                break;
            case FORM:
                LOG.info("Form params for {}", param.getDisplayName());
                resourceClassBuilder.getApiClass()
                        .withMethod(new AddFormParamMethod((FormParameter) param, param.getDisplayName(),
                                ruleFactory.getReq(), resourceClassBuilder.getApiClass()));
                resourceClassBuilder.getDefaultsMethod().forParamDefaults(param.getDisplayName(), param);
                if (param.getEnumeration() != null && !param.getEnumeration().isEmpty()) {
                    TypeSpec.Builder enumParam =
                            TypeSpec.enumBuilder(capitalize(sanitize(param.getDisplayName())) + "Param")
                            .addModifiers(Modifier.PUBLIC)
                            .addField(String.class, "value", Modifier.PRIVATE, Modifier.FINAL)
                            .addMethod(MethodSpec.methodBuilder("value")
                                    .addModifiers(Modifier.PUBLIC)
                                    .returns(String.class)
                                    .addStatement("return $N", "value")
                                    .build())
                            .addMethod(MethodSpec.constructorBuilder()
                                    .addParameter(String.class, "value")
                                    .addStatement("this.$N = $N", "value", "value")
                                    .build());
                    param.getEnumeration()
                            .forEach(value -> enumParam.addEnumConstant(
                                    StringUtils.upperCase(sanitize(value)),
                                    TypeSpec.anonymousClassBuilder("$S", value).build()
                            ));
                    resourceClassBuilder.getApiClass().withEnum(enumParam.build());
                    resourceClassBuilder.getApiClass().withMethod(() -> {
                        // для энума
                        String sanitized = sanitize(param.getDisplayName());
                        return MethodSpec.methodBuilder("with" + capitalize(sanitized))
                                .addJavadoc("required: $L\n", param.isRequired())
                                .addJavadoc("$L\n", isNotEmpty(param.getExample()) ?
                                        "example: " + param.getExample() : "")
                                .addJavadoc("@param $L $L\n", sanitized, trimToEmpty(param.getDescription()))
                                .addModifiers(Modifier.PUBLIC)
                                .returns(ClassName.bestGuess(resourceClassBuilder.getApiClass().name()))
                                .varargs(param.isRepeat())
                                .addParameter(param.isRepeat() ?
                                        ArrayTypeName.of(ClassName.bestGuess(enumParam.build().name)) :
                                        ClassName.bestGuess(enumParam.build().name), sanitized)
                                .addStatement("$L.addQueryParam($S, $L.value())", ruleFactory.getReq().name(),
                                        param.getDisplayName(), sanitized)
                                .addStatement("return this", ruleFactory.getReq().name())
                                .build();
                    });
                }
        }
    }
}
