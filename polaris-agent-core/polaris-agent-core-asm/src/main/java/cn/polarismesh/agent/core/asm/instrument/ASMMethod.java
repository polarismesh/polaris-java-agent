/*
 * Tencent is pleased to support the open source community by making Polaris available.
 *
 * Copyright (C) 2019 THL A29 Limited, a Tencent company. All rights reserved.
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

package cn.polarismesh.agent.core.asm.instrument;

import cn.polarismesh.agent.core.asm.instrument.interceptor.CaptureType;
import cn.polarismesh.agent.core.asm.instrument.interceptor.InterceptorDefinition;
import cn.polarismesh.agent.core.asm.instrument.interceptor.InterceptorDefinitionFactory;
import cn.polarismesh.agent.core.extension.registry.InterceptorRegistry;
import cn.polarismesh.agent.core.extension.instrument.exception.InstrumentException;
import cn.polarismesh.agent.core.common.exception.PolarisAgentException;
import cn.polarismesh.agent.core.common.logger.CommonLogger;
import cn.polarismesh.agent.core.common.logger.StdoutCommonLoggerFactory;
import cn.polarismesh.agent.core.extension.instrument.InstrumentMethod;
import cn.polarismesh.agent.core.extension.interceptor.Interceptor;
import java.lang.reflect.Constructor;
import java.util.Objects;

public class ASMMethod implements InstrumentMethod {

    private static final CommonLogger logger = StdoutCommonLoggerFactory.INSTANCE
            .getLogger(ASMMethod.class.getCanonicalName());

    private final InstrumentContext pluginContext;
    private final ASMClass declaringClass;
    private final ASMMethodNodeAdapter methodNode;

    private final InterceptorDefinitionFactory factory;

    public ASMMethod(InstrumentContext pluginContext, ASMClass declaringClass, ASMMethodNodeAdapter methodNode,
            InterceptorDefinitionFactory factory) {
        this.pluginContext = Objects.requireNonNull(pluginContext, "pluginContext");
        this.declaringClass = declaringClass;
        this.methodNode = methodNode;
        this.factory = factory;
    }

    @Override
    public String getName() {
        return this.methodNode.getName();
    }

    @Override
    public String[] getParameterTypes() {
        return this.methodNode.getParameterTypes();
    }

    @Override
    public String getReturnType() {
        return this.methodNode.getReturnType();
    }

    @Override
    public int getModifiers() {
        return this.methodNode.getAccess();
    }

    @Override
    public boolean isConstructor() {
        return this.methodNode.isConstructor();
    }

    public Class<? extends Interceptor> loadInterceptorClass(String interceptorClassName) throws InstrumentException {
        try {
            ClassLoader classLoader = this.declaringClass.getClassLoader();
            return pluginContext.injectClass(classLoader, interceptorClassName);
        } catch (Exception ex) {
            throw new InstrumentException(interceptorClassName + " not found Caused by:" + ex.getMessage(), ex);
        }
    }

    private int addInterceptor0(Interceptor interceptor) {
        final int interceptorId = InterceptorRegistry.addInterceptor(interceptor);

        addInterceptor0(interceptor, interceptorId);
        return interceptorId;
    }

    private Interceptor newInterceptor(Class<? extends Interceptor> interceptorClass) {
        return createInterceptor(interceptorClass);
    }


    private Interceptor createInterceptor(Class<? extends Interceptor> interceptorClass) {
        // exception handling.
        try {
            Constructor<? extends Interceptor> constructor = interceptorClass.getConstructor();
            return constructor.newInstance();
        } catch (Exception e) {
            throw new PolarisAgentException("Fail to invoke constructor: " + interceptorClass.getCanonicalName(), e);
        }
    }

    private void addInterceptor0(Interceptor interceptor, int interceptorId) {
        Objects.requireNonNull(interceptor, "interceptor");

        final InterceptorDefinition interceptorDefinition = this.factory
                .createInterceptorDefinition(interceptor.getClass());
        final Class<?> interceptorClass = interceptorDefinition.getInterceptorClass();
        final CaptureType captureType = interceptorDefinition.getCaptureType();
        if (this.methodNode.hasInterceptor()) {
            logger.warn(String.format("Skip adding interceptor. 'already intercepted method' class=%s, interceptor=%s",
                    this.declaringClass.getName(), interceptorClass.getName()));
            return;
        }

        if (this.methodNode.isAbstract() || this.methodNode.isNative()) {
            logger.warn(String.format("Skip adding interceptor. 'abstract or native method' class=%s, interceptor=%s",
                    this.declaringClass.getName(), interceptorClass.getName()));
            return;
        }

        // add before interceptor.
        if (isBeforeInterceptor(captureType) && interceptorDefinition.getBeforeMethod() != null) {
            this.methodNode.addBeforeInterceptor(interceptorId, interceptorDefinition);
            this.declaringClass.setModified(true);
        } else {
            logger.info(
                    String.format(
                            "Skip adding before interceptorDefinition because the interceptorDefinition doesn't have before method: %s",
                            interceptorClass.getName()));
        }

        // add after interface.
        if (isAfterInterceptor(captureType) && interceptorDefinition.getAfterMethod() != null) {
            this.methodNode.addAfterInterceptor(interceptorId, interceptorDefinition);
            this.declaringClass.setModified(true);
        } else {
            logger.info(
                    String.format("Skip adding after interceptor because the interceptor doesn't have after method: %s",
                            interceptorClass.getName()));
        }
    }

    private boolean isBeforeInterceptor(CaptureType captureType) {
        return CaptureType.BEFORE == captureType || CaptureType.AROUND == captureType;
    }

    private boolean isAfterInterceptor(CaptureType captureType) {
        return CaptureType.AFTER == captureType || CaptureType.AROUND == captureType;
    }

    @Override
    public int addInterceptor(Class<? extends Interceptor> interceptorClass) {
        Objects.requireNonNull(interceptorClass, "interceptorClass");

        final Interceptor interceptor = newInterceptor(interceptorClass);
        return addInterceptor0(interceptor);
    }

}