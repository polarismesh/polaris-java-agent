/*
 * Tencent is pleased to support the open source community by making polaris-java-agent available.
 *
 * Copyright (C) 2021 Tencent. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

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
        return ModuleSupportHolder.getInstance().getModuleSupport(instrumentation);
    }

}
