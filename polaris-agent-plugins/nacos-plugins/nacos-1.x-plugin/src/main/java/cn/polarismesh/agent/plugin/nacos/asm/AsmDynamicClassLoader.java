package cn.polarismesh.agent.plugin.nacos.asm;


import java.util.concurrent.atomic.AtomicBoolean;

public class AsmDynamicClassLoader extends ClassLoader {

    private AtomicBoolean loaded = new AtomicBoolean(false);
    private Class<?> clazz;
    public AsmDynamicClassLoader(ClassLoader parent) {
        super(parent);
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        if (!loaded.get()){
            byte[] codeBytes = ProxyDump.createDynamicNamingProxyClass();
            clazz = defineClass(name,codeBytes , 0, codeBytes.length);
            loaded.set(true);
        }
        return clazz;
    }
}
