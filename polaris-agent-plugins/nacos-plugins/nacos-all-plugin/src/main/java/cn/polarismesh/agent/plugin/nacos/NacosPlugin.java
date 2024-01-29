package cn.polarismesh.agent.plugin.nacos;

import cn.polarismesh.agent.core.extension.instrument.exception.InstrumentException;
import cn.polarismesh.agent.core.common.logger.CommonLogger;
import cn.polarismesh.agent.core.common.logger.StdoutCommonLoggerFactory;
import cn.polarismesh.agent.core.extension.AgentPlugin;
import cn.polarismesh.agent.core.extension.PluginContext;
import cn.polarismesh.agent.core.extension.instrument.InstrumentClass;
import cn.polarismesh.agent.core.extension.instrument.InstrumentMethod;
import cn.polarismesh.agent.core.extension.instrument.Instrumentor;
import cn.polarismesh.agent.core.extension.transform.TransformCallback;
import cn.polarismesh.agent.core.extension.transform.TransformOperations;
import cn.polarismesh.agent.plugin.nacos.constants.NacosConstants;
import cn.polarismesh.agent.plugin.nacos.interceptor.NamingFactoryCreateNamingServiceInterceptor;
import cn.polarismesh.agent.plugin.nacos.route.NearbyLevel;
import cn.polarismesh.agent.plugin.nacos.utils.StringUtils;

import java.security.ProtectionDomain;
import java.util.Objects;

public class NacosPlugin implements AgentPlugin {
    private static final CommonLogger logger = StdoutCommonLoggerFactory.INSTANCE
            .getLogger(NacosPlugin.class.getCanonicalName());

    @Override
    public void init(PluginContext pluginContext) {
        logger.info("nacos plugin initializing");
        checkParams();
        TransformOperations transformTemplate = pluginContext.getTransformOperations();
        addTransformers(transformTemplate);
    }

    public void checkParams() {
        String otherAddr = System.getProperty(NacosConstants.OTHER_NACOS_SERVER_ADDR);
        if (StringUtils.isBlank(otherAddr)) {
            throw new IllegalArgumentException(NacosConstants.OTHER_NACOS_SERVER_ADDR + " can not empty");
        }
        String routerNearbyLevel = System.getProperty(NacosConstants.ROUTER_NEARBY_LEVEL);

        if (Objects.isNull(routerNearbyLevel)) {
            return;
        }

        if (NearbyLevel.NACOS_CLUSTER.name().equals(routerNearbyLevel.toUpperCase())) {
            // 开启了就近路由必须指定 nacos.cluster.name
            String nacosClusterName = System.getProperty(NacosConstants.NACOS_CLUSTER_NAME);
            if (StringUtils.isBlank(nacosClusterName)) {
                throw new IllegalArgumentException(NacosConstants.OTHER_NACOS_SERVER_ADDR + " can not empty");
            }
        }

    }

    private void addTransformers(TransformOperations transformTemplate) {
        transformTemplate.transform(NacosConstants.NACOS_NAMING_FACTORY, NamingFactoryTransform.class);

    }

    public static class NamingFactoryTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            InstrumentMethod method = target.getDeclaredMethod("createNamingService", "java.util.Properties");
            if (method != null) {

                method.addInterceptor(NamingFactoryCreateNamingServiceInterceptor.class);
            }
            return target.toBytecode();
        }
    }

}