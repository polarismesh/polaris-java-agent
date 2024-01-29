package cn.polarismesh.agent.core.asm9.instrument;

import cn.polarismesh.agent.core.asm.instrument.classloading.PluginClassInjector;
import cn.polarismesh.agent.core.asm.instrument.plugin.PluginConfig;
import cn.polarismesh.agent.core.asm9.module.impl.DefaultModuleSupport;
import cn.polarismesh.agent.core.asm9.starter.ModuleSupportHolder;
import cn.polarismesh.agent.core.common.exception.PolarisAgentException;
import cn.polarismesh.agent.core.common.logger.CommonLogger;
import cn.polarismesh.agent.core.common.logger.StdoutCommonLoggerFactory;
import jdk.internal.loader.BuiltinClassLoader;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class BuiltinClassLoaderHandler implements PluginClassInjector {

    private static final CommonLogger logger = StdoutCommonLoggerFactory.INSTANCE
            .getLogger(BuiltinClassLoaderHandler.class.getCanonicalName());

    private static final Method ADD_URL;

    private final AtomicBoolean urlAdded = new AtomicBoolean(false);

    private final Object lock = new Object();

    static {
        try {
            ADD_URL = BuiltinClassLoader.class.getDeclaredMethod("appendClassPath", String.class);
            ADD_URL.setAccessible(true);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot access URLClassLoader.addURL(URL)", e);
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
                ADD_URL.invoke(classLoader, pluginConfig.getPluginUrl().getFile());
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
