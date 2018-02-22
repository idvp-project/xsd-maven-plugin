package com.idvp.plugins.xsd;

import com.idvp.plugins.xsd.filters.AnnotatedFilter;
import com.idvp.plugins.xsd.filters.ExcludeClassNameFilter;
import com.idvp.plugins.xsd.filters.Filter;
import com.idvp.plugins.xsd.filters.IncludeClassNameFilter;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.scanners.AbstractScanner;
import org.reflections.scanners.Scanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Oleg Zinoviev
 * @since 22.02.18.
 */
@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
public class XsdSource {

    @Parameter(required = true)
    private String basePackage;

    @Parameter
    private List<String> exclude = new ArrayList<>();

    Set<Class<?>> getJaxbClasses(Log logger) {
        Scanner scanner = createScanner(logger);

        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        if (Thread.currentThread().getContextClassLoader() != null) {
            configurationBuilder = configurationBuilder
                    .addClassLoader(Thread.currentThread().getContextClassLoader());
        }

        configurationBuilder
                .addUrls(ClasspathHelper.forPackage(basePackage, configurationBuilder.getClassLoaders()))
                .filterInputsBy(new FilterBuilder()
                        .includePackage(basePackage))
                .addScanners(scanner);

        Reflections reflections = new Reflections(configurationBuilder);

        Collection<String> types = scanner.getStore().values();
        List<Class<?>> classes = ReflectionUtils.forNames(types, reflections.getConfiguration().getClassLoaders());
        return Collections.unmodifiableSet(new HashSet<>(classes));
    }

    private Scanner createScanner(Log logger) {
        List<Filter> filters = new ArrayList<>();
        filters.add(new IncludeClassNameFilter(Pattern.quote(basePackage) + ".*"));
        filters.add(new AnnotatedFilter());
        for (String exclusion : exclude) {
            filters.add(new ExcludeClassNameFilter(exclusion));
        }

        return new AbstractScanner() {
            @SuppressWarnings("unchecked")
            @Override
            public void scan(Object cls) {
                String className = this.getMetadataAdapter().getClassName(cls);
                String superclassName = this.getMetadataAdapter().getSuperclassName(cls);
                Class<?> $class = ReflectionUtils.forName(className, this.getConfiguration().getClassLoaders());

                for (Filter filter : filters) {
                    boolean result = filter.isFitToFilter($class);

                    if (logger.isDebugEnabled()) {
                        if (result) {
                            logger.debug(className + " is accepted by filter " + filter.getClass().getSimpleName());
                        } else {
                            logger.debug(className + " is rejected by filter " + filter.getClass().getSimpleName());
                        }
                    }

                    if (!result) {
                        return;
                    }
                }

                getStore().put(superclassName, className);
            }
        };

    }

    @Override
    public String toString() {
        return "XsdSource{" +
                "basePackage='" + basePackage + '\'' +
                ", exclude=" + exclude +
                '}';
    }


}
