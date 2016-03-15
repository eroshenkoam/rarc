package ru.lanwen.raml.rarc.util;

import com.sun.codemodel.JCodeModel;
import org.jsonschema2pojo.*;
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
    private final JsonCodegenConfig config;

    public JsonCodegen(JsonCodegenConfig config) {
        this.config = config;
    }

    public String generate() throws IOException {
        JCodeModel codeModel = new JCodeModel();
        URL source = new URL("file://" + config.getInputPath());
        GenerationConfig generationConfig = new DefaultGenerationConfig() {
            @Override
            public boolean isGenerateBuilders() { // set config option by overriding metho
                return true;
            }

            @Override
            public SourceType getSourceType() {
                return SourceType.JSON;
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
