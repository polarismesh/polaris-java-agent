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

package cn.polarismesh.agent.bootstrap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.instrument.Instrumentation;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.jar.JarFile;

import cn.polarismesh.agent.bootstrap.extension.BootStrapStarter;
import cn.polarismesh.agent.bootstrap.util.AgentDirUtils;
import cn.polarismesh.agent.bootstrap.util.PropertyUtils;
import cn.polarismesh.agent.common.config.InternalConfig;
import cn.polarismesh.agent.common.exception.PolarisAgentException;

public class PolarisAgentBootStrap {

	private static final BootLogger logger = BootLogger.getLogger(PolarisAgentBootStrap.class);

	public static final String CONFIG_FILE_NAME = "application.config";

	private static final String POLARIS_LIB_DIR = "polaris" + File.separator + "lib";

	private static final LoadState STATE = new LoadState();

	private static final String SEPARATOR = File.separator;

	static BootStrapStarter loadStarters() {
		ServiceLoader<BootStrapStarter> starters = ServiceLoader.load(BootStrapStarter.class);
		for (BootStrapStarter starter : starters) {
			return starter;
		}
		return null;
	}

	public static void agentmain(String agentArgs, Instrumentation instrumentation) {

	}

	public static void premain(String agentArgs, Instrumentation instrumentation) {
		final boolean success = STATE.start();
		if (!success) {
			logger.warn("[Bootstrap] polaris-bootstrap already started. skipping agent loading.");
			return;
		}

		logger.info("[Bootstrap] polaris-agent agentArgs:" + agentArgs);
		logger.info("[Bootstrap] polarisAgentBootStrap.ClassLoader:" + PolarisAgentBootStrap.class.getClassLoader());
		logger.info("[Bootstrap] contextClassLoader:" + Thread.currentThread().getContextClassLoader());

		final JavaAgentPathResolver javaAgentPathResolver = JavaAgentPathResolver.newJavaAgentPathResolver();
		final String agentPath = javaAgentPathResolver.resolveJavaAgentPath();
		logger.info("[Bootstrap] javaAgentPath:" + agentPath);
		if (agentPath == null) {
			logger.warn("[Bootstrap] agentPath not found path");
		}

		if (Object.class.getClassLoader() != PolarisAgentBootStrap.class.getClassLoader()) {
			logger.warn("[Bootstrap] invalid polaris-agent-bootstrap.jar:" + agentArgs);
			return;
		}

		String agentDirPath = AgentDirUtils.resolveAgentDir(agentPath);

		System.setProperty(InternalConfig.INTERNAL_KEY_AGENT_DIR, agentDirPath);
		System.setProperty(InternalConfig.INTERNAL_POLARIS_LOG_HOME,
				agentDirPath + File.separator + "polaris" + File.separator + "logs");

		instrumentPolarisDependencies(instrumentation, agentDirPath);

		// load starter
		BootStrapStarter starter = loadStarters();
		if (null == starter) {
			logger.warn("[Bootstrap] no starter found, exit agentmain");
			return;
		}
		logger.info(String.format("[Bootstrap] start bootStrapStarter:%s", starter.name()));
		// load polaris Properties
		String userConfigPath = agentDirPath + SEPARATOR + CONFIG_FILE_NAME;
		if (Objects.equals(userConfigPath, "")) {
			userConfigPath = System.getProperty(InternalConfig.USER_APPLICATION_FILE);
		}
		final Properties polarisProperties = loadFileProperties(userConfigPath);
		logger.info(String.format("[Bootstrap] load default config:%s, data : %s", userConfigPath, polarisProperties));
		starter.start(agentDirPath, polarisProperties, agentArgs, instrumentation);
	}

	public static void instrumentPolarisDependencies(Instrumentation instrumentation, String agentDir) {
		logger.info("[Bootstrap] start to instrumentation polaris dependencies");
		String libPath = agentDir + File.separator + POLARIS_LIB_DIR;
		File[] polarisDependencies = (new File(libPath)).listFiles();
		if (null == polarisDependencies || polarisDependencies.length == 0) {
			return;
		}
		for (File polarisDependency : polarisDependencies) {
			if (polarisDependency.getName().endsWith(".jar")) {
				logger.info(String.format("[Bootstrap] instrument polaris jar %s", polarisDependency));
				JarFile jarFile;
				try {
					jarFile = new JarFile(polarisDependency);
				}
				catch (IOException e) {
					logger.error(String.format("[Bootstrap] fail to parse file %s to jar: %s", polarisDependency,
							e.getMessage()));
					continue;
				}
				instrumentation.appendToSystemClassLoaderSearch(jarFile);
			}
		}
	}

	private static Properties loadFileProperties(String filePath) {
		try {
			Properties properties = new Properties();
			return PropertyUtils.loadProperty(properties, new PropertyUtils.FileInputStreamFactory(filePath));
		}
		catch (IOException e) {
            throw new RuntimeException(e);
		}
	}

}
