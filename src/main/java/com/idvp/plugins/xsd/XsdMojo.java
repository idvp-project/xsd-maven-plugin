package com.idvp.plugins.xsd;

import com.sun.xml.bind.v2.schemagen.xmlschema.Schema;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final Logger logger = LoggerFactory.getLogger(XsdMojo.class);

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

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (sources == null || sources.isEmpty()) {
            throw new MojoFailureException("You must configure at least one source element");
        }

        if (skipXsdGeneration) {
            logger.info("Xsd generation is skipped.");
            return;
        }

        bootstrapJava9ClassLoading();

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

            Set<Class<?>> jaxbClasses = source.getJaxbClasses();

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

    /**
     * note: При сборке проекта classloader'ом выступает org.codehaus.plexus.classworlds.realm.ClassRealm
     * note: ClassRealm::loadClass переопределен таким образом, что делегирует загрузку классов "стратегии"
     * note: ClassRealm::findClass переопределен криво, поэтому возникает проблема с получением стандартного namespace
     *
     * Details:
     *
     * При работе TXW::create пытается получить информацию о пакете com.sun.xml.bind.v2.schemagen.xmlschema
     * При этом вызывается метод Package::getAnnotation.
     * Этот метод в свою очередь в java 9 вызывает не старный метод ClassLoader::loadClass("name"), а новый ClassLoader::loadClass(module, "name")
     * Метод ClassLoader::loadClass(module, "name") является финальным и не переопределяется в ClassRealm.
     *
     * В свою очердь ClassLoader::loadClass(module, "name") сначала пытается найти package-info среди загруженных классов (ClassLoader::findLoadedClass("name")),
     * а в случае неудачи вызывает ClassLoader::findClass("name")
     * Последний метод в ClassRealm всегда просто кидает исключение.
     * В итоге TXW::create не может получить данные о XmlNamespace аннотации при работе с java 9
     *
     * Fix:
     *
     * Пытаемся в лоб загрузить информацию о пакете
     */
    private void bootstrapJava9ClassLoading() {
        try {
            Class.forName(Schema.class.getPackage().getName() + ".package-info");
        } catch (ClassNotFoundException e) {
            logger.error("Cannot load package-info");
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
