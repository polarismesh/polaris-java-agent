package cn.polarismesh.agent.core.asm.instrument;

import java.security.ProtectionDomain;

public interface JavaLangAccess {
    /**
     * Defines a class with the given name to a class loader.
     */
    Class<?> defineClass(ClassLoader cl, String name, byte[] b, ProtectionDomain pd, String source);

    void registerShutdownHook(int slot, boolean registerShutdownInProgress, Runnable hook);
}
