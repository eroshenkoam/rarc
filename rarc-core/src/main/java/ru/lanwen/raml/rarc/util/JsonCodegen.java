package ru.lanwen.raml.rarc.util;

import com.sun.codemodel.JCodeModel;
import io.restassured.path.json.JsonPath;
import org.jsonschema2pojo.*;
import org.jsonschema2pojo.rules.RuleFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.capitalize;
import static ru.lanwen.raml.rarc.api.ApiResourceClass.sanitize;

/**
 * Created by stassiak
 */
public class JsonCodegen {
    private final Logger LOG = LoggerFactory.getLogger(JsonCodegen.class);

    private final ResponseCodegenConfig config;
    private static Map<String, String> schemas = new HashMap<>();

    public JsonCodegen(ResponseCodegenConfig config) {
        this.config = config;
    }

    /**
     * Generates classes based on json/jsonschema.
     *
     * @return class name
     * @throws IOException
     */
    public String generate() throws IOException {
        String schemaPath = this.config.getJsonSchemaPath();
        if (schemas.containsKey(schemaPath)){
            LOG.info("Schema already exists " + schemaPath);
            return schemas.get(schemaPath);
        }

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

        schemas.put(schemaPath, getClassName());
        return getClassName();
    }

    private String getClassName() {
        Matcher matcher = Pattern.compile("\\/([\\w]+).json").matcher(config.getJsonSchemaPath());
        matcher.find();

        return capitalize(sanitize(matcher.group(1)));
    }
}
