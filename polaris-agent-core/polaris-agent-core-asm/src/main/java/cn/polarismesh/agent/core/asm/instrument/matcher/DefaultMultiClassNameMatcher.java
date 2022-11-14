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

package cn.polarismesh.agent.core.asm.instrument.matcher;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class DefaultMultiClassNameMatcher implements MultiClassNameMatcher {

    private final List<String> classNameList;

    DefaultMultiClassNameMatcher(List<String> classNameMatcherList) {
        Objects.requireNonNull(classNameMatcherList, "classNameMatcherList");

        this.classNameList = Collections.unmodifiableList(classNameMatcherList);
    }

    @Override
    public List<String> getClassNames() {
        return classNameList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DefaultMultiClassNameMatcher that = (DefaultMultiClassNameMatcher) o;

        return classNameList.equals(that.classNameList);

    }

    @Override
    public int hashCode() {
        return classNameList.hashCode();
    }
}
