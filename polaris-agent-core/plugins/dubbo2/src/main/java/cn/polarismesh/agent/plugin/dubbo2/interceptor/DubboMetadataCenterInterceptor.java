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

package cn.polarismesh.agent.plugin.dubbo2.interceptor;

import cn.polarismesh.agent.common.tools.ReflectionUtils;
import cn.polarismesh.common.interceptor.AbstractInterceptor;
import org.apache.dubbo.config.AbstractConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * interceptor for org.apache.dubbo.config.context.ConfigManager#getMetadataConfigs()
 */
public class DubboMetadataCenterInterceptor implements AbstractInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DubboMetadataCenterInterceptor.class);

    private static final String METADATA_REPORT_KEY = "metadata-report";

    /**
     * 在getMetadataConfigs()处拦截，清除掉configsCache中关于metadata-report的信息
     */
    @Override
    @SuppressWarnings("unchecked")
    public void before(Object target, Object[] args) {
        Map<String, Map<String, AbstractConfig>> configsCache =
                (Map<String, Map<String, AbstractConfig>>) ReflectionUtils.getObjectByFieldName(target, "configsCache");
        if (null == configsCache) {
            LOGGER.error("metadata-report: get configsCache fail, object is null");
            return;
        }
        configsCache.remove(METADATA_REPORT_KEY);
        LOGGER.info("clean metadata-report key in dubbo ConfigManager");
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
    }
}