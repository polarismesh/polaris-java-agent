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

package cn.polarismesh.agent.plugin.spring.cloud.common;

import cn.polarismesh.agent.core.common.exception.PolarisAgentException;
import com.tencent.cloud.common.metadata.StaticMetadataManager;
import com.tencent.cloud.common.metadata.config.MetadataLocalProperties;
import com.tencent.cloud.plugin.lossless.config.LosslessConfigModifier;
import com.tencent.cloud.plugin.lossless.config.LosslessProperties;
import com.tencent.cloud.polaris.context.ModifyAddress;
import com.tencent.cloud.polaris.context.PolarisConfigModifier;
import com.tencent.cloud.polaris.context.PolarisSDKContextManager;
import com.tencent.cloud.polaris.context.config.PolarisContextProperties;
import com.tencent.polaris.api.utils.StringUtils;
import com.tencent.polaris.logging.LoggingConsts;
import com.tencent.polaris.logging.PolarisLogging;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.cloud.client.HostInfoEnvironmentPostProcessor;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class Holder {
	private static MetadataLocalProperties localProperties;

	private static StaticMetadataManager staticMetadataManager;

	private static PolarisContextProperties polarisContextProperties;

	private static LosslessProperties losslessProperties;

	private static Environment environment;

	private static String CONF_FILE_PATH;

	private static boolean allowDiscovery = true;

	private static PolarisSDKContextManager contextManager;

	private static void initProperties() {
		polarisContextProperties = new PolarisContextProperties();
		localProperties = new MetadataLocalProperties();
		losslessProperties = new LosslessProperties();
	}

	public static void init() {
		CONF_FILE_PATH = Paths.get(System.getProperty(Constant.AGENT_CONF_PATH), "conf").toString();
		initProperties();
		try (InetUtils utils = new InetUtils(new InetUtilsProperties())) {
			polarisContextProperties.setLocalIpAddress(utils.findFirstNonLoopbackHostInfo().getIpAddress());
			// 读取 application.yaml
			environment = buildEnv();

			// sct 本身的额外的配饰信息
			bindObject("spring.cloud.tencent.metadata", localProperties, environment);
			bindObject("spring.cloud.polaris", polarisContextProperties, environment);
			staticMetadataManager = new StaticMetadataManager(localProperties, null);

			// lossless
			bindObject("spring.cloud.polaris.lossless", losslessProperties, environment);

			runConfigModifiers(environment);
		}
		catch (Throwable ex) {
			throw new PolarisAgentException(ex);
		}
	}

	private static Environment buildEnv() throws Exception {
		StandardEnvironment environment = new StandardEnvironment();
		HostInfoEnvironmentPostProcessor processor = new HostInfoEnvironmentPostProcessor();
		processor.postProcessEnvironment(environment, null);

		InputStream stream = Holder.class.getClassLoader().getResourceAsStream("default-plugin.conf");
		Properties defaultProperties = new Properties();
		defaultProperties.load(stream);
		environment.getPropertySources()
				.addFirst(new PropertiesPropertySource("__default_polaris_agent_spring_cloud_tencent__", defaultProperties));

		Properties properties = new Properties();

		String confPath = Paths.get(CONF_FILE_PATH, "plugin", "spring-cloud-hoxton", "application.properties").toString();
		String cmdVal = System.getProperty("polaris.agent.user.application.conf");
		if (StringUtils.isNotBlank(cmdVal)) {
			confPath = cmdVal;
		}

		properties.load(Files.newInputStream(Paths.get(confPath).toFile().toPath()));
		environment.getPropertySources()
				.addFirst(new PropertiesPropertySource("__polaris_agent_spring_cloud_tencent__", properties));

		return environment;
	}

	private static void bindObject(String prefix, Object bean, Environment environment) {
		Binder binder = Binder.get(environment);
		ResolvableType type = ResolvableType.forClass(bean.getClass());
		Bindable<?> target = Bindable.of(type).withExistingValue(bean);
		binder.bind(prefix, target);
	}

	private static void runConfigModifiers(Environment environment) throws IOException {

		if (StringUtils.isBlank(polarisContextProperties.getLocalIpAddress())) {
			polarisContextProperties.setLocalIpAddress(environment.getProperty("spring.cloud.client.ip-address"));
		}

		List<PolarisConfigModifier> modifiers = new ArrayList<>(Arrays.asList(
				new ModifyAddress(polarisContextProperties),
				new LosslessConfigModifier(losslessProperties)
		));

		contextManager = new PolarisSDKContextManager(polarisContextProperties, environment, modifiers);
		contextManager.init();
	}

	public static Environment getEnvironment() {
		return environment;
	}

	public static MetadataLocalProperties getLocalProperties() {
		return localProperties;
	}

	public static StaticMetadataManager getStaticMetadataManager() {
		return staticMetadataManager;
	}

	public static PolarisContextProperties getPolarisContextProperties() {
		return polarisContextProperties;
	}

	public static LosslessProperties getLosslessProperties() {
		return losslessProperties;
	}

	public static PolarisSDKContextManager getContextManager() {
		return contextManager;
	}

	public static boolean isAllowDiscovery() {
		return allowDiscovery;
	}
}
