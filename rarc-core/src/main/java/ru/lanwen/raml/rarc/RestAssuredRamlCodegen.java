package ru.lanwen.raml.rarc;

import org.raml.model.Raml;
import org.raml.model.Resource;
import org.raml.model.parameter.UriParameter;
import org.raml.parser.loader.FileResourceLoader;
import org.raml.parser.visitor.RamlDocumentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lanwen.raml.rarc.api.ra.root.NestedConfigClass;
import ru.lanwen.raml.rarc.api.ra.root.ReqSpecSupplField;
import ru.lanwen.raml.rarc.api.ra.root.RootApiClase;
import ru.lanwen.raml.rarc.rules.ResourceClassBuilder;
import ru.lanwen.raml.rarc.rules.RuleFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;

/**
 * @author lanwen (Merkushev Kirill)
 */
public class RestAssuredRamlCodegen {
    private final Logger LOG = LoggerFactory.getLogger(RestAssuredRamlCodegen.class);

    private RuleFactory ruleFactory;

    public RestAssuredRamlCodegen(CodegenConfig config) {
        this.ruleFactory = new RuleFactory(config);
    }

    public void generate() throws IOException {
        Path path = ruleFactory.getCodegenConfig().getInputPath();
        LOG.info("RAML: {}", path.toAbsolutePath());
        if (path.endsWith(".raml")) {
            LOG.warn("Wrong path - should end with .raml");
            return;
        }

        Raml raml = new RamlDocumentBuilder(new FileResourceLoader(path.getParent().toFile()))
                .build(path.getFileName().toString());

        ReqSpecSupplField baseReqSpec = new ReqSpecSupplField();
        NestedConfigClass nestedConfigClass = new NestedConfigClass(raml.getTitle(), baseReqSpec, ruleFactory.getReq());

        new RootApiClase(nestedConfigClass).javaFile(raml, ruleFactory.getCodegenConfig().getBasePackage())
                .writeTo(ruleFactory.getCodegenConfig().getOutputPath());

        raml.getResources().values().stream()
                .flatMap(res -> fromResource(res).stream()).collect(toList()).stream()
                .forEach(generateResourseClasses);
    }

    Consumer<Resource> generateResourseClasses = resource -> {
        new ResourceClassBuilder().withRuleFactory(ruleFactory).withResource(resource).generate();
    };

    private Collection<Resource> fromResource(Resource resource) {
        // in case of /account/{uid}/options/
        if (resource.getParentResource() != null && !resource.getParentResource().getUriParameters().isEmpty()) {
            Map<String, UriParameter> combined = new HashMap<>();
            combined.putAll(resource.getParentResource().getUriParameters());
            combined.putAll(resource.getUriParameters());
            resource.setUriParameters(combined);
        }
        if (resource.getResources().isEmpty()) {
            return Collections.singleton(resource);
        } else {
            List<Resource> all = new ArrayList<>();
            all.add(resource);
            for (Resource next : resource.getResources().values()) {
                all.addAll(fromResource(next));
            }
            return all;
        }
    }
}
