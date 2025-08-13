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

package cn.polarismesh.agent.core.asm.instrument;

import cn.polarismesh.agent.core.extension.instrument.ClassFilter;
import cn.polarismesh.agent.core.extension.instrument.InstrumentClass;

public class ClassFilters {

    private ClassFilters() {
    }

    public static final ClassFilter ACCEPT_ALL = new ClassFilter() {
        @Override
        public boolean accept(InstrumentClass clazz) {
            return ACCEPT;
        }
    };

    public static ClassFilter chain(
            ClassFilter... classFilters) {
        return new ChainFilter(classFilters);
    }

    public static ClassFilter name(String... classNames) {
        return new ClassNameFilter(classNames);
    }

    public static ClassFilter enclosingMethod(String methodName,
            String... parameterTypes) {
        return new EnclosingMethodFilter(methodName, parameterTypes);
    }

    public static ClassFilter interfaze(String... interfaceNames) {
        return new InterfaceFilter(interfaceNames);
    }

    private static final class ClassNameFilter implements ClassFilter {

        private final String[] classNames;

        public ClassNameFilter(String[] classNames) {
            this.classNames = classNames;
        }

        @Override
        public boolean accept(InstrumentClass clazz) {
            if (classNames == null) {
                return REJECT;
            }

            for (String className : classNames) {
                if (className != null && className.equals(clazz.getName())) {
                    return ACCEPT;
                }
            }

            return REJECT;
        }
    }

    private static final class EnclosingMethodFilter implements
            ClassFilter {

        private final String methodName;
        private final String[] parameterTypes;

        public EnclosingMethodFilter(String methodName, String[] parameterTypes) {
            this.methodName = methodName;
            this.parameterTypes = parameterTypes;
        }

        @Override
        public boolean accept(InstrumentClass clazz) {
            if (methodName == null) {
                return REJECT;
            }

            return clazz.hasEnclosingMethod(methodName, parameterTypes);
        }
    }

    private static final class InterfaceFilter implements ClassFilter {

        private final String[] interfaceNames;

        // interface names is 'or' condition.
        public InterfaceFilter(String[] interfaceNames) {
            this.interfaceNames = interfaceNames;
        }

        @Override
        public boolean accept(InstrumentClass clazz) {
            if (interfaceNames == null) {
                return REJECT;
            }

            for (String interfaceName : interfaceNames) {
                for (String name : clazz.getInterfaces()) {
                    if (name != null && name.equals(interfaceName)) {
                        return ACCEPT;
                    }
                }
            }

            return REJECT;
        }
    }

    private static final class ChainFilter implements ClassFilter {

        private final ClassFilter[] classFilters;

        public ChainFilter(ClassFilter[] classFilters) {
            this.classFilters = classFilters;
        }

        @Override
        public boolean accept(InstrumentClass clazz) {
            if (classFilters == null) {
                return REJECT;
            }

            for (ClassFilter classFilter : classFilters) {
                if (classFilter == null || !classFilter.accept(clazz)) {
                    return REJECT;
                }
            }

            return ACCEPT;
        }
    }
}