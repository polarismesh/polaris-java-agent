package cn.polarismesh.agent.core.asm9.starter;

import cn.polarismesh.agent.core.asm9.module.impl.DefaultModuleSupport;
import cn.polarismesh.agent.core.asm9.module.JavaModuleFactory;
import cn.polarismesh.agent.core.asm9.module.impl.Java9ModuleFactory;
import cn.polarismesh.agent.core.asm9.transform.ClassFileTransformModuleAdaptor;
import cn.polarismesh.agent.core.common.starter.InstrumentationStarter;
import cn.polarismesh.agent.core.common.starter.ModuleSupport;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;

public class Java9InstrumentationStarter implements InstrumentationStarter {

    @Override
    public ClassFileTransformer wrapTransformer(Instrumentation instrumentation, ClassFileTransformer classFileTransformer) {
        JavaModuleFactory javaModuleFactory = new Java9ModuleFactory(instrumentation);
        ClassFileTransformer java9Transformer = new ClassFileTransformModuleAdaptor(instrumentation, classFileTransformer, javaModuleFactory);
        return java9Transformer;
    }

    public ModuleSupport createModuleSupport(Instrumentation instrumentation) {
        return new DefaultModuleSupport(instrumentation);
    }

}
