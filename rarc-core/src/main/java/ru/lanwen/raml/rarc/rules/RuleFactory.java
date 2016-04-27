package ru.lanwen.raml.rarc.rules;

import org.raml.model.Action;
import org.raml.model.MimeType;
import org.raml.model.Resource;
import org.raml.model.parameter.*;
import ru.lanwen.raml.rarc.CodegenConfig;
import ru.lanwen.raml.rarc.api.ra.ReqSpecField;
import ru.lanwen.raml.rarc.api.ra.RespSpecField;

/**
 * Created by stassiak
 */
public class RuleFactory {
    private final CodegenConfig codegenConfig;
    private ReqSpecField req = new ReqSpecField();
    private RespSpecField resp = new RespSpecField();

    public RuleFactory(CodegenConfig codegenConfig) {
        this.codegenConfig = codegenConfig;
    }

    public CodegenConfig getCodegenConfig() {
        return codegenConfig;
    }

    public Rule<Resource> getResourseRule() {
        return new ResourseRule();
    }

    public Rule<AbstractParam> getParameterRule() {
        return new ParameterRule();
    }

    public Rule<Action> getActionRule() {
        return new ActionRule();
    }

    public Rule<Header> getHeaderRule() {
        return new HeaderRule();
    }

    public Rule<MimeType> getBodyRule() {
        return new BodyRule();
    }

    public Rule<MimeType> getResponseRule() {
        return new ResponseRule();
    }

    public Rule<QueryParameter> getQueryParamRule() {
        return new QueryParamRule();
    }

    public Rule<FormParameter> getFormParamRule() {
        return new FormParamRule();
    }

    public Rule<UriParameter> getUriParamRule() {
        return new UriParamRule();
    }

    public ReqSpecField getReq() {
        return req;
    }

    public RespSpecField getResp() {
        return resp;
    }
}
