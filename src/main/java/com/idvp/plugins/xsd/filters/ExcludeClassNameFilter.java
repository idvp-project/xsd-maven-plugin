package com.idvp.plugins.xsd.filters;

/**
 * @author Oleg Zinoviev
 * @since 22.02.18.
 */
public class ExcludeClassNameFilter extends IncludeClassNameFilter {
    public ExcludeClassNameFilter(String pattern) {
        super(pattern);
    }

    @Override
    public boolean isFitToFilter(Class<?> $class) {
        return !super.isFitToFilter($class);
    }
}
