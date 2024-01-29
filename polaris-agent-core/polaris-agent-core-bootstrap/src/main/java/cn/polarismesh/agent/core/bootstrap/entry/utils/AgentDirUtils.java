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

package cn.polarismesh.agent.core.bootstrap.entry.utils;

import cn.polarismesh.agent.core.bootstrap.entry.JarDescription;
import cn.polarismesh.agent.core.bootstrap.entry.JavaAgentPathResolver;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

public class AgentDirUtils {

    public static List<String> getJava9LibDir(String agentDirPath) {
        List<String> values = getJava8LibDir(agentDirPath);
        values.add(agentDirPath + File.separator + "lib" + File.separator + "java9" + File.separator);
        return values;
    }

    public static List<String> getJava8LibDir(String agentDirPath) {
        List<String> values = new ArrayList<>();
        values.add(agentDirPath + File.separator + "lib" + File.separator);
        return values;
    }

    public static String getBootDir(String agentDirPath) {
        return agentDirPath + File.separator + "boot" + File.separator;
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

        System.out.println("Agent original-path:" + agentDirPath);
        // defense alias change
        agentDirPath = toCanonicalPath(new File(agentDirPath));
        System.out.println("Agent canonical-path:" + agentDirPath);
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
            System.out.println(file.getPath() + " getCanonicalPath() error. Error:" + e.getMessage());
            return file.getAbsolutePath();
        }
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

        System.out.println(String.format("agent LibDir:%s", agentLibPath));
        for (URL url : jarURLList) {
            System.out.println(String.format("agent Lib:%s", url));
        }
        return jarURLList;
    }

    private static URL toURL(File file) {
        try {
            return FileUtils.toURL(file);
        } catch (IOException e) {
            System.out.println(file.getName() + ".toURL() failed: " + e.getMessage());
            throw new RuntimeException(file.getName() + ".toURL() failed.", e);
        }
    }

    private static boolean checkDirectory(File file) {
        if (!file.exists()) {
            System.out.println(file + " not found");
            return true;
        }
        if (!file.isDirectory()) {
            System.out.println(file + " is not a directory");
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
