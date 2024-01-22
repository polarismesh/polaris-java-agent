package cn.polarismesh.agent.plugin.nacos.asm;


import cn.polarismesh.agent.plugin.nacos.constants.NacosConstants;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AsmDynamicClassLoader extends ClassLoader {
    private LoadedClass loadedClass = LoadedClass.getInstance();
    private static Map<String, AsmDynamicClassGenerator> generatorMap = new ConcurrentHashMap<>();

    static {
        generatorMap.put(NacosConstants.NAMING_PROXY, new NamingProxyGenerator());
        generatorMap.put(NacosConstants.NAMING_CLIENT_PROXY, new NamingClientProxyGenerator());
    }

    public AsmDynamicClassLoader(ClassLoader parent) {
        super(parent);
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        if (!generatorMap.containsKey(name)) {
            throw new ClassNotFoundException();
        }
        Class<?> clazz = loadedClass.findClass(name);
        if (clazz != null) {
            return clazz;
        }
        synchronized (AsmDynamicClassLoader.class) {
            byte[] codeBytes = generatorMap.get(name).generate();
            clazz = defineClass(name, codeBytes, 0, codeBytes.length);
            loadedClass.add(name, clazz);
        }
        return clazz;
    }

    private static class LoadedClass {

        private volatile static LoadedClass single;
        private final ConcurrentHashMap<String, Class<?>> loadedClassMap = new ConcurrentHashMap<>();

        private LoadedClass() {
        }

        private static LoadedClass getInstance() {
            if (null == single) {
                synchronized (LoadedClass.class) {
                    if (null == single) {
                        single = new LoadedClass();
                    }
                }
            }
            return single;
        }

        private Class<?> findClass(String name) {
            return loadedClassMap.get(name);
        }


        private void add(String name, Class<?> clazz) {
            loadedClassMap.putIfAbsent(name, clazz);
        }
    }
}
