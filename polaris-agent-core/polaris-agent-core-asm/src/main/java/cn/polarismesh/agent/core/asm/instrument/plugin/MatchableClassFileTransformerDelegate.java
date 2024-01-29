/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.polarismesh.agent.core.asm.instrument.plugin;

import cn.polarismesh.agent.core.extension.instrument.exception.InstrumentException;
import cn.polarismesh.agent.core.common.exception.PolarisAgentException;
import cn.polarismesh.agent.core.asm.instrument.GuardInstrumentor;
import cn.polarismesh.agent.core.asm.instrument.InstrumentContext;
import cn.polarismesh.agent.core.asm.instrument.matcher.Matcher;
import cn.polarismesh.agent.core.extension.transform.TransformCallback;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Objects;

/**
 * @author emeroad
 */
public class MatchableClassFileTransformerDelegate implements MatchableClassFileTransformer {

    private final InstrumentContext instrumentContext;
    private final Matcher matcher;
    private final TransformCallbackProvider transformCallbackProvider;


    public MatchableClassFileTransformerDelegate(InstrumentContext instrumentContext, Matcher matcher,
            TransformCallbackProvider transformCallbackProvider) {
        this.instrumentContext = Objects.requireNonNull(instrumentContext, "instrumentContext");
        this.matcher = Objects.requireNonNull(matcher, "matcher");
        this.transformCallbackProvider = Objects.requireNonNull(transformCallbackProvider, "transformCallback");
    }


    @Override
    public Matcher getMatcher() {
        return matcher;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        Objects.requireNonNull(className, "className");

        final InstrumentContext instrumentContext = this.instrumentContext;
        final GuardInstrumentor guard = new GuardInstrumentor(instrumentContext);
        try {
            // WARN external plugin api
            final TransformCallback transformCallback = transformCallbackProvider
                    .getTransformCallback(instrumentContext, loader);
            return transformCallback
                    .doInTransform(guard, loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
        } catch (InstrumentException e) {
            throw new PolarisAgentException(e);
        } finally {
            guard.close();
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MatchableClassFileTransformerDelegate{");
        sb.append("matcher=").append(matcher);
        sb.append(", transformCallbackProvider=").append(transformCallbackProvider);
        sb.append('}');
        return sb.toString();
    }
}
