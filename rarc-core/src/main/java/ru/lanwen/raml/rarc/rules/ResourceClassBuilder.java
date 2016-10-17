package ru.lanwen.raml.rarc.rules;

import com.squareup.javapoet.JavaFile;
import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.MimeType;
import org.raml.model.Resource;
import org.raml.model.Response;
import org.raml.model.parameter.AbstractParam;
import org.raml.model.parameter.FormParameter;
import org.raml.model.parameter.UriParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lanwen.raml.rarc.CodegenConfig;
import ru.lanwen.raml.rarc.api.ApiResourceClass;
import ru.lanwen.raml.rarc.api.ra.DefaultsMethod;
import ru.lanwen.raml.rarc.api.ra.ReqSpecField;
import ru.lanwen.raml.rarc.api.ra.RespSpecField;
import ru.lanwen.raml.rarc.api.ra.UriConst;
import ru.lanwen.raml.rarc.util.ResponseParserClass;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static ru.lanwen.raml.rarc.util.ResponseParserClass.respParserForResource;

/**
 * Created by stassiak
 */
public class ResourceClassBuilder {
    private final Logger LOG = LoggerFactory.getLogger(ResourceClassBuilder.class);

    private CodegenConfig codegenConfig;

    private Resource resource;
    private UriConst uri;

    private ApiResourceClass apiClass;
    private DefaultsMethod defaultsMethod;
    private ResponseParserClass responseParser;

    private ArrayList<JavaFile> javaFiles = new ArrayList<>();

    ReqSpecField req;
    RespSpecField resp = new RespSpecField();

    public ResourceClassBuilder withCodegenConfig(CodegenConfig codegenConfig) {
        this.codegenConfig = codegenConfig;
        return this;
    }

    public ResourceClassBuilder withResource(Resource resource) {
        this.resource = resource;
        return this;
    }

    public ResourceClassBuilder withReq(ReqSpecField req) {
        this.req = req;
        return this;
    }

    public void generate() {
        if (resource.getParentResource() != null && !resource.getParentResource().getUriParameters().isEmpty()) {
            Map<String, UriParameter> combined = new HashMap<>();
            combined.putAll(resource.getParentResource().getUriParameters());
            combined.putAll(resource.getUriParameters());
            resource.setUriParameters(combined);
        }

        resource.getResources().values().stream().forEach(generateResourseClasses);

        uri = new UriConst(resource.getUri());
        apiClass = ApiResourceClass.forResource(resource)
                .withField(uri)
                .withField(req)
                .withField(resp);
        responseParser = respParserForResource(resource);
        defaultsMethod = new DefaultsMethod(apiClass, req);

        new ResourseRule().apply(resource, this);
        javaFiles.stream().forEach(writeTo);
    }

    Consumer<Resource> generateResourseClasses = resource -> {
        new ResourceClassBuilder().withCodegenConfig(codegenConfig).withResource(resource).withReq(req).generate();
    };

    Consumer<JavaFile> writeTo = javaFile -> {
        try {
            LOG.info("Writing {}", javaFile.toJavaFileObject().getName());
            javaFile.writeTo(codegenConfig.getOutputPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    };

    BiConsumer<String, AbstractParam> applyParamRule = (name, param) -> {
        param.setDisplayName(name);
        new ParameterRule().apply(param, this);
    };

    BiConsumer<ActionType, Action> applyActionRule = (actionType, action) -> {
        action.setType(actionType);
        new ActionRule().apply(action, this);
    };

    BiConsumer<String, List<FormParameter>> applyFormParamsRule = (name, list) -> {
        list.forEach(param -> applyParamRule.accept(name, param));
    };

    Consumer<MimeType> applyBodyRule = mimeType -> {
        new BodyRule().apply(mimeType, this);
    };

    Consumer<Response> applyResponseRule = response -> {
        if (response.hasBody()) {
            response.getBody().values().forEach(mimeType -> {
                new ResponseRule().apply(mimeType, this);
            });
        }
    };

    public ApiResourceClass getApiClass() {
        return apiClass;
    }

    public DefaultsMethod getDefaultsMethod() {
        return defaultsMethod;
    }

    public UriConst getUri() {
        return uri;
    }

    public ResponseParserClass getResponseParser() {
        return responseParser;
    }

    public Resource getResource() {
        return resource;
    }

    public ArrayList<JavaFile> getJavaFiles() {
        return javaFiles;
    }

    public ReqSpecField getReq() {
        return req;
    }

    public RespSpecField getResp() {
        return resp;
    }

    public CodegenConfig getCodegenConfig() {
        return codegenConfig;
    }
}
