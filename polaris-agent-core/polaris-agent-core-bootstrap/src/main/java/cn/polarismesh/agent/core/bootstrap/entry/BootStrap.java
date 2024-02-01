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

package cn.polarismesh.agent.core.bootstrap.entry;

import cn.polarismesh.agent.core.bootstrap.entry.utils.AgentDirUtils;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class BootStrap {

    private static final String BOOT_STRAP_CLAZZ_NAME = "cn.polarismesh.agent.core.bootstrap.PolarisAgentBootStrap";

    public static void premain(String agentArgs, Instrumentation instrumentation) {
        final JavaAgentPathResolver javaAgentPathResolver = JavaAgentPathResolver.newJavaAgentPathResolver(BootStrap.class.getCanonicalName());
        final String agentPath = javaAgentPathResolver.resolveJavaAgentPath();
        System.out.println("[Bootstrap] javaagent start with path:" + agentPath);
        if (agentPath == null) {
            System.out.println("[Bootstrap] javaagent path not found path, exit");
            return;
        }
        String agentDirPath = AgentDirUtils.resolveAgentDir(agentPath);
        List<String> libDirPaths;
        if (checkModuleClass()) {
            // load java9 bootstrap
            libDirPaths = AgentDirUtils.getJava9LibDir(agentDirPath);
        } else {
            libDirPaths = AgentDirUtils.getJava8LibDir(agentDirPath);
        }
        List<URL> urls = new ArrayList<>();
        for (String libDir : libDirPaths) {
            urls.addAll(AgentDirUtils.resolveLib(libDir));
        }
        try {
            ClassLoader classLoader = new URLClassLoader(urls.toArray(new URL[0]), BootStrap.class.getClassLoader());
            Class<Object> clazz = getClazz(BOOT_STRAP_CLAZZ_NAME, classLoader);

            Method premain = clazz.getMethod("premain", String.class, Instrumentation.class, String.class);
            premain.invoke(null, agentArgs, instrumentation, agentDirPath);
            System.out.println("[Bootstrap] javaagent inject successfully");
        } catch (Exception e) {
            String errMsg = e.getMessage();
            if (e instanceof InvocationTargetException) {
                Throwable targetException = ((InvocationTargetException)e).getTargetException();
                if (null != targetException) {
                    errMsg = targetException.getMessage();
                }
            }
            System.err.println("[Bootstrap] javaagent inject failed: " + errMsg);
        }
    }

    private static boolean checkModuleClass() {
        try {
            Class.forName("java.lang.Module", false, null);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static <T> Class<T> getClazz(String clazzName, ClassLoader classLoader) {
        try {
            return (Class<T>) Class.forName(clazzName, false, classLoader);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(clazzName + " not found");
        }
    }

}
