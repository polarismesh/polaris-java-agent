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

package cn.polarismesh.agent.core.asm.instrument.plugin;

import cn.polarismesh.agent.core.common.logger.CommonLogger;
import cn.polarismesh.agent.core.common.logger.StdoutCommonLoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AgentPackageSkipFilter implements ClassNameFilter {

    private static final CommonLogger logger = StdoutCommonLoggerFactory.INSTANCE
            .getLogger(AgentPackageSkipFilter.class.getCanonicalName());
    private final String[] packageList;

    public AgentPackageSkipFilter() {
        this(getAgentPackageList());
    }

    private AgentPackageSkipFilter(List<String> packageList) {
        Objects.requireNonNull(packageList, "packageList");
        this.packageList = packageList.toArray(new String[0]);
    }


    @Override
    public boolean accept(String className) {
        Objects.requireNonNull(className, "className");

        for (String packageName : packageList) {
            if (className.startsWith(packageName)) {
                if (logger.isDebugEnabled()) {
                    logger.info(String.format("skip ProfilerPackage:%s Class:%s", packageName, className));
                }
                return REJECT;
            }
        }
        return ACCEPT;
    }

    private static List<String> getAgentPackageList() {
        List<String> agentPackageList = new ArrayList<>();
        agentPackageList.add("cn.polarismesh.agent.core.asm");
        agentPackageList.add("cn.polarismesh.agent.core.bootstrap");
        return agentPackageList;
    }
}
