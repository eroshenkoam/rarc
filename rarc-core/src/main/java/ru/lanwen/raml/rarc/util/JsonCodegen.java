package ru.lanwen.raml.rarc.util;

import com.jayway.restassured.path.json.JsonPath;
import com.sun.codemodel.JCodeModel;
import org.jsonschema2pojo.DefaultGenerationConfig;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.GsonAnnotator;
import org.jsonschema2pojo.SchemaGenerator;
import org.jsonschema2pojo.SchemaMapper;
import org.jsonschema2pojo.SchemaStore;
import org.jsonschema2pojo.SourceType;
import org.jsonschema2pojo.rules.RuleFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.capitalize;
import static ru.lanwen.raml.rarc.api.ApiResourceClass.sanitize;

/**
 * Created by stassiak
 */
public class JsonCodegen {
    private final ResponseCodegenConfig config;

    public JsonCodegen(ResponseCodegenConfig config) {
        this.config = config;
    }

    public String generate() throws IOException {
        JCodeModel codeModel = new JCodeModel();
        URL source = new File(config.getInputPath()).toURI().toURL();
        GenerationConfig generationConfig = new DefaultGenerationConfig() {
            @Override
            public boolean isGenerateBuilders() { // set config option by overriding metho
                return true;
            }

            @Override
            public SourceType getSourceType() {
                if (JsonPath.from(source).get("$schema") != null) {
                    return SourceType.JSONSCHEMA;
                }
                return SourceType.JSON;
            }

            @Override
            public boolean isUseLongIntegers() {
                return true;
            }

            @Override
            public boolean isUseCommonsLang3() {
                return true;
            }
        };

        SchemaMapper mapper = new SchemaMapper(
                new RuleFactory(generationConfig, new GsonAnnotator(), new SchemaStore()), new SchemaGenerator());
        mapper.generate(codeModel, getClassName(), config.getPackageName(), source);

        codeModel.build(new File(config.getOutputPath()));

        return getClassName();
    }

    private String getClassName() {
        Matcher matcher = Pattern.compile("\\/([\\w]+).json").matcher(config.getJsonSchemaPath());
        matcher.find();

        return capitalize(sanitize(matcher.group(1)));
    }
}
