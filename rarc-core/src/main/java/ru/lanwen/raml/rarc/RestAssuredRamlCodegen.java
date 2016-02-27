package ru.lanwen.raml.rarc;

import com.squareup.javapoet.JavaFile;
import org.raml.model.MimeType;
import org.raml.model.Raml;
import org.raml.model.Resource;
import org.raml.parser.loader.FileResourceLoader;
import org.raml.parser.visitor.RamlDocumentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lanwen.raml.rarc.api.ApiResourceClass;
import ru.lanwen.raml.rarc.api.ra.ActionMethod;
import ru.lanwen.raml.rarc.api.ra.AddFormParamMethod;
import ru.lanwen.raml.rarc.api.ra.AddHeaderMethod;
import ru.lanwen.raml.rarc.api.ra.AddPathParamMethod;
import ru.lanwen.raml.rarc.api.ra.AddQueryParamMethod;
import ru.lanwen.raml.rarc.api.ra.DefaultsMethod;
import ru.lanwen.raml.rarc.api.ra.ReqSpecField;
import ru.lanwen.raml.rarc.api.ra.RespSpecField;
import ru.lanwen.raml.rarc.api.ra.UriConst;
import ru.lanwen.raml.rarc.api.ra.root.NestedConfigClass;
import ru.lanwen.raml.rarc.api.ra.root.ReqSpecSupplField;
import ru.lanwen.raml.rarc.api.ra.root.RootApiClase;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static ru.lanwen.raml.rarc.api.ra.ChangeSpecsMethods.changeReq;
import static ru.lanwen.raml.rarc.api.ra.ChangeSpecsMethods.changeResp;
import static ru.lanwen.raml.rarc.api.ra.Constructors.defaultConstructor;
import static ru.lanwen.raml.rarc.api.ra.Constructors.specsConstructor;
import static ru.lanwen.raml.rarc.api.ra.NextResourceMethods.childResource;

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

                    resource.getActions().forEach((type, action) -> {
                        apiClass.withMethod(new ActionMethod(req, resp, uri, action));

                        action.getQueryParameters().forEach((name, param) -> {
                            apiClass.withMethod(new AddQueryParamMethod(param, name, req, apiClass));
                            defaultsMethod.forParamDefaults(name, param);
                        });

                        action.getHeaders().forEach((name, header) -> {
                            apiClass.withMethod(new AddHeaderMethod(header, name, req, apiClass));
                            defaultsMethod.forParamDefaults(name, header);
                        });

                        if (action.getBody() != null) {
                            action.getBody().getOrDefault("application/x-www-form-urlencoded", new MimeType()).getFormParameters()
                                    .forEach((name, formParameters) -> {
                                        if (formParameters.isEmpty()) {
                                            return;
                                        }
                                        LOG.info("Form params for {}: {}", name, formParameters.size());
                                        apiClass.withMethod(new AddFormParamMethod(formParameters.get(0), name, req, apiClass));
                                        defaultsMethod.forParamDefaults(name, formParameters.get(0));
                                    });
                        }
                    });

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
