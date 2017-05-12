package ru.lanwen.raml.rarc.api.ra;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import org.raml.model.parameter.AbstractParam;
import org.raml.model.parameter.FormParameter;
import org.raml.model.parameter.Header;
import org.raml.model.parameter.QueryParameter;
import org.raml.model.parameter.UriParameter;
import ru.lanwen.raml.rarc.api.ApiResourceClass;
import ru.lanwen.raml.rarc.api.Method;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.squareup.javapoet.CodeBlock.builder;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * @author lanwen (Merkushev Kirill)
 */
public class DefaultsMethod implements Method {

    private ApiResourceClass apiClass;
    private ReqSpecField req;
    private List<CodeBlock> defaults = new ArrayList<>();

    public DefaultsMethod(ApiResourceClass apiClass, ReqSpecField req) {
        this.apiClass = apiClass;
        this.req = req;
    }

    public DefaultsMethod forParamDefaults(String name, AbstractParam param) {
        if (isEmpty(param.getDefaultValue())) {
            return this;
        }

        CodeBlock.Builder builder = builder();
        switch (Param.byClass(param.getClass())) {
            case URI_PARAM:
                builder.addStatement("$N.addPathParam($S, $S)", req.name(), name, param.getDefaultValue());
                break;
            case QUERY:
                builder.addStatement("$N.addQueryParam($S, $S)", req.name(), name, param.getDefaultValue());
                break;
            case FORM:
                builder.addStatement("$N.addFormParam($S, $S)", req.name(), name, param.getDefaultValue());
                break;
            case HEADER:
                builder.addStatement("$N.addHeader($S, $S)", req.name(), name, param.getDefaultValue());
        }

        defaults.add(builder.build());
        return this;
    }

    @Override
    public MethodSpec methodSpec() {
        MethodSpec.Builder defaultsMethod = MethodSpec.methodBuilder("withDefaults")
                .returns(ClassName.bestGuess(apiClass.name()))
                .addModifiers(Modifier.PUBLIC);
        defaults.stream().distinct().forEach(defaultsMethod::addCode);
        defaultsMethod.addStatement("return this");
        return defaultsMethod.build();
    }

    public enum Param {
        URI_PARAM(UriParameter.class),
        QUERY(QueryParameter.class),
        FORM(FormParameter.class),
        HEADER(Header.class);

        private Class<? extends AbstractParam> clazz;

        Param(Class<? extends AbstractParam> clazz) {
            this.clazz = clazz;
        }

        public static Param byClass(Class<? extends AbstractParam> clazz) {
            return Stream.of(values()).filter(param -> param.clazz.equals(clazz)).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No param for class " + clazz.getName()));
        }
    }
}
