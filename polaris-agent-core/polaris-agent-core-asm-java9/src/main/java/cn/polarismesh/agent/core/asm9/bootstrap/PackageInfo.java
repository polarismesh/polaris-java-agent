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

/* copy from pinpoint-apm
 * https://github.com/pinpoint-apm/pinpoint/blob/v2.3.3/bootstraps/bootstrap-java9/src/main/java/com/navercorp/pinpoint/bootstrap/java9/module;/PackageInfo.java
 */

package cn.polarismesh.agent.core.asm9.bootstrap;


import cn.polarismesh.agent.core.asm9.module.Providers;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author Woonduk Kang(emeroad)
 */
public class PackageInfo {
    private final Set<String> packageSet;
    private final List<Providers> providersList;

    public PackageInfo(Set<String> packageSet, List<Providers> providersList) {
        this.packageSet = Objects.requireNonNull(packageSet, "packageSet");
        this.providersList = Objects.requireNonNull(providersList, "providersList");
    }

    public Set<String> getPackage() {
        return packageSet;
    }

    public List<Providers> getProviders() {
        return providersList;
    }

    @Override
    public String toString() {
        return "PackageInfo{" +
                "packageSet=" + packageSet +
                ", providersList=" + providersList +
                '}';
    }
}
