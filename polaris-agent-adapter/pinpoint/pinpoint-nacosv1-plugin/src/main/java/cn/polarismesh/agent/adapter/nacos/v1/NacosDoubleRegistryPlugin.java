package cn.polarismesh.agent.adapter.nacos.v1;


import cn.polarismesh.agent.adapter.nacos.v1.interceptor.NacosNamingFactoryInterceptor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;

import java.security.ProtectionDomain;

/**
 * nacos v1 double registry Plugin
 *
 * @author bruceppeng
 */
public class NacosDoubleRegistryPlugin implements ProfilerPlugin, TransformTemplateAware {

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        addNacosTransformers();
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

    /**
     * add nacos transformers
     */
    private void addNacosTransformers() {
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