package cn.polarismesh.agent.core.common.utils;

import java.util.Objects;

public class ClassUtils {

    public static String getPackageName(String fqcn, char packageSeparator, String defaultValue) {
        Objects.requireNonNull(fqcn, "fqcn");

        final int lastPackageSeparatorIndex = fqcn.lastIndexOf(packageSeparator);
        if (lastPackageSeparatorIndex == -1) {
            return defaultValue;
        }
        return fqcn.substring(0, lastPackageSeparatorIndex);
    }

}
