package ru.lanwen.raml.rarc.rules;

import com.squareup.javapoet.JavaFile;
import org.raml.model.MimeType;
import org.raml.model.Resource;
import org.raml.model.Response;
import org.raml.model.parameter.FormParameter;
import org.raml.model.parameter.Header;
import org.raml.model.parameter.QueryParameter;
import org.raml.model.parameter.UriParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lanwen.raml.rarc.api.ApiResourceClass;
import ru.lanwen.raml.rarc.api.ra.DefaultsMethod;
import ru.lanwen.raml.rarc.api.ra.UriConst;
import ru.lanwen.raml.rarc.util.ResponseParserClass;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import static ru.lanwen.raml.rarc.util.ResponseParserClass.respParserForResource;

/**
 * Created by stassiak
 */
public class ResourceClassBuilder {
    private final Logger LOG = LoggerFactory.getLogger(ResourceClassBuilder.class);
    private RuleFactory ruleFactory;
    private ApiResourceClass apiClass;
    private DefaultsMethod defaultsMethod;
    private UriConst uri;
    private ResponseParserClass responseParser;
    private Resource resource;
    private ArrayList<JavaFile> javaFiles = new ArrayList<>();

    public ResourceClassBuilder withRuleFactory(RuleFactory ruleFactory) {
        this.ruleFactory = ruleFactory;
        return this;
    }

    public ResourceClassBuilder withResource(Resource resource) {
        this.resource = resource;
        return this;
    }

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

    public void generate() {
        resource.getResources().values().stream().forEach(generateResourseClasses);

        if (resource.getParentResource() != null && !resource.getParentResource().getUriParameters().isEmpty()) {
            Map<String, UriParameter> combined = new HashMap<>();
            combined.putAll(resource.getParentResource().getUriParameters());
            combined.putAll(resource.getUriParameters());
            resource.setUriParameters(combined);
        }

        uri = new UriConst(resource.getUri());
        apiClass = ApiResourceClass.forResource(resource)
                .withField(uri)
                .withField(ruleFactory.getReq())
                .withField(ruleFactory.getResp());
        responseParser = respParserForResource(resource);
        defaultsMethod = new DefaultsMethod(apiClass, ruleFactory.getReq());

        ruleFactory.getResourseRule().apply(resource, this);
        javaFiles.stream().forEach(writeTo);
    }

    Consumer<Resource> generateResourseClasses = resource -> {
        new ResourceClassBuilder().withRuleFactory(ruleFactory).withResource(resource).generate();
    };

    Consumer<JavaFile> writeTo = javaFile -> {
        try {
            LOG.info("Writing " + javaFile.toJavaFileObject().getName());
            javaFile.writeTo(ruleFactory.getCodegenConfig().getOutputPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    };

    Consumer<Entry<String, QueryParameter>> applyQueryParamRule = entry -> {
        entry.getValue().setDisplayName(entry.getKey());
        ruleFactory.getParameterRule().apply(entry.getValue(), this);
    };
    Consumer<Entry<String, Header>> applyHeaderRule = entry -> {
        entry.getValue().setDisplayName(entry.getKey());
        ruleFactory.getHeaderRule().apply(entry.getValue(), this);
    };

    Consumer<Entry<String, List<FormParameter>>> applyFormParamsRule = entry -> {
        entry.getValue().forEach(formParameter -> {
            formParameter.setDisplayName(entry.getKey());
            ruleFactory.getParameterRule().apply(formParameter, this);
        });
    };

    Consumer<MimeType> applyBodyRule = mimeType -> {
        ruleFactory.getBodyRule().apply(mimeType, this);
    };

    Consumer<Response> applyResponseRule = response -> {
        if(response.hasBody()){
            response.getBody().values().forEach(mimeType -> {
                ruleFactory.getResponseRule().apply(mimeType, this);
            });
        }
    };
}
