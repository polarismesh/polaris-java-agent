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

package cn.polarismesh.agent.core.extension.registry;

import cn.polarismesh.agent.core.extension.interceptor.Interceptor;

import java.util.concurrent.atomic.AtomicInteger;

public class InterceptorRegistryAdaptor {

    private final static int DEFAULT_MAX = 8192;
    private final int registrySize;

    private final AtomicInteger id = new AtomicInteger(0);

    private final WeakAtomicReferenceArray<Interceptor> index;

    public InterceptorRegistryAdaptor() {
        this(DEFAULT_MAX);
    }

    public InterceptorRegistryAdaptor(int maxRegistrySize) {
        if (maxRegistrySize < 0) {
            throw new IllegalArgumentException("negative maxRegistrySize:" + maxRegistrySize);
        }
        this.registrySize = maxRegistrySize;
        this.index = new WeakAtomicReferenceArray<Interceptor>(maxRegistrySize, Interceptor.class);
    }

    public int addInterceptor(Interceptor interceptor) {
        if (interceptor == null) {
            return -1;
        }

        final int newId = nextId();
        if (newId >= registrySize) {
            throw new IndexOutOfBoundsException(
                    "Interceptor registry size exceeded. Check the \"profiler.interceptorregistry.size\" setting. size="
                            + index.length() + " id=" + id);
        }
        index.set(newId, interceptor);
        return newId;
    }

    private int nextId() {
        return id.getAndIncrement();
    }

    public Interceptor getInterceptor(int key) {
        return this.index.get(key);
    }
}
