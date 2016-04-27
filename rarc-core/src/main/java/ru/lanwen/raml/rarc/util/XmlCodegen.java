package ru.lanwen.raml.rarc.util;

import com.jayway.restassured.path.xml.XmlPath;
import org.apache.maven.shared.invoker.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

import static org.apache.commons.lang3.StringUtils.capitalize;
import static ru.lanwen.raml.rarc.api.ApiResourceClass.sanitize;

/**
 * Created by stassiak
 */
public class XmlCodegen {
    private final Logger LOG = LoggerFactory.getLogger(XmlCodegen.class);
    private static final String POM_PATH = "jaxb2/pom.xml";
    private final ResponseCodegenConfig config;

    public XmlCodegen(ResponseCodegenConfig config) {
        this.config = config;
    }

    public String generate() throws MavenInvocationException, IOException {
        InvocationRequest request = new DefaultInvocationRequest();

        Path pomPath = Files.createTempFile("pom", ".xml");
        Files.copy(getClass().getClassLoader().getResourceAsStream(POM_PATH),
                pomPath, StandardCopyOption.REPLACE_EXISTING);

        request.setPomFile(pomPath.toFile());
        request.setProperties(config.asJaxb2Properties());
        request.setGoals(Arrays.asList("clean", "generate-sources"));

        InvocationResult result = new DefaultInvoker().execute(request);

        if(result.getExitCode() != 0) {
            LOG.info("Xmlgen failed: {}", result.getExecutionException());
        }

        pomPath.toFile().delete();

        return capitalize(sanitize(XmlPath.from(new File(config.getInputPath()))
                .getNode("schema").getNode("element").getAttribute("name"))) + "Type";
    }
}
