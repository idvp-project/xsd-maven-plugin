package com.idvp.plugins.xsd;

import org.junit.Assert;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;

/**
 * @author Oleg Zinoviev
 * @since 05.03.18.
 */
public class XsdTest {
    @Test
    public void test1() throws JAXBException, IOException {
        JAXBContext context = JAXBContext.newInstance(ObjectA.class);
        StringWriter result = new StringWriter();
        context.generateSchema(new SchemaOutputResolver() {
            @Override
            public Result createOutput(String namespaceUri, String suggestedFileName) {
                StreamResult result1 = new StreamResult();
                result1.setSystemId(suggestedFileName);
                result1.setWriter(result);
                return result1;
            }
        });

        Assert.assertTrue(result.toString().contains("<xs:"));
    }
}
