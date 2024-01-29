/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.polarismesh.agent.core.bootstrap.util;

import cn.polarismesh.agent.core.bootstrap.BootLogger;
import cn.polarismesh.agent.core.common.utils.FileUtils;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AgentDirUtils {

    private static final BootLogger logger = BootLogger.getLogger(AgentDirUtils.class);

    public static String getRootDir(String agentDirPath) {
        return agentDirPath + File.separator;
    }

    public static String getBootDir(String agentDirPath) {
        return agentDirPath + File.separator + "boot" + File.separator;
    }

    public static List<String> resolveJarPaths(String bootDirPath) {
        File[] jarFiles = FileUtils.listFiles(new File(bootDirPath), new String[]{".jar"});
        List<String> fileNames = new ArrayList<>();
        if (FileUtils.isEmpty(jarFiles)) {
            logger.info(bootDirPath + " is empty");
        } else {
            for (File file : jarFiles) {
                fileNames.add(FileUtils.toCanonicalPath(file));
            }
        }
        return fileNames;
    }

    public static List<URL> resolveLib(String agentLibPath) {
        final File libDir = new File(agentLibPath);
        if (checkDirectory(libDir)) {
            return Collections.emptyList();
        }
        final File[] libFileList = FileUtils.listFiles(libDir, new String[]{".jar"});

        List<URL> libURLList = toURLs(libFileList);
        // add directory
        URL agentDirUri = toURL(new File(agentLibPath));

        List<URL> jarURLList = new ArrayList<>(libURLList);
        jarURLList.add(agentDirUri);

        logger.info(String.format("agent LibDir:%s", agentLibPath));
        for (URL url : jarURLList) {
            logger.info(String.format("agent Lib:%s", url));
        }
        return jarURLList;
    }

    private static URL toURL(File file) {
        try {
            return FileUtils.toURL(file);
        } catch (IOException e) {
            logger.warn(file.getName() + ".toURL() failed.", e);
            throw new RuntimeException(file.getName() + ".toURL() failed.", e);
        }
    }

    private static boolean checkDirectory(File file) {
        if (!file.exists()) {
            logger.warn(file + " not found");
            return true;
        }
        if (!file.isDirectory()) {
            logger.warn(file + " is not a directory");
            return true;
        }
        return false;
    }

    public static List<URL> toURLs(File[] jarFileList) {
        try {
            URL[] jarURLArray = FileUtils.toURLs(jarFileList);
            return Arrays.asList(jarURLArray);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
