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
import cn.polarismesh.agent.core.common.utils.JvmUtils;
import cn.polarismesh.agent.core.common.utils.JvmVersion;
import java.util.Objects;

public class JavaVersionFilter implements PluginFilter {

    private static final CommonLogger logger = StdoutCommonLoggerFactory.INSTANCE
            .getLogger(JavaVersionFilter.class.getCanonicalName());
    private final JvmVersion jvmVersion;

    public JavaVersionFilter() {
        this(JvmUtils.getVersion());
    }

    public JavaVersionFilter(JvmVersion jvmVersion) {
        this.jvmVersion = Objects.requireNonNull(jvmVersion, "jvmVersion");
    }

    @Override
    public boolean accept(PluginJar pluginJar) {
        String pluginId = pluginJar.getPluginId();
        if (pluginId == null) {
            logger.warn(
                    String.format("invalid plugin : %s, missing manifest entry : %s", pluginJar.getJarFile().getName(),
                            PluginJar.PLUGIN_ID));
            return REJECT;
        }
        String pluginCompilerVersion = pluginJar.getPluginCompilerVersion();
        if (pluginCompilerVersion == null) {
            logger.info(
                    String.format("skipping %s due to missing manifest entry : %s", pluginJar.getJarFile().getName(),
                            PluginJar.PLUGIN_COMPILER_VERSION));
            return REJECT;
        }
        JvmVersion pluginJvmVersion = JvmVersion.getFromVersion(pluginCompilerVersion);
        if (pluginJvmVersion == JvmVersion.UNSUPPORTED) {
            logger.info(String.format("skipping %s due to unknown plugin compiler version : %s", pluginId,
                    pluginCompilerVersion));
            return REJECT;
        }
        if (jvmVersion.onOrAfter(pluginJvmVersion)) {
            return ACCEPT;
        }
        logger.info(
                String.format("skipping %s due to java version. Required : %s, found : %s", pluginId, pluginJvmVersion,
                        jvmVersion));
        return REJECT;
    }

    @Override
    public String toString() {
        return "JavaVersionFilter{" + "jvmVersion=" + jvmVersion + '}';
    }
}
