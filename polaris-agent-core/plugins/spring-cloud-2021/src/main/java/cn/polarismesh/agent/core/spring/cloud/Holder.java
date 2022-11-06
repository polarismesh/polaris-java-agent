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

package cn.polarismesh.agent.core.spring.cloud;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import cn.polarismesh.agent.common.config.InternalConfig;
import cn.polarismesh.agent.common.exception.PolarisAgentException;
import cn.polarismesh.agent.core.spring.cloud.configuration.AgentPolarisRateLimitProperties;
import cn.polarismesh.agent.core.spring.cloud.util.PolarisSingleton;
import com.tencent.cloud.common.metadata.StaticMetadataManager;
import com.tencent.cloud.common.metadata.config.MetadataLocalProperties;
import com.tencent.cloud.plugin.pushgateway.PolarisStatPushGatewayModifier;
import com.tencent.cloud.plugin.pushgateway.PolarisStatPushGatewayProperties;
import com.tencent.cloud.polaris.DiscoveryConfigModifier;
import com.tencent.cloud.polaris.PolarisDiscoveryConfigModifier;
import com.tencent.cloud.polaris.PolarisDiscoveryProperties;
import com.tencent.cloud.polaris.circuitbreaker.config.PolarisCircuitBreakerAutoConfiguration;
import com.tencent.cloud.polaris.config.ConfigurationModifier;
import com.tencent.cloud.polaris.config.config.PolarisConfigProperties;
import com.tencent.cloud.polaris.context.ModifyAddress;
import com.tencent.cloud.polaris.context.PolarisConfigModifier;
import com.tencent.cloud.polaris.context.config.PolarisContextProperties;
import com.tencent.cloud.polaris.extend.consul.ConsulConfigModifier;
import com.tencent.cloud.polaris.extend.consul.ConsulContextProperties;
import com.tencent.cloud.polaris.extend.nacos.NacosConfigModifier;
import com.tencent.cloud.polaris.extend.nacos.NacosContextProperties;
import com.tencent.cloud.polaris.ratelimit.config.RateLimitConfigModifier;
import com.tencent.cloud.polaris.router.config.properties.PolarisMetadataRouterProperties;
import com.tencent.cloud.polaris.router.config.properties.PolarisNearByRouterProperties;
import com.tencent.cloud.polaris.router.config.properties.PolarisRuleBasedRouterProperties;
import com.tencent.cloud.rpc.enhancement.config.RpcEnhancementReporterProperties;
import com.tencent.cloud.rpc.enhancement.stat.config.PolarisStatProperties;
import com.tencent.cloud.rpc.enhancement.stat.config.StatConfigModifier;
import com.tencent.polaris.api.utils.StringUtils;
import com.tencent.polaris.factory.ConfigAPIFactory;
import com.tencent.polaris.factory.config.ConfigurationImpl;

