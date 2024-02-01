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
import cn.polarismesh.agent.plugin.spring.cloud.configuration.AgentPolarisRateLimitProperties;
import com.tencent.cloud.common.metadata.StaticMetadataManager;
import com.tencent.cloud.common.metadata.config.MetadataLocalProperties;
import com.tencent.cloud.polaris.DiscoveryConfigModifier;
import com.tencent.cloud.polaris.PolarisDiscoveryConfigModifier;
import com.tencent.cloud.polaris.PolarisDiscoveryProperties;
import com.tencent.cloud.polaris.circuitbreaker.common.CircuitBreakerConfigModifier;
import com.tencent.cloud.polaris.config.ConfigurationModifier;
import com.tencent.cloud.polaris.config.config.PolarisConfigProperties;
import com.tencent.cloud.polaris.context.ModifyAddress;
import com.tencent.cloud.polaris.context.PolarisConfigModifier;
import com.tencent.cloud.polaris.context.PolarisSDKContextManager;
import com.tencent.cloud.polaris.context.ServiceRuleManager;
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
import java.util.Objects;
import java.util.Properties;

import static com.tencent.cloud.polaris.extend.nacos.NacosContextProperties.DEFAULT_GROUP;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class Holder {

    private static final String GROUP_SERVER_ID_FORMAT = "%s__%s";
    private static final String NACOS_CLUSTER = "nacos.cluster";

    private static PolarisDiscoveryProperties discoveryProperties;

    private static ConsulContextProperties consulContextProperties;

    private static NacosContextProperties nacosContextProperties;

    private static MetadataLocalProperties localProperties;

    private static StaticMetadataManager staticMetadataManager;

    private static PolarisContextProperties polarisContextProperties;

    private static PolarisRuleBasedRouterProperties routerProperties;

    private static PolarisNearByRouterProperties nearByRouterProperties;

    private static PolarisMetadataRouterProperties metadataRouterProperties;

    private static AgentPolarisRateLimitProperties rateLimitProperties;

    private static PolarisConfigProperties polarisConfigProperties;

    private static PolarisStatProperties polarisStatProperties;

    private static RpcEnhancementReporterProperties rpcEnhancementReporterProperties;

    private static String CONF_FILE_PATH;

    private static boolean allowDiscovery = true;

    private static PolarisSDKContextManager contextManager;

    private static Environment environment;

    private static void initProperties() {
        polarisContextProperties = new PolarisContextProperties();
        discoveryProperties = new PolarisDiscoveryProperties();
        consulContextProperties = new ConsulContextProperties();
        nacosContextProperties = new NacosContextProperties();
        localProperties = new MetadataLocalProperties();
        routerProperties = new PolarisRuleBasedRouterProperties();
        nearByRouterProperties = new PolarisNearByRouterProperties();
        metadataRouterProperties = new PolarisMetadataRouterProperties();
        rateLimitProperties = new AgentPolarisRateLimitProperties();
        polarisConfigProperties = new PolarisConfigProperties();
        polarisStatProperties = new PolarisStatProperties();
        rpcEnhancementReporterProperties = new RpcEnhancementReporterProperties();

        discoveryProperties.setService(Holder.getLocalService());
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

            allowDiscovery = environment.getProperty("spring.cloud.discovery.enabled", Boolean.class, true);
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

            // rpc 调用增强
            bindObject("spring.cloud.tencent.rpc-enhancement.reporter", rpcEnhancementReporterProperties, environment);

            runConfigModifiers(environment);
        } catch (Throwable ex) {
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

        String confPath = Paths.get(CONF_FILE_PATH, "plugin", "spring-cloud-2021", "application.properties").toString();
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

        String logConfig = "";
        try {
            Class.forName(PolarisLogging.LOGBACK_CLASSIC_LOGGER);
            logConfig = Holder.class.getClassLoader().getResource("polaris-logback.xml").getFile();
        } catch (ClassNotFoundException e) {
            try {
                Class.forName(PolarisLogging.LOG4J2_CLASSIC_LOGGER);
                logConfig = Holder.class.getClassLoader().getResource("polaris-log4j2.xml").getFile();
            } catch (ClassNotFoundException e1) {
                logConfig = Holder.class.getClassLoader().getResource("polaris-log4j.xml").getFile();
            }
        }
        System.setProperty(LoggingConsts.LOGGING_CONFIG_PROPERTY, logConfig);

        if (StringUtils.isBlank(polarisContextProperties.getLocalIpAddress())) {
            polarisContextProperties.setLocalIpAddress(environment.getProperty("spring.cloud.client.ip-address"));
        }

        List<PolarisConfigModifier> modifiers = new ArrayList<>(Arrays.asList(
                new ModifyAddress(polarisContextProperties),
                new DiscoveryConfigModifier(discoveryProperties),
                new PolarisDiscoveryConfigModifier(discoveryProperties),
                new RateLimitConfigModifier(rateLimitProperties),
                new StatConfigModifier(polarisStatProperties, environment),
                new CircuitBreakerConfigModifier(rpcEnhancementReporterProperties)
        ));
        if (consulContextProperties.isEnabled()) {
            modifiers.add(new ConsulConfigModifier(consulContextProperties));
        }
        if (nacosContextProperties.isEnabled()) {
            modifiers.add(new NacosConfigModifier(nacosContextProperties));
        }
        if (polarisConfigProperties.isEnabled()) {
            modifiers.add(new ConfigurationModifier(polarisConfigProperties, polarisContextProperties));
        }

        contextManager = new PolarisSDKContextManager(polarisContextProperties, environment, modifiers);
        contextManager.init();
    }

    private static String getLocalService() {
        String serviceId = "";
        if (Objects.isNull(nacosContextProperties)) {
            serviceId = discoveryProperties.getService();
        } else {
            String group = nacosContextProperties.getGroup();
            if (org.apache.commons.lang.StringUtils.isNotBlank(group) && !DEFAULT_GROUP.equals(group)) {
                serviceId = String.format(GROUP_SERVER_ID_FORMAT, group, discoveryProperties.getService());
            } else {
                serviceId = discoveryProperties.getService();
            }
        }
        return serviceId;
    }

    public static Environment getEnvironment() {
        return environment;
    }

    public static PolarisStatProperties getPolarisStatProperties() {
        return polarisStatProperties;
    }

    public static PolarisSDKContextManager getContextManager() {
        return contextManager;
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

    public static RpcEnhancementReporterProperties getRpcEnhancementReporterProperties() {
        return rpcEnhancementReporterProperties;
    }

    public static ServiceRuleManager newServiceRuleManager() {
        return new ServiceRuleManager(getContextManager().getSDKContext(), getContextManager().getConsumerAPI());
    }

    public static boolean isAllowDiscovery() {
        return allowDiscovery;
    }
}
