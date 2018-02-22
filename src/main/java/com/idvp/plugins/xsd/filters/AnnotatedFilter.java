package com.idvp.plugins.xsd.filters;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Oleg Zinoviev
 * @since 22.02.18.
 */
public class AnnotatedFilter implements Filter {

    @Override
    public boolean isFitToFilter(Class<?> $class) {

        return ($class.getAnnotation(XmlType.class) != null || $class.getAnnotation(XmlRootElement.class) != null)
                && $class.getAnnotation(XmlTransient.class) == null;
    }
}
