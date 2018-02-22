package com.idvp.plugins.xsd;

import org.apache.maven.plugins.annotations.Parameter;

/**
 * @author Oleg Zinoviev
 * @since 22.02.18.
 */
@SuppressWarnings("WeakerAccess")
public class XsdSchema {
    @Parameter(required = true)
    private String namespace;

    @Parameter(required = true)
    private String fileName;

    public String getFileName() {
        return fileName;
    }

    public String getNamespace() {
        return namespace;
    }
}
