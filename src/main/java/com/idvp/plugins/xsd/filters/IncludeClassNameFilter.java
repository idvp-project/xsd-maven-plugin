package com.idvp.plugins.xsd.filters;

import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * @author Oleg Zinoviev
 * @since 22.02.18.
 */
public class IncludeClassNameFilter implements Filter {
    private final Predicate<String> predicate;

    public IncludeClassNameFilter(String pattern) {
        this.predicate = Pattern.compile(pattern).asPredicate();
    }

    @Override
    public boolean isFitToFilter(Class<?> $class) {
        return predicate.test($class.getName());
    }
}
