package ru.lanwen.raml.rarc.rules;

import com.squareup.javapoet.JavaFile;
import org.raml.model.Raml;
import org.raml.model.Resource;
import org.raml.model.parameter.UriParameter;
import ru.lanwen.raml.rarc.api.ra.root.NestedConfigClass;
import ru.lanwen.raml.rarc.api.ra.root.ReqSpecSupplField;
import ru.lanwen.raml.rarc.api.ra.root.RootApiClase;

import java.io.IOException;
import java.util.*;

import static java.util.stream.Collectors.toList;

/**
 * Created by stassiak
 */
public class RamlRule {
    RuleFactory ruleFactory;

    public RamlRule(RuleFactory ruleFactory) {
        this.ruleFactory = ruleFactory;
    }

    public void apply(Raml raml) throws IOException {
        ReqSpecSupplField baseReqSpec = new ReqSpecSupplField();
        NestedConfigClass nestedConfigClass = new NestedConfigClass(raml.getTitle(), baseReqSpec, ruleFactory.getReq());

        new RootApiClase(nestedConfigClass).javaFile(raml, ruleFactory.getCodegenConfig().getBasePackage())
                .writeTo(ruleFactory.getCodegenConfig().getOutputPath());

        ArrayList<JavaFile> javaFiles = new ArrayList<>();
        List<Resource> resources = raml.getResources().values().stream()
                .flatMap(res -> fromResource(res).stream()).collect(toList());
        resources.forEach(resource -> {
            ruleFactory.getResourseRule().apply(resource, javaFiles);
        });

        javaFiles.forEach(file -> {
            try {
                file.writeTo(ruleFactory.getCodegenConfig().getOutputPath());
            } catch (IOException e) {
                throw new RuntimeException("Can't write to " + ruleFactory.getCodegenConfig().getOutputPath(), e);
            }
        });
    }

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
