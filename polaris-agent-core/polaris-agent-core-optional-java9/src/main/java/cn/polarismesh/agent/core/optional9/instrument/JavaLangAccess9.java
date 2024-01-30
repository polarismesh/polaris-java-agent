package cn.polarismesh.agent.core.optional9.instrument;

import java.security.ProtectionDomain;
import cn.polarismesh.agent.core.asm.instrument.JavaLangAccess;

public class JavaLangAccess9 implements JavaLangAccess {

    private static final jdk.internal.misc.JavaLangAccess javaLangAccess = jdk.internal.misc.SharedSecrets.getJavaLangAccess();

    @Override
    public Class<?> defineClass(ClassLoader cl, String name, byte[] b, ProtectionDomain pd, String source) {
        return javaLangAccess.defineClass(cl, name, b, pd, source);
    }

    @Override
    public void registerShutdownHook(int slot, boolean registerShutdownInProgress, Runnable hook) {
        javaLangAccess.registerShutdownHook(slot, registerShutdownInProgress, hook);
    }

}
