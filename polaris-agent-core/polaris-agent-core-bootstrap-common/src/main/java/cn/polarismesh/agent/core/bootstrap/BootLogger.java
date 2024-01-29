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

package cn.polarismesh.agent.core.bootstrap;

import cn.polarismesh.agent.core.common.logger.CommonLogger;
import cn.polarismesh.agent.core.common.logger.StdoutCommonLoggerFactory;

public class BootLogger {

    private final CommonLogger logger;

    public BootLogger(String loggerName) {
        logger = StdoutCommonLoggerFactory.INSTANCE.getLogger(loggerName);
    }

    public static BootLogger getLogger(Class<?> clazz) {
        return new BootLogger(clazz.getSimpleName());
    }

    public static BootLogger getLogger(String loggerName) {
        return new BootLogger(loggerName);
    }

    public void info(String msg) {
        logger.info(msg);
    }

    public void warn(String msg) {
        logger.warn(msg);
    }

    public void warn(String msg, Throwable throwable) {
        logger.warn(msg, throwable);
    }

}
