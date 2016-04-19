package ru.lanwen.raml.rarc.rules;

import com.squareup.javapoet.JavaFile;
import org.raml.model.Raml;
import org.raml.model.Resource;
import ru.lanwen.raml.rarc.CodegenConfig;
import ru.lanwen.raml.rarc.api.ra.ReqSpecField;
import ru.lanwen.raml.rarc.api.ra.RespSpecField;

import java.util.List;

/**
 * Created by stassiak
 */
public class RuleFactory {
    private CodegenConfig codegenConfig;
    ReqSpecField req = new ReqSpecField();
    RespSpecField resp = new RespSpecField();

    public RuleFactory(CodegenConfig codegenConfig) {
        this.codegenConfig = codegenConfig;
    }

    public CodegenConfig getCodegenConfig() {
        return codegenConfig;
    }

    public Rule<Raml, List<JavaFile>> getRamlRule() {
        return new RamlRule(this);
    }

    public Rule<Resource, JavaFile> getResourseRule() {
        return new ResourseRule();
    }

    public ReqSpecField getReq() {
        return req;
    }

    public RespSpecField getResp() {
        return resp;
    }
}
