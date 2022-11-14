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


import cn.polarismesh.agent.core.common.utils.JavaAssistUtils;

/**
 * @author Woonduk Kang(emeroad)
 */
public class BootstrapPackage {

    public BootstrapPackage() {
    }

    private static final String[] BOOTSTRAP_PACKAGE_LIST = {
            "cn.polarismesh.agent.core",
    };

    private static final String[] INTERNAL_BOOTSTRAP_PACKAGE_LIST = toInternalName(BOOTSTRAP_PACKAGE_LIST);

    private static String[] toInternalName(String[] bootstrapPackageList) {
        String[] internalPackageNames = new String[bootstrapPackageList.length];
        for (int i = 0; i < bootstrapPackageList.length; i++) {
            String packageName = bootstrapPackageList[i];
            internalPackageNames[i] = JavaAssistUtils.javaNameToJvmName(packageName);
        }
        return internalPackageNames;
    }


    public boolean isBootstrapPackage(String className) {
        if (className == null) {
            return false;
        }

        for (String bootstrapPackage : BOOTSTRAP_PACKAGE_LIST) {
            if (className.startsWith(bootstrapPackage)) {
                return true;
            }
        }
        return false;
    }


    public boolean isBootstrapPackageByInternalName(String internalClassName) {
        if (internalClassName == null) {
            return false;
        }

        for (String bootstrapPackage : INTERNAL_BOOTSTRAP_PACKAGE_LIST) {
            if (internalClassName.startsWith(bootstrapPackage)) {
                return true;
            }
        }
        return false;
    }


}
