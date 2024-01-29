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

package cn.polarismesh.agent.core.asm9.module.impl;

import cn.polarismesh.agent.core.asm9.bootstrap.JarFileAnalyzer;
import cn.polarismesh.agent.core.asm9.bootstrap.PackageInfo;
import cn.polarismesh.agent.core.asm9.module.Providers;
import cn.polarismesh.agent.core.common.logger.CommonLogger;
import cn.polarismesh.agent.core.common.logger.StdoutCommonLoggerFactory;
import jdk.internal.module.Modules;

import java.io.Closeable;
import java.io.IOException;
import java.lang.module.ModuleDescriptor;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarFile;

public class ModuleBuilder {

    private static final CommonLogger logger = StdoutCommonLoggerFactory.INSTANCE
            .getLogger(ModuleBuilder.class.getCanonicalName());


    public Module defineModule(String moduleName, ClassLoader classLoader, URL[] urls) {
        Objects.requireNonNull(moduleName, "moduleName");
        Objects.requireNonNull(urls, "urls");
        if (urls.length == 0) {
            throw new IllegalArgumentException("urls.length is 0");
        }
        logger.info("platform unnamedModule:" + ClassLoader.getPlatformClassLoader().getUnnamedModule());
        logger.info("system unnamedModule:" + ClassLoader.getSystemClassLoader().getUnnamedModule());

        Module unnamedModule = classLoader.getUnnamedModule();
        logger.info("defineModule classLoader: " + classLoader);
        logger.info("defineModule classLoader-unnamedModule: " + unnamedModule);


        List<PackageInfo> packageInfos = parsePackageInfo(urls);
        Set<String> packages = mergePackageInfo(packageInfos);
        logger.info("packages:" + packages);
        Map<String, Set<String>> serviceInfoMap = mergeServiceInfo(packageInfos);
        logger.info("providers:" + serviceInfoMap);

        ModuleDescriptor.Builder builder = ModuleDescriptor.newModule(moduleName);
        builder.packages(packages);
        for (Map.Entry<String, Set<String>> entry : serviceInfoMap.entrySet()) {
            builder.provides(entry.getKey(), new ArrayList<>(entry.getValue()));
        }

        ModuleDescriptor moduleDescriptor = builder.build();
        URI url = getInformationURI(urls);

        Module module = Modules.defineModule(classLoader, moduleDescriptor , url);
        logger.info("defineModule module:" + module);
        return module;
    }

    private Map<String, Set<String>> mergeServiceInfo(List<PackageInfo> packageInfos) {
        Map<String, Set<String>> providesMap = new HashMap<>();
        for (PackageInfo packageInfo : packageInfos) {
            List<Providers> serviceLoader = packageInfo.getProviders();
            for (Providers provides : serviceLoader) {
                Set<String> providerSet = providesMap.computeIfAbsent(provides.getService(), s -> new HashSet<>());
                providerSet.addAll(provides.getProviders());
            }
        }
        return providesMap;
    }

    private Set<String> mergePackageInfo(List<PackageInfo> packageInfos) {
        Set<String> packageSet = new HashSet<>();
        for (PackageInfo packageInfo : packageInfos) {
            packageSet.addAll(packageInfo.getPackage());
        }
        return packageSet;
    }

    private JarFile newJarFile(URL jarFile) {
        try {
            if (!jarFile.getProtocol().equals("file")) {
                throw new IllegalStateException("invalid file " + jarFile);
            }
            return new JarFile(jarFile.getFile());
        } catch (IOException e) {
            throw new RuntimeException(jarFile.getFile() +  " create fail " + e.getMessage(), e);
        }
    }

    private URI getInformationURI(URL[] urls) {
        if (isEmpty(urls)) {
            return null;
        }
        final URL url = urls[0];
        try {
            return url.toURI();
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    private boolean isEmpty(URL[] urls) {
        return urls == null || urls.length == 0;
    }

    private List<PackageInfo> parsePackageInfo(URL[] urls) {

        final List<PackageInfo> packageInfoList = new ArrayList<>();
        for (URL url : urls) {
            if (!isJar(url)) {
                continue;
            }
            JarFile jarFile = null;
            try {
                jarFile = newJarFile(url);
                JarFileAnalyzer packageAnalyzer = new JarFileAnalyzer(jarFile);
                PackageInfo packageInfo = packageAnalyzer.analyze();
                packageInfoList.add(packageInfo);
            } finally {
                close(jarFile);
            }
        }
        return packageInfoList;
    }

    private boolean isJar(URL url){
        // filter *.xml
        if (url.getPath().endsWith(".jar")) {
            return true;
        }
        return false;
    }

    private void close(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException ignore) {
            // skip
        }
    }


}
