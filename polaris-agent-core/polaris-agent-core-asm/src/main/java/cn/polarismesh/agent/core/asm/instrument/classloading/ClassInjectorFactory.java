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

package cn.polarismesh.agent.core.asm.instrument.classloading;

import cn.polarismesh.agent.core.asm.instrument.InstrumentEngine;
import cn.polarismesh.agent.core.asm.instrument.plugin.PluginConfig;
import java.util.Objects;

public class ClassInjectorFactory {

    private final InstrumentEngine instrumentEngine;
    private final BootstrapCore bootstrapCore;

    public ClassInjectorFactory(InstrumentEngine instrumentEngine,
            BootstrapCore bootstrapCore) {
        this.instrumentEngine = Objects.requireNonNull(instrumentEngine, "instrumentEngine");
        this.bootstrapCore = Objects.requireNonNull(bootstrapCore, "bootstrapCore");
    }

    public ClassInjector newClassInjector(PluginConfig pluginConfig) {
        return new JarProfilerPluginClassInjector(pluginConfig,
                instrumentEngine, bootstrapCore);
    }
}
