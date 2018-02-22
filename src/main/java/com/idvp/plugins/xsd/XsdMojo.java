package com.idvp.plugins.xsd;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.SchemaOutputResolver;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Oleg Zinoviev
 * @since 22.02.18.
 */
@Mojo(name = "generate",
        defaultPhase = LifecyclePhase.PROCESS_CLASSES,
        configurator = "include-project-dependencies",
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME
)
public class XsdMojo extends AbstractMojo {

    private final Log logger;

    @Parameter( defaultValue = "${project}", readonly = true )
    private MavenProject project;

    @Parameter(property = "class2xsd.skip", defaultValue = "false")
    private boolean skipXsdGeneration;

    @Parameter(defaultValue = "${project.build.sourceEncoding}")
    private String encoding;

    @Parameter
    private String outputPath;

    @Parameter(required = true)
    private List<XsdSource> sources;

    @Parameter
    private List<XsdSchema> schemas;

    public XsdMojo() {
        this.logger = getLog();
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (sources == null || sources.isEmpty()) {
            throw new MojoFailureException("You must configure at least one source element");
        }

        if (skipXsdGeneration) {
            logger.info("Xsd generation is skipped.");
            return;
        }


        String targetPath = outputPath;
        if (targetPath == null || targetPath.isEmpty()) {
            targetPath = Paths.get(project.getModel().getBuild().getDirectory(), "generated-resources", "class2xsd")
                    .toAbsolutePath()
                    .toString();
        } else {
            targetPath = project.getBasedir().toPath()
                    .resolve(targetPath)
                    .toString();
        }

        logger.debug("output directory: " + targetPath);
        for (XsdSource source : sources) {
            logger.info("Started: " + source.toString());

            Set<Class<?>> jaxbClasses = source.getJaxbClasses(logger);

            if (logger.isDebugEnabled()) {
                for (Class<?> $class : jaxbClasses) {
                    logger.debug("JAXB Class candidate: " + $class.getName());
                }
            }

            JAXBContext context;
            try {
                context = JAXBContext.newInstance(jaxbClasses.toArray(new Class[0]));
            } catch (JAXBException e) {
                logger.error("Cannot create jaxb context", e);
                if (e.getLinkedException() != null) {
                    logger.error("Cannot create jaxb context. Linked", e.getLinkedException());
                }
                throw new MojoExecutionException("Cannot create jaxb context", e);
            }


            try {
                context.generateSchema(createResolver(targetPath));
            } catch (IOException e) {
                logger.error("Cannot generate schema", e);
                throw new MojoExecutionException("Cannot generate schema", e);
            }

            logger.info("Finished: " + source.toString());
        }
    }

    private SchemaOutputResolver createResolver(String targetPath) {
        return new XsdSchemaOutputResolver(this, targetPath);
    }

    List<XsdSchema> getSchemas() {
        return schemas == null ? Collections.emptyList() : schemas;
    }

    String getEncoding() {
        return encoding == null ? StandardCharsets.UTF_8.name() : encoding;
    }
}