import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.cloud.client.HostInfoEnvironmentPostProcessor;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.util.CollectionUtils;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class Holder {

	private static StandardEnvironment environment = new StandardEnvironment();

	private static PolarisDiscoveryProperties discoveryProperties = new PolarisDiscoveryProperties();

	private static ConsulContextProperties consulContextProperties = new ConsulContextProperties();

	private static NacosContextProperties nacosContextProperties = new NacosContextProperties();

	private static MetadataLocalProperties localProperties = new MetadataLocalProperties();

	private static StaticMetadataManager staticMetadataManager;

	private static PolarisContextProperties polarisContextProperties = new PolarisContextProperties();

	private static PolarisRuleBasedRouterProperties routerProperties = new PolarisRuleBasedRouterProperties();

	private static PolarisNearByRouterProperties nearByRouterProperties = new PolarisNearByRouterProperties();

	private static PolarisMetadataRouterProperties metadataRouterProperties = new PolarisMetadataRouterProperties();

	private static AgentPolarisRateLimitProperties rateLimitProperties = new AgentPolarisRateLimitProperties();

	private static PolarisConfigProperties polarisConfigProperties = new PolarisConfigProperties();

	private static PolarisStatProperties polarisStatProperties = new PolarisStatProperties();

	private static PolarisStatPushGatewayProperties polarisStatPushGatewayProperties = new PolarisStatPushGatewayProperties();

	private static RpcEnhancementReporterProperties rpcEnhancementReporterProperties = new RpcEnhancementReporterProperties();

	public static void init() {

		System.setProperty("polaris.logging.config",
				Paths.get(System.getProperty(InternalConfig.INTERNAL_KEY_AGENT_DIR), "polaris", "conf").toString());

		try (InetUtils utils = new InetUtils(new InetUtilsProperties())) {
			polarisContextProperties.setLocalIpAddress(utils.findFirstNonLoopbackHostInfo().getIpAddress());
			// 读取 application.yaml
			buildEnv();

			// sct 本身的额外的配饰信息
			bindObject("spring.cloud.tencent.metadata", localProperties, environment);
			bindObject("spring.cloud.polaris", polarisContextProperties, environment);
			staticMetadataManager  = new StaticMetadataManager(localProperties, null);

			// 服务发现配置
			discoveryProperties.setRegisterEnabled(environment.getProperty("spring.cloud.polaris.discovery.register", Boolean.class, true));
			discoveryProperties.setProtocol(environment.getProperty("spring.cloud.polaris.discovery.protocol", String.class, "http"));
			discoveryProperties.setService(environment.getProperty("spring.application.name", String.class));
			discoveryProperties.setWeight(environment.getProperty("spring.cloud.polaris.discovery.weight", Integer.class, 100));
			String namespace = environment.getProperty("spring.cloud.polaris.namespace", String.class);
			if (StringUtils.isBlank(namespace)) {
				namespace = environment.getProperty("spring.cloud.polaris.discovery.namespace", String.class, "default");
			}
			discoveryProperties.setNamespace(namespace);

			bindObject("spring.cloud.consul", consulContextProperties, environment);
			bindObject("spring.cloud.nacos.discovery", nacosContextProperties, environment);

			// 路由规则配置
			bindObject("spring.cloud.polaris.router.rule-router", routerProperties, environment);
			bindObject("spring.cloud.polaris.router.nearby-router", nearByRouterProperties, environment);
			bindObject("spring.cloud.polaris.router.metadata-router", metadataRouterProperties, environment);

			// 限流规则配置
			bindObject("spring.cloud.polaris.ratelimit", rateLimitProperties, environment);

			// 配置中心
			bindObject("spring.cloud.polaris.config", polarisConfigProperties, environment);

			// 监控
			bindObject("spring.cloud.polaris.stat", polarisStatProperties, environment);
			bindObject("spring.cloud.polaris.stat.pushgateway", polarisStatPushGatewayProperties, environment);

			// rpc 调用增强
			bindObject("spring.cloud.tencent.rpc-enhancement.reporter", rpcEnhancementReporterProperties, environment);

			runConfigModifiers(environment);

		} catch (Throwable ex) {
			throw new PolarisAgentException(ex);
		}
	}

	private static Environment buildEnv() throws Exception {
		HostInfoEnvironmentPostProcessor processor = new HostInfoEnvironmentPostProcessor();
		processor.postProcessEnvironment(environment, null);

		String agentDir = System.getProperty(InternalConfig.INTERNAL_KEY_AGENT_DIR);

		Properties defaultProperties = new Properties();
		defaultProperties.load(Files.newInputStream(Paths.get(agentDir, "polaris", "conf", "default-application.config").toFile().toPath()));
		environment.getPropertySources().addFirst(new PropertiesPropertySource("__default_polaris_agent_spring_cloud_tencent__", defaultProperties));

		Properties properties = new Properties();
		String filePath = System.getProperty(InternalConfig.USER_APPLICATION_FILE);
		if (StringUtils.isBlank(filePath)) {
			filePath = Paths.get(agentDir, "application.config").toString();
		}

		properties.load(Files.newInputStream(Paths.get(filePath).toFile().toPath()));
		environment.getPropertySources().addFirst(new PropertiesPropertySource("__polaris_agent_spring_cloud_tencent__", properties));

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

		List<PolarisConfigModifier> modifiers = Arrays.asList(
				new ModifyAddress(polarisContextProperties),
				new DiscoveryConfigModifier(discoveryProperties),
				new PolarisDiscoveryConfigModifier(discoveryProperties),
				new ConsulConfigModifier(consulContextProperties),
				new NacosConfigModifier(nacosContextProperties),
				new RateLimitConfigModifier(rateLimitProperties),
				new ConfigurationModifier(polarisConfigProperties, polarisContextProperties),
				new StatConfigModifier(polarisStatProperties, environment),
				new PolarisStatPushGatewayModifier(polarisStatPushGatewayProperties, environment),
				new PolarisCircuitBreakerAutoConfiguration.CircuitBreakerConfigModifier(rpcEnhancementReporterProperties)
		);

		String polarisConfigFile = System.getProperty(InternalConfig.INTERNAL_KEY_AGENT_DIR) + File.separator + PolarisReflectConst.POLARIS_CONF_DIR + File.separator
				+ PolarisReflectConst.POLARIS_CONF_FILE;
		InputStream inputStream = Files.newInputStream(Paths.get(polarisConfigFile));
		ConfigurationImpl configuration = (ConfigurationImpl) ConfigAPIFactory.loadConfig(inputStream);
		configuration.getGlobal().getAPI().setBindIP(polarisContextProperties.getLocalIpAddress());

		modifiers = modifiers.stream()
				.sorted(Comparator.comparingInt(PolarisConfigModifier::getOrder))
				.collect(Collectors.toList());
		if (!CollectionUtils.isEmpty(modifiers)) {
			for (PolarisConfigModifier modifier : modifiers) {
				modifier.modify(configuration);
			}
		}

		PolarisSingleton.init(configuration);
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

	public static PolarisDiscoveryProperties getDiscoveryProperties() {
		return discoveryProperties;
	}

	public static PolarisRuleBasedRouterProperties getRouterProperties() {
		return routerProperties;
	}

	public static PolarisNearByRouterProperties getNearByRouterProperties() {
		return nearByRouterProperties;
	}

	public static PolarisMetadataRouterProperties getMetadataRouterProperties() {
		return metadataRouterProperties;
	}

	public static AgentPolarisRateLimitProperties getRateLimitProperties() {
		return rateLimitProperties;
	}

	public static ConsulContextProperties getConsulContextProperties() {
		return consulContextProperties;
	}

	public static NacosContextProperties getNacosContextProperties() {
		return nacosContextProperties;
	}

}
