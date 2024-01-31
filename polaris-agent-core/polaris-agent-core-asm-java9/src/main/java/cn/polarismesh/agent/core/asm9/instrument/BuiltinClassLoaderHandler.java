package cn.polarismesh.agent.core.asm9.instrument;

import cn.polarismesh.agent.core.asm.instrument.classloading.PluginClassInjector;
import cn.polarismesh.agent.core.asm.instrument.plugin.PluginConfig;
import cn.polarismesh.agent.core.asm9.module.impl.DefaultModuleSupport;
import cn.polarismesh.agent.core.asm9.starter.ModuleSupportHolder;
import cn.polarismesh.agent.core.common.exception.PolarisAgentException;
import cn.polarismesh.agent.core.common.logger.CommonLogger;
import cn.polarismesh.agent.core.common.logger.StdoutCommonLoggerFactory;
import jdk.internal.loader.BuiltinClassLoader;
import jdk.internal.loader.URLClassPath;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class BuiltinClassLoaderHandler implements PluginClassInjector {

    private static final CommonLogger logger = StdoutCommonLoggerFactory.INSTANCE
            .getLogger(BuiltinClassLoaderHandler.class.getCanonicalName());

    private static Method APPEND_CLASS_PATH;

    private static Field UCP_OBJECT_FIELD;

    private static Method ADD_URL;

    private final AtomicBoolean urlAdded = new AtomicBoolean(false);

    private final Object lock = new Object();

    static {
        try {
            // for java17 supported
            APPEND_CLASS_PATH = BuiltinClassLoader.class.getDeclaredMethod("appendClassPath", String.class);
            APPEND_CLASS_PATH.setAccessible(true);
            UCP_OBJECT_FIELD = null;
            ADD_URL = null;
        } catch (NoSuchMethodException ne) {
            APPEND_CLASS_PATH = null;
            try {
                UCP_OBJECT_FIELD = BuiltinClassLoader.class.getDeclaredField("ucp");
                UCP_OBJECT_FIELD.setAccessible(true);
                ADD_URL = URLClassPath.class.getDeclaredMethod("addURL", URL.class);
            } catch (Exception e1) {
                throw new IllegalStateException("Cannot access BuiltinClassLoader.ucp", e1);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Cannot access BuiltinClassLoader.appendClassPath(String)", e);
        }
    }

    private final PluginConfig pluginConfig;

    public BuiltinClassLoaderHandler(PluginConfig pluginConfig) {
        this.pluginConfig = Objects.requireNonNull(pluginConfig, "pluginConfig");
    }


    @Override
    public <T> Class<? extends T> injectClass(ClassLoader classLoader, String className) {
        try {
                if (classLoader instanceof BuiltinClassLoader) {
                    BuiltinClassLoader builtinClassLoader = (BuiltinClassLoader) classLoader;
                    addPluginURLIfAbsent(builtinClassLoader);
                    return (Class<T>) builtinClassLoader.loadClass(className);
                }
        } catch (Exception e) {
            logger.warn(String.format("failed to load plugin class %s with classLoader %s", className, classLoader), e);
            throw new PolarisAgentException(
                    "Failed to load plugin class " + className + " with classLoader " + classLoader,
                    e);
        }
        throw new PolarisAgentException("invalid ClassLoader");
    }

    @Override
    public InputStream getResourceAsStream(ClassLoader targetClassLoader, String internalName) {
        try {
            if (targetClassLoader instanceof BuiltinClassLoader) {
                final BuiltinClassLoader builtinClassLoader = (BuiltinClassLoader) targetClassLoader;
                addPluginURLIfAbsent(builtinClassLoader);
                return targetClassLoader.getResourceAsStream(internalName);
            }
        } catch (Exception e) {
            logger.warn(String.format("failed to load plugin resource as stream %s with classLoader %s", internalName,
                    targetClassLoader), e);
            return null;
        }
        return null;
    }

    private void addPluginURLIfAbsent(BuiltinClassLoader classLoader)
            throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        synchronized (lock) {
            if (!urlAdded.get()) {
                urlAdded.set(true);
                if (null != APPEND_CLASS_PATH) {
                    APPEND_CLASS_PATH.invoke(classLoader, pluginConfig.getPluginUrl().getFile());
                } else {
                    Object ucp = UCP_OBJECT_FIELD.get(classLoader);
                    ADD_URL.invoke(ucp, pluginConfig.getPluginUrl());
                }
                // to support cn.polarismesh.agent.core.common.utils.ReflectionUtils.setSuperValueByFieldName usage
                if (!pluginConfig.getPlugin().getOpenModules().isEmpty()) {
                    DefaultModuleSupport moduleSupport = ModuleSupportHolder.getInstance().getModuleSupport(null);
                    moduleSupport.baseModuleAddOpens(pluginConfig.getPlugin().getOpenModules(), moduleSupport.wrapJavaModule(classLoader.getUnnamedModule()));
                }
            }
        }
    }

    @Override
    public PluginConfig getPluginConfig() {
        return pluginConfig;
    }

    @Override
    public boolean match(ClassLoader classLoader) {
        return classLoader instanceof BuiltinClassLoader;
    }
}
