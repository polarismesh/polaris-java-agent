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

package cn.polarismesh.agent.core.extension.interceptor;

import cn.polarismesh.agent.core.common.logger.CommonLogger;
import cn.polarismesh.agent.core.common.logger.StdoutCommonLoggerFactory;
import java.util.Arrays;

public class LoggingInterceptor implements Interceptor {

    private final CommonLogger logger;

    public LoggingInterceptor() {
        this.logger = StdoutCommonLoggerFactory.INSTANCE.getLogger(LoggingInterceptor.class.getCanonicalName());
    }

    public LoggingInterceptor(String loggerName) {
        this.logger = StdoutCommonLoggerFactory.INSTANCE.getLogger(loggerName);
    }

    @Override
    public void before(Object target, Object[] args) {
        logger.info("before " + target + " args:" + Arrays.toString(args));
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        logger.info("after " + target + " args:" + Arrays.toString(args) + " result:" + result
                + " Throwable:" + throwable);

    }
}
