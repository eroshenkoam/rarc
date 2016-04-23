package ru.lanwen.raml.rarc.util;

import com.jayway.restassured.path.xml.XmlPath;
import org.apache.maven.shared.invoker.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
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
        File pomFile = getPomFile();

        request.setPomFile(pomFile);
        request.setProperties(config.asJaxb2Properties());
        request.setGoals(Arrays.asList("clean", "generate-sources"));

        InvocationResult result = new DefaultInvoker().execute(request);

        if(result.getExitCode() != 0) {
            LOG.info("Xmlgen failed: {}", result.getExecutionException());
        }

        pomFile.delete();

        return capitalize(sanitize(XmlPath.from(new File(config.getInputPath()))
                .getNode("schema").getNode("element").getAttribute("name"))) + "Type";
    }

    File getPomFile() throws IOException {
        File file = File.createTempFile("pom", ".xml");
        try {
            InputStream input = getClass().getClassLoader().getResourceAsStream(POM_PATH);
            OutputStream out = new FileOutputStream(file);
            int read;
            byte[] bytes = new byte[1024];

            while ((read = input.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            file.deleteOnExit();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return file;
    }
}
