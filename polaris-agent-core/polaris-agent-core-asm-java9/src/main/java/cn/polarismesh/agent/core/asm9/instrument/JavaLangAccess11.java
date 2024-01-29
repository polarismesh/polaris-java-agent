package cn.polarismesh.agent.core.asm9.instrument;

import java.security.ProtectionDomain;

public class JavaLangAccess11 implements JavaLangAccess {
    private static final jdk.internal.access.JavaLangAccess javaLangAccess = jdk.internal.access.SharedSecrets.getJavaLangAccess();

    @Override
    public Class<?> defineClass(ClassLoader cl, String name, byte[] b, ProtectionDomain pd, String source) {
        return javaLangAccess.defineClass(cl, name, b, pd, source);
    }

    @Override
    public void registerShutdownHook(int slot, boolean registerShutdownInProgress, Runnable hook) {
        javaLangAccess.registerShutdownHook(slot, registerShutdownInProgress, hook);
    }
}

