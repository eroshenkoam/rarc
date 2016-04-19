package ru.lanwen.raml.rarc.rules;

import com.squareup.javapoet.*;
import org.apache.commons.lang3.StringUtils;
import org.raml.model.MimeType;
import org.raml.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lanwen.raml.rarc.CodegenConfig;
import ru.lanwen.raml.rarc.api.ApiResourceClass;
import ru.lanwen.raml.rarc.api.ra.*;
import ru.lanwen.raml.rarc.util.JsonCodegen;
import ru.lanwen.raml.rarc.util.ResponseParserClass;

import javax.lang.model.element.Modifier;
import java.io.IOException;

import static org.apache.commons.lang3.StringUtils.*;
import static ru.lanwen.raml.rarc.api.ApiResourceClass.*;
import static ru.lanwen.raml.rarc.api.ra.AddJsonBodyMethod.bodyMethod;
import static ru.lanwen.raml.rarc.api.ra.ChangeSpecsMethods.changeReq;
import static ru.lanwen.raml.rarc.api.ra.ChangeSpecsMethods.changeResp;
import static ru.lanwen.raml.rarc.api.ra.Constructors.defaultConstructor;
import static ru.lanwen.raml.rarc.api.ra.Constructors.specsConstructor;
import static ru.lanwen.raml.rarc.api.ra.NextResourceMethods.childResource;
import static ru.lanwen.raml.rarc.util.JsonCodegenConfig.jsonCodegenConfig;
import static ru.lanwen.raml.rarc.util.ResponseParserClass.respParserForResource;

/**
 * Created by stassiak
 */
public class ResourseRule implements Rule<Resource, JavaFile>{
    private final Logger LOG = LoggerFactory.getLogger(ResourseRule.class);
    private RuleFactory ruleFactory;
    ReqSpecField req;
    RespSpecField resp;
    CodegenConfig config;

    public ResourseRule(RuleFactory ruleFactory) {
        this.ruleFactory = ruleFactory;
        this.req = ruleFactory.req;
        this.resp = ruleFactory.resp;
        this.config = ruleFactory.getCodegenConfig();
    }

