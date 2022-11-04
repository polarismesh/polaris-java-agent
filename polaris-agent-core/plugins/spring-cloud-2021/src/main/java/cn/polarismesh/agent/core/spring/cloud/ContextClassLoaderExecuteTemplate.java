/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.polarismesh.agent.core.spring.cloud;

import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This template is used for changing the current thread's classloader to the assigned one and executing a callable.
 *
 * @author emeroad
 */
public class ContextClassLoaderExecuteTemplate {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContextClassLoaderExecuteTemplate.class);

    private final ClassLoader classLoader;

    public ContextClassLoaderExecuteTemplate(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public Object execute(String command, Callable<?> callable) {
        final Thread currentThread = Thread.currentThread();
        final ClassLoader before = currentThread.getContextClassLoader();
        currentThread.setContextClassLoader(this.classLoader);
        try {
            return callable.call();
        } catch (Exception e) {
            LOGGER.error("fail to execute command {}", command, e);
            return null;
        } finally {
            // even though  the "BEFORE" classloader  is null, rollback  is needed.
            // if an exception occurs BEFORE callable.call(), the call flow can't reach here.
            // so  rollback  here is right.
            currentThread.setContextClassLoader(before);
        }
    }
}
