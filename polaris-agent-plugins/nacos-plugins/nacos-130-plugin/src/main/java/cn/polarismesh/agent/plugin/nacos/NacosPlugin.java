package cn.polarismesh.agent.plugin.nacos;

import cn.polarismesh.agent.core.common.exception.InstrumentException;
import cn.polarismesh.agent.core.extension.AgentPlugin;
import cn.polarismesh.agent.core.extension.PluginContext;
import cn.polarismesh.agent.core.extension.instrument.InstrumentClass;
import cn.polarismesh.agent.core.extension.instrument.InstrumentMethod;
import cn.polarismesh.agent.core.extension.instrument.Instrumentor;
import cn.polarismesh.agent.core.extension.transform.TransformCallback;
import cn.polarismesh.agent.core.extension.transform.TransformOperations;
import cn.polarismesh.agent.plugin.nacos.interceptor.NacosNamingFactoryInterceptor;
import java.security.ProtectionDomain;

/**
 * nacos 1.3.0 Plugin
 *
 * @author bruceppeng
 */

public class NacosPlugin implements AgentPlugin {

    //private static final Logger LOGGER = LoggerFactory.getLogger(NacosPlugin.class);

    @Override
    public void init(PluginContext pluginContext) {
        TransformOperations transformTemplate = pluginContext.getTransformOperations();
        addTransformers(transformTemplate);
    }

    private void addTransformers(TransformOperations transformTemplate) {
        transformTemplate.transform(ClassNames.NACOS_NAMING_FACTORY, NacosNamingFactoryTransform.class);

    }
    public static class NacosNamingFactoryTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
                Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                byte[] classfileBuffer) throws InstrumentException {

            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            InstrumentMethod method = target.getDeclaredMethod("createNamingService", "java.util.Properties");
            if (method != null) {
                method.addInterceptor(NacosNamingFactoryInterceptor.class);
            }

            return target.toBytecode();
        }

    }

    public interface ClassNames {

        String NACOS_NAMING_FACTORY = "com.alibaba.nacos.api.naming.NamingFactory";

    }
}