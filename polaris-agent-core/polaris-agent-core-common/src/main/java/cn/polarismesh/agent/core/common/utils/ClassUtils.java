package cn.polarismesh.agent.core.common.utils;

import java.util.Objects;

import cn.polarismesh.agent.core.common.logger.CommonLogger;
import cn.polarismesh.agent.core.common.logger.StdoutCommonLoggerFactory;

public class ClassUtils {

    private static final CommonLogger logger = StdoutCommonLoggerFactory.INSTANCE
            .getLogger(ClassUtils.class.getCanonicalName());

    public static String getPackageName(String fqcn, char packageSeparator, String defaultValue) {
        Objects.requireNonNull(fqcn, "fqcn");

        final int lastPackageSeparatorIndex = fqcn.lastIndexOf(packageSeparator);
        if (lastPackageSeparatorIndex == -1) {
            return defaultValue;
        }
        return fqcn.substring(0, lastPackageSeparatorIndex);
    }

    public static <T> Class<T> getClazz(String clazzName, ClassLoader classLoader) {
        if (null == classLoader) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        try {
            return (Class<T>) Class.forName(clazzName, false, classLoader);
        } catch (ClassNotFoundException e) {
            logger.warn(String.format("class %s not found, error %s", clazzName, e.getLocalizedMessage()));
        }
        return null;
    }

}