    @Override
    public JavaFile apply(Resource resource) {
        LOG.info("Process resource {}", resource.getUri());
        UriConst uri = new UriConst(resource.getUri());

        ApiResourceClass apiClass = ApiResourceClass.forResource(resource)
                .withField(uri)
                .withField(req)
                .withField(resp);

        DefaultsMethod defaultsMethod = new DefaultsMethod(apiClass, req);

        resource.getUriParameters().forEach((name, uriParameter) -> {
            apiClass.withMethod(new AddPathParamMethod(uriParameter, name, req, apiClass));
            defaultsMethod.forParamDefaults(name, uriParameter);
        });


        //TODO выносить имена параметров в константы
        // TODO булевые и интежер типы

        ResponseParserClass parser = respParserForResource(resource);
        resource.getActions().forEach((type, action) -> {
            apiClass.withMethod(new ActionMethod(req, resp, uri, action));

            action.getQueryParameters().forEach((name, param) -> {
                apiClass.withMethod(new AddQueryParamMethod(param, name, req, apiClass));
                defaultsMethod.forParamDefaults(name, param);
                if (param.getEnumeration() != null && !param.getEnumeration().isEmpty()) {
                    TypeSpec.Builder enumParam = TypeSpec.enumBuilder(capitalize(sanitize(name)) + "Param")
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
                                    enumParam(value),
                                    TypeSpec.anonymousClassBuilder("$S", value).build()
                            ));
                    apiClass.withEnum(enumParam.build());
                    // для энума
                    apiClass.withMethod(() -> {
                        String sanitized = sanitize(name);
                        return MethodSpec.methodBuilder("with" + capitalize(sanitized))
                                .addJavadoc("required: $L\n", param.isRequired())
                                .addJavadoc("$L\n", isNotEmpty(param.getExample()) ? "example: " + param.getExample() : "")
                                .addJavadoc("@param $L $L\n", sanitized, trimToEmpty(param.getDescription()))
                                .addModifiers(Modifier.PUBLIC)
                                .returns(ClassName.bestGuess(apiClass.name()))
                                .varargs(param.isRepeat())
                                .addParameter(param.isRepeat() ? ArrayTypeName.of(ClassName.bestGuess(enumParam.build().name)) : ClassName.bestGuess(enumParam.build().name), sanitized)
                                .addStatement("$L.addQueryParam($S, $L.value())", req.name(), name, sanitized)
                                .addStatement("return this", req.name())
                                .build();
                    });
                }
            });

            action.getHeaders().forEach((name, header) -> {
                apiClass.withMethod(new AddHeaderMethod(header, name, req, apiClass));
                defaultsMethod.forParamDefaults(name, header);
            });

            if (action.getBody() != null) {
                if (action.getBody().containsKey("application/x-www-form-urlencoded")) {
                    action.getBody().get("application/x-www-form-urlencoded").getFormParameters()
                            .forEach((name, formParameters) -> {
                                if (formParameters.isEmpty()) {
                                    return;
                                }
                                LOG.info("Form params for {}: {}", name, formParameters.size());
                                apiClass.withMethod(new AddFormParamMethod(formParameters.get(0), name, req, apiClass));
                                defaultsMethod.forParamDefaults(name, formParameters.get(0));
                                if (formParameters.get(0).getEnumeration() != null && !formParameters.get(0).getEnumeration().isEmpty()) {
                                    TypeSpec.Builder enumParam = TypeSpec.enumBuilder(capitalize(sanitize(name)) + "Param")
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
                                    formParameters.get(0).getEnumeration()
                                            .forEach(value -> enumParam.addEnumConstant(
                                                    StringUtils.upperCase(sanitize(value)),
                                                    TypeSpec.anonymousClassBuilder("$S", value).build()
                                            ));
                                    apiClass.withEnum(enumParam.build());
                                    apiClass.withMethod(() -> {
                                        // для энума
                                        String sanitized = sanitize(name);
                                        return MethodSpec.methodBuilder("with" + capitalize(sanitized))
                                                .addJavadoc("required: $L\n", formParameters.get(0).isRequired())
                                                .addJavadoc("$L\n", isNotEmpty(formParameters.get(0).getExample()) ? "example: " + formParameters.get(0).getExample() : "")
                                                .addJavadoc("@param $L $L\n", sanitized, trimToEmpty(formParameters.get(0).getDescription()))
                                                .addModifiers(Modifier.PUBLIC)
                                                .returns(ClassName.bestGuess(apiClass.name()))
                                                .varargs(formParameters.get(0).isRepeat())
                                                .addParameter(formParameters.get(0).isRepeat() ? ArrayTypeName.of(ClassName.bestGuess(enumParam.build().name)) : ClassName.bestGuess(enumParam.build().name), sanitized)
                                                .addStatement("$L.addQueryParam($S, $L.value())", req.name(), name, sanitized)
                                                .addStatement("return this", req.name())
                                                .build();
                                    });
                                }
                            });
                } else if (action.getBody().containsKey("application/json")) {
                    MimeType jsonBody = action.getBody().get("application/json");
                    apiClass.withMethod(
                            bodyMethod()
                                    .withShema(jsonBody.getCompiledSchema())
                                    .withExample(jsonBody.getExample())
                                    .withReqName(req.name())
                                    .withInputPathForJsonGen(config.getInputPath().getParent())
                                    .withOutputPathForJsonGen(config.getOutputPath())
                                    .withPackageForJsonGen(config.getBasePackage() + "." + packageName(resource))
                                    .returns(apiClass.name()));
                }
            }

            action.getResponses().values().forEach(response -> {
                if (response.hasBody() && response.getBody().containsKey("application/json")) {
                    MimeType jsonBody = response.getBody().get("application/json");
                    if (jsonBody.getCompiledSchema() != null) {
                        try {
                            String respClass = new JsonCodegen(
                                    jsonCodegenConfig()
                                            .withJsonSchemaPath(jsonBody.getCompiledSchema().toString())
                                            .withPackageName(config.getBasePackage() + "." + packageName(resource) + ".responses")
                                            .withInputPath(config.getInputPath().getParent())
                                            .withOutputPath(config.getOutputPath())
                            ).generate();
                            if(!parser.containsParser(respClass)) {
                                parser.addParser(respClass);
                            }

                        } catch (IOException e) {
                            throw new RuntimeException("Can't generate code for response: " + resource.getUri(), e);
                        }

                    }
                }
            });

            try {
                if(!parser.isEmpty()){
                    parser.javaFile(config.getBasePackage()).writeTo(config.getOutputPath());
                }
            } catch (IOException e) {
                throw new RuntimeException("Can't write to " + this.config.getOutputPath(), e);
            }
        });

        // TODO: default как название параметра
        apiClass.withMethod(defaultConstructor(req, resp))
                .withMethod(specsConstructor(req, resp))
                .withMethod(defaultsMethod)
                .withMethod(changeReq(req, apiClass))
                .withMethod(changeResp(resp, apiClass));

        resource.getResources().values().stream()
                .forEach(child -> apiClass.withMethod(
                        () -> childResource(child, this.config.getBasePackage(), req.name())
                ));

        return apiClass.javaFile(this.config.getBasePackage());
    }
}
