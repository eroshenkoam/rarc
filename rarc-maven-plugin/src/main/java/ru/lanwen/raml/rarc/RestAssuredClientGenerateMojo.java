package ru.lanwen.raml.rarc;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.nio.file.Paths;

/**
 * @author lanwen (Merkushev Kirill)
 */
@Mojo(name = "generate-client", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
@Execute(goal = "generate-client")
public class RestAssuredClientGenerateMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.build.resources[0].directory}/api.raml")
    private String ramlFile;

    @Parameter(required = true)
    private String basePackage;

    @Parameter(defaultValue = "${project.build.directory}/generated-sources/raml")
    private String outputDir;

    @Parameter(required = true, readonly = true, defaultValue = "${project}")
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            new RestAssuredRamlCodegen(
                    CodegenConfig.codegenConf()
                            .withInputPath(Paths.get(ramlFile))
                            .withBasePackage(basePackage)
                            .withOutputPath(Paths.get(outputDir))
            ).generate();

            project.addCompileSourceRoot(outputDir);

        } catch (Exception e) {
            throw new MojoExecutionException("Exception while generating client.", e);
        }
    }
}
