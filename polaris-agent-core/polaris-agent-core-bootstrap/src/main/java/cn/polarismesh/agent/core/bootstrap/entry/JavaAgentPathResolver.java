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

package cn.polarismesh.agent.core.bootstrap.entry;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Woonduk Kang(emeroad)
 */
public class JavaAgentPathResolver {

    public static final JarDescription BOOT_JAR_DESC = new JarDescription("polaris-agent-core-bootstrap", true);

    private final AgentPathFinder[] agentPathFinders;

    JavaAgentPathResolver(AgentPathFinder[] agentPathFinders) {
        this.agentPathFinders = Objects.requireNonNull(agentPathFinders, "agentPathFinders");
    }

    public static JavaAgentPathResolver newJavaAgentPathResolver(String clazzName) {
        final AgentPathFinder[] agentPathFinders = newAgentPathFinder(clazzName);
        return new JavaAgentPathResolver(agentPathFinders);
    }

    private static AgentPathFinder[] newAgentPathFinder(String clazzName) {
        AgentPathFinder classAgentPath = new ClassAgentPathFinder(clazzName);
        AgentPathFinder inputArgumentAgentPath = new InputArgumentAgentPathFinder();
        return new AgentPathFinder[]{classAgentPath, inputArgumentAgentPath};
    }

    public String resolveJavaAgentPath() {
        for (AgentPathFinder agentPath : agentPathFinders) {
            final String path = agentPath.getPath();
            if (path != null) {
                return path;
            }
        }
        return null;
    }

    interface AgentPathFinder {

        String getPath();
    }

    static class ClassAgentPathFinder implements AgentPathFinder {

        private final String className;

        public ClassAgentPathFinder(String className) {
            this.className = className;
        }

        @Override
        public String getPath() {
            // get bootstrap.jar location
            return getJarLocation(this.className);
        }

        String getJarLocation(String className) {
            final String internalClassName = className.replace('.', '/') + ".class";
            final URL classURL = getResource(internalClassName);
            if (classURL == null) {
                return null;
            }

            if (classURL.getProtocol().equals("jar")) {
                String path = classURL.getPath();
                int jarIndex = path.indexOf("!/");
                if (jarIndex == -1) {
                    throw new IllegalArgumentException("!/ not found " + path);
                }
                final String agentPath = path.substring("file:".length(), jarIndex);
                System.out.println("agentPath:" + agentPath);
                return agentPath;
            }
            // unknown
            return null;
        }

        private URL getResource(String internalClassName) {
            return ClassLoader.getSystemResource(internalClassName);
        }

    }

    @Deprecated
    static class InputArgumentAgentPathFinder implements AgentPathFinder {

        static final String JAVA_AGENT_OPTION = "-javaagent:";

        private final Pattern DEFAULT_AGENT_PATTERN = BOOT_JAR_DESC.getVersionPattern();

        @Override
        public String getPath() {
            final List<String> inputArguments = getInputArguments();
            for (String inputArgument : inputArguments) {
                if (isPinpointAgent(inputArgument, DEFAULT_AGENT_PATTERN)) {
                    String agentPath = removeJavaAgentPrefix(inputArgument);
                    System.out.println("agentPath:" + agentPath);
                    return agentPath;
                }
            }
            return null;
        }

        List<String> getInputArguments() {
            RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
            return runtimeMXBean.getInputArguments();
        }


        private boolean isPinpointAgent(String inputArgument, Pattern javaPattern) {
            if (!inputArgument.startsWith(JAVA_AGENT_OPTION)) {
                return false;
            }
            Matcher matcher = javaPattern.matcher(inputArgument);
            return matcher.find();
        }

        private String removeJavaAgentPrefix(String inputArgument) {
            return inputArgument.substring(JAVA_AGENT_OPTION.length(), inputArgument.length());
        }
    }


}
