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

package cn.polarismesh.agent.core.common.utils;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ClassLoadingChecker {

    private final Set<String> loadClass = new HashSet<>();

    public boolean isFirstLoad(String className) {
        Objects.requireNonNull(className, "className");

        if (this.loadClass.add(className)) {
            // first load
            return true;
        }
        // already exist
        return false;
    }
}
