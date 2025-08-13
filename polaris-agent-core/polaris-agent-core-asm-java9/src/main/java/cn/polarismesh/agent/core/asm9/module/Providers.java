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

package cn.polarismesh.agent.core.asm9.module;

import java.util.List;
import java.util.Objects;

public class Providers {
    private final String services;
    private final List<String> providers;

    public Providers(String services, List<String> providers) {
        this.services = Objects.requireNonNull(services, "services");
        this.providers = Objects.requireNonNull(providers, "providers");
    }

    public String getService() {
        return services;
    }

    public List<String> getProviders() {
        return providers;
    }

    @Override
    public String toString() {
        return "Providers{" +
                "services='" + services + '\'' +
                ", providers=" + providers +
                '}';
    }
}
