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
        return new ResourseRule(this);
    }

    public Rule<AbstractParam> getParameterRule() {
        return new ParameterRule(this);
    }

    public Rule<Action> getActionRule() {
        return new ActionRule(this);
    }

    public Rule<Header> getHeaderRule() {
        return new HeaderRule(this);
    }

    public Rule<MimeType> getBodyRule() {
        return new BodyRule(this);
    }

    public Rule<MimeType> getResponseRule() {
        return new ResponseRule(this);
    }

    public Rule<QueryParameter> getQueryParamRule() {
        return new QueryParamRule(this);
    }

    public Rule<FormParameter> getFormParamRule() {
        return new FormParamRule(this);
    }

    public Rule<UriParameter> getUriParamRule() {
        return new UriParamRule(this);
    }

    public ReqSpecField getReq() {
        return req;
    }

    public RespSpecField getResp() {
        return resp;
    }
}
