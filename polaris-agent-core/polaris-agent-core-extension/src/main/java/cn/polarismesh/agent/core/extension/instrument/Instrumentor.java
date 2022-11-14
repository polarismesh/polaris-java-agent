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

package cn.polarismesh.agent.core.extension.instrument;

import cn.polarismesh.agent.core.extension.transform.TransformCallback;
import java.security.ProtectionDomain;

public interface Instrumentor {

    InstrumentClass getInstrumentClass(ClassLoader classLoader, String className, ProtectionDomain protectionDomain,
            byte[] classfileBuffer);

    InstrumentClass getInstrumentClass(ClassLoader classLoader, String className, byte[] classfileBuffer);

    boolean exist(ClassLoader classLoader, String className);

    boolean exist(ClassLoader classLoader, String className, ProtectionDomain protectionDomain);

    <T> Class<? extends T> injectClass(ClassLoader targetClassLoader, String className);

    void transform(ClassLoader classLoader, String targetClassName,
            Class<? extends TransformCallback> transformCallbackClass);

    void retransform(Class<?> target, TransformCallback transformCallback);
}
