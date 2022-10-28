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


package cn.polarismesh.agent.core.spring.cloud.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 日志工具类
 *
 * @author zhuyuhan
 */
public class LogUtils {

    private static final Logger log = LoggerFactory.getLogger(LogUtils.class);

    public static void logTargetFound(Object target) {
        log.info("target {} has been found", target);
    }

    public static void logTargetMethodFound(String method) {
        log.info("agent method is found: {} for instrumentation", method);
    }

    public static void logInvoke(Object invoker, String method) {
        log.info("agent method {}() is invoked by {}", method, invoker);
    }

    public static void logError(String message, Object... objects) {
        log.error(message, objects);
    }

    public static void logInterceptError(String interceptor, Object... objects) {
        log.error(interceptor + " intercept fail with error {}", objects);
    }

}
