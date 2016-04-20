package ru.lanwen.raml.rarc.rules;

import com.squareup.javapoet.JavaFile;
import org.raml.model.Action;
import org.raml.model.MimeType;
import org.raml.model.Resource;
import org.raml.model.parameter.AbstractParam;
import org.raml.model.parameter.Header;
import org.raml.model.parameter.QueryParameter;
import ru.lanwen.raml.rarc.CodegenConfig;
import ru.lanwen.raml.rarc.api.ApiResourceClass;
import ru.lanwen.raml.rarc.api.ra.DefaultsMethod;
import ru.lanwen.raml.rarc.api.ra.ReqSpecField;
import ru.lanwen.raml.rarc.api.ra.RespSpecField;
import ru.lanwen.raml.rarc.api.ra.UriConst;
import ru.lanwen.raml.rarc.util.ResponseParserClass;

import java.util.List;

import static ru.lanwen.raml.rarc.util.ResponseParserClass.respParserForResource;

/**
 * Created by stassiak
 */
public class RuleFactory {
    private CodegenConfig codegenConfig;
    private ReqSpecField req = new ReqSpecField();
    private RespSpecField resp = new RespSpecField();

    public RuleFactory(CodegenConfig codegenConfig) {
        this.codegenConfig = codegenConfig;
    }

    public CodegenConfig getCodegenConfig() {
        return codegenConfig;
    }

    public RamlRule getRamlRule() {
        return new RamlRule(this);
    }

    public Rule<Resource, List<JavaFile>> getResourseRule() {
        return new ResourseRule(this);
    }

    public Rule<AbstractParam, ResourceClassBuilder> getParameterRule() {
        return new ParameterRule(this);
    }

    public Rule<Action, ResourceClassBuilder> getActionRule() {
        return new ActionRule(this);
    }

    public Rule<Header, ResourceClassBuilder> getHeaderRule() {
        return new HeaderRule(this);
    }

    public Rule<MimeType, ResourceClassBuilder> getBodyRule() {
        return new BodyRule(this);
    }

    public Rule<MimeType, ResourceClassBuilder> getResponseRule() {
        return new ResponseRule(this);
    }

    public Rule<QueryParameter, ResourceClassBuilder> getQueryParamRule() {
        return new QueryParamRule(this);
    }

    public ReqSpecField getReq() {
        return req;
    }

    public RespSpecField getResp() {
        return resp;
    }

    public ResourceClassBuilder getResourceClass(Resource resource) {
        UriConst uri = new UriConst(resource.getUri());
        ApiResourceClass apiClass = ApiResourceClass.forResource(resource)
                .withField(uri)
                .withField(req)
                .withField(resp);
        ResponseParserClass parser = respParserForResource(resource);
        return new ResourceClassBuilder(apiClass, new DefaultsMethod(apiClass, req), uri, parser, resource);
    }

    public static class ResourceClassBuilder {
        private ApiResourceClass apiClass;
        private DefaultsMethod defaultsMethod;
        private UriConst uri;
        private ResponseParserClass responseParserClass;
        private Resource resource;

        private ResourceClassBuilder(ApiResourceClass apiClass, DefaultsMethod defaultsMethod, UriConst uri,
                                     ResponseParserClass responseParserClass, Resource resource) {
            this.apiClass = apiClass;
            this.defaultsMethod = defaultsMethod;
            this.uri = uri;
            this.responseParserClass = responseParserClass;
            this.resource = resource;
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

        public ResponseParserClass getResponseParserClass() {
            return responseParserClass;
        }

        public Resource getResource() {
            return resource;
        }
    }
}
