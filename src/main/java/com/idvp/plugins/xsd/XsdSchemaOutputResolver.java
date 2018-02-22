package com.idvp.plugins.xsd;

import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Oleg Zinoviev
 * @since 22.02.18.
 */
class XsdSchemaOutputResolver extends SchemaOutputResolver {
    private final Map<String, String> map;
    private XsdMojo xsdMojo;
    private final Path path;

    XsdSchemaOutputResolver(XsdMojo xsdMojo, String targetPath) {
        this.xsdMojo = xsdMojo;
        this.map = xsdMojo.getSchemas()
                .stream()
                .collect(Collectors.toMap(XsdSchema::getNamespace, XsdSchema::getFileName));
        path = Paths.get(targetPath);
    }

    private String getFileName(String namespace, String defaultFile) {
        return map.getOrDefault(namespace, defaultFile);
    }

    @Override
    public Result createOutput(String namespace, String file) throws IOException {

        String fileName = getFileName(namespace, file);
        Path targetPath = path.resolve(fileName);

        Files.createDirectories(targetPath.getParent());

        OutputStream outputStream = Files.newOutputStream(targetPath,
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);

        StreamResult streamResult = new StreamResult(new OutputStreamWriter(outputStream, xsdMojo.getEncoding()));
        streamResult.setSystemId(targetPath.toUri().toASCIIString());
        return streamResult;
    }
}
