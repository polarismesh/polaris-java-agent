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

package cn.polarismesh.agent.core.asm.instrument.classloading;

import cn.polarismesh.agent.core.asm.instrument.BootstrapPackage;
import cn.polarismesh.agent.core.asm.scanner.JarFileRepository;
import cn.polarismesh.agent.core.common.logger.CommonLogger;
import cn.polarismesh.agent.core.common.logger.StdoutCommonLoggerFactory;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

public class BootstrapCore {

    private static final CommonLogger logger = StdoutCommonLoggerFactory.INSTANCE
            .getLogger(BootstrapCore.class.getCanonicalName());

    private final BootstrapPackage bootstrapPackage;
    private final JarFileRepository bootstrapRepository;
    private final ClassLoader bootstrapClassLoader = Object.class.getClassLoader();


    public BootstrapCore(List<String> bootstrapJarPaths) {
        Objects.requireNonNull(bootstrapJarPaths, "bootstrapJarPaths");

        this.bootstrapRepository = new JarFileRepository(bootstrapJarPaths);
        this.bootstrapPackage = new BootstrapPackage();
    }

    public boolean isBootstrapPackage(String className) {
        return bootstrapPackage.isBootstrapPackage(className);
    }

    public boolean isBootstrapPackageByInternalName(String internalClassName) {
        return bootstrapPackage.isBootstrapPackageByInternalName(internalClassName);
    }

    public InputStream openStream(String internalClassName) {
        return bootstrapRepository.openStream(internalClassName);
    }

    @SuppressWarnings("unchecked")
    public <T> Class<? extends T> loadClass(String className) {
        try {
            return (Class<T>) Class.forName(className, false, bootstrapClassLoader);
        } catch (ClassNotFoundException ex) {
            logger.warn(String.format("ClassNotFound %s cl:%s", ex.getMessage(), bootstrapClassLoader));
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }
}
