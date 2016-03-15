package ru.lanwen.raml.rarc;

import com.squareup.javapoet.*;
import org.apache.commons.lang3.StringUtils;
import org.raml.model.MimeType;
import org.raml.model.Raml;
import org.raml.model.Resource;
import org.raml.model.parameter.UriParameter;
import org.raml.parser.loader.FileResourceLoader;
import org.raml.parser.visitor.RamlDocumentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lanwen.raml.rarc.api.ApiResourceClass;
import ru.lanwen.raml.rarc.api.ra.*;
import ru.lanwen.raml.rarc.api.ra.root.NestedConfigClass;
import ru.lanwen.raml.rarc.api.ra.root.ReqSpecSupplField;
import ru.lanwen.raml.rarc.api.ra.root.RootApiClase;
import ru.lanwen.raml.rarc.util.JsonCodegen;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.*;
import static ru.lanwen.raml.rarc.api.ApiResourceClass.*;
import static ru.lanwen.raml.rarc.api.ra.AddJsonBodyMethod.bodyMethod;
import static ru.lanwen.raml.rarc.api.ra.ChangeSpecsMethods.changeReq;
import static ru.lanwen.raml.rarc.api.ra.ChangeSpecsMethods.changeResp;
import static ru.lanwen.raml.rarc.api.ra.Constructors.defaultConstructor;
import static ru.lanwen.raml.rarc.api.ra.Constructors.specsConstructor;
import static ru.lanwen.raml.rarc.api.ra.NextResourceMethods.childResource;
import static ru.lanwen.raml.rarc.util.JsonCodegenConfig.jsonCodegenConfig;

/**
 * @author lanwen (Merkushev Kirill)
 */
public class RestAssuredRamlCodegen {
    private final Logger LOG = LoggerFactory.getLogger(RestAssuredRamlCodegen.class);

    private final CodegenConfig config;

    public RestAssuredRamlCodegen(CodegenConfig config) {
        this.config = config;
    }

    public void generate() throws IOException {
        Path path = config.getInputPath();
        LOG.info("RAML: {}", path.toAbsolutePath());
        if (path.endsWith(".raml")) {
            LOG.warn("Wrong path - should end with .raml");
            return;
        }

        Raml raml = new RamlDocumentBuilder(new FileResourceLoader(path.getParent().toFile())).build(path.getFileName().toString());

        ReqSpecField req = new ReqSpecField();
        RespSpecField resp = new RespSpecField();

        ReqSpecSupplField baseReqSpec = new ReqSpecSupplField();
        NestedConfigClass nestedConfigClass = new NestedConfigClass(raml.getTitle(), baseReqSpec, req);
        new RootApiClase(nestedConfigClass).javaFile(raml, this.config.getBasePackage()).writeTo(this.config.getOutputPath());

        List<Resource> resources = raml.getResources().values().stream()
                .flatMap(res -> fromResource(res).stream()).collect(toList());

        List<JavaFile> files = resources.stream()
                .map(resource -> {
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
                                        new JsonCodegen(
                                                jsonCodegenConfig()
                                                        .withJsonSchemaPath(jsonBody.getCompiledSchema().toString())
                                                        .withPackageName(config.getBasePackage() + "." + packageName(resource) + ".responses")
                                                        .withInputPath(config.getInputPath().getParent())
                                                        .withOutputPath(config.getOutputPath())
                                        ).generate();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                }
                            }
                        });
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
                })
                .collect(toList());

        files.forEach(file -> {
            try {
                file.writeTo(this.config.getOutputPath());
            } catch (IOException e) {
                throw new RuntimeException("Can't write to " + this.config.getOutputPath(), e);
            }
        });
    }

    private static Collection<Resource> fromResource(Resource resource) {
        // in case of /account/{uid}/options/
        if (resource.getParentResource() != null && !resource.getParentResource().getUriParameters().isEmpty()) {
            Map<String, UriParameter> combined = new HashMap<>();
            combined.putAll(resource.getParentResource().getUriParameters());
            combined.putAll(resource.getUriParameters());
            resource.setUriParameters(combined);
        }
        if (resource.getResources().isEmpty()) {
            return Collections.singleton(resource);
        } else {
            List<Resource> all = new ArrayList<>();
            all.add(resource);
            for (Resource next : resource.getResources().values()) {
                all.addAll(fromResource(next));
            }
            return all;
        }
    }

}
