package com.idvp.plugins.xsd.filters;

/**
 * @author Oleg Zinoviev
 * @since 22.02.18.
 */
public interface Filter {
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean isFitToFilter(Class<?> $class);
}
