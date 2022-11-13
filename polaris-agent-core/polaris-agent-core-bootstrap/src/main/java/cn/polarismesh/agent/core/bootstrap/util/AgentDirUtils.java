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
import cn.polarismesh.agent.core.bootstrap.JarDescription;
import cn.polarismesh.agent.core.bootstrap.JavaAgentPathResolver;
import cn.polarismesh.agent.core.common.utils.FileUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class AgentDirUtils {

    private static final BootLogger logger = BootLogger.getLogger(AgentDirUtils.class);

    public static String getBootDir(String agentDirPath) {
        return agentDirPath + File.separator;
    }

    public static String resolveAgentDir(String classPath) {
        final JarDescription bootstrap = JavaAgentPathResolver.BOOT_JAR_DESC;
        // find boot-strap.jar
        final String bootstrapJarName = findBootstrapJar(bootstrap, classPath);
        if (bootstrapJarName == null) {
            throw new IllegalStateException(bootstrap.getSimplePattern() + " not found: classpath " + classPath);
        }

        final String agentJarFullPath = parseAgentJarPath(classPath, bootstrapJarName);
        if (agentJarFullPath == null) {
            throw new IllegalStateException(bootstrap.getSimplePattern() + " not found. " + classPath);
        }
        return getAgentDirPath(agentJarFullPath, classPath);
    }

    private static String getAgentDirPath(String agentJarFullPath, String classPath) {
        String agentDirPath = parseAgentDirPath(agentJarFullPath);
        if (agentDirPath == null) {
            throw new IllegalStateException("agentDirPath is null " + classPath);
        }

        logger.info("Agent original-path:" + agentDirPath);
        // defense alias change
        agentDirPath = toCanonicalPath(new File(agentDirPath));
        logger.info("Agent canonical-path:" + agentDirPath);
        return agentDirPath;
    }


    private static String findBootstrapJar(JarDescription bootstrap, String classPath) {
        final Matcher matcher = bootstrap.getVersionPattern().matcher(classPath);
        if (!matcher.find()) {
            return null;
        }
        return parseAgentJar(matcher, classPath);
    }


    private static String parseAgentJar(Matcher matcher, String classpath) {

        int start = matcher.start();
        int end = matcher.end();
        return classpath.substring(start, end);
    }

    private static String parseAgentJarPath(String classPath, String agentJar) {
        String[] classPathList = classPath.split(File.pathSeparator);
        for (String findPath : classPathList) {
            boolean find = findPath.contains(agentJar);
            if (find) {
                return findPath;
            }
        }
        return null;
    }

    private static String parseAgentDirPath(String agentJarFullPath) {
        int index1 = agentJarFullPath.lastIndexOf("/");
        int index2 = agentJarFullPath.lastIndexOf("\\");
        int max = Math.max(index1, index2);
        if (max == -1) {
            return null;
        }
        return agentJarFullPath.substring(0, max);
    }

    private static String toCanonicalPath(File file) {
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            logger.warn(file.getPath() + " getCanonicalPath() error. Error:" + e.getMessage(), e);
            return file.getAbsolutePath();
        }
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

}
