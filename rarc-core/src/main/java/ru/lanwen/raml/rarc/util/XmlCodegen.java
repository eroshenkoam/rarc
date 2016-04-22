package ru.lanwen.raml.rarc.util;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.MavenInvocationException;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by stassiak
 */
public class XmlCodegen {
    private static final String POM_PATH = "jaxb2/pom.xml";
    private final ResponseCodegenConfig config;

    public XmlCodegen(ResponseCodegenConfig config) {
        this.config = config;
    }

    public void generate() throws MavenInvocationException, IOException {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File(this.getClass().getClassLoader().getResource(POM_PATH).getFile()));
        request.setProperties(config.asJaxb2Properties());
        request.setGoals(Arrays.asList("clean", "compile"));

        new DefaultInvoker().execute(request);
    }
}
