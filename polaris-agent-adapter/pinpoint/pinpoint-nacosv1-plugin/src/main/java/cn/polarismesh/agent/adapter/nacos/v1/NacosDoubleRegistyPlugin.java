package cn.polarismesh.agent.adapter.nacos.v1;


import cn.polarismesh.agent.adapter.nacos.v1.interceptor.NacosNamingProxyInterceptor;
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
public class NacosDoubleRegistyPlugin implements ProfilerPlugin, TransformTemplateAware {

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
        transformTemplate.transform(ClassNames.NACOS_NAMING_PROXY, NacosNamingProxyTransform.class);
//        transformTemplate.transform(ClassNames.NACOS_NAMING_SERVICE, NacosDoubleRegistryTransform.class);

    }

    public static class NacosNamingProxyTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
                Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                byte[] classfileBuffer) throws InstrumentException {

            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            InstrumentMethod method = target.getDeclaredMethod("reqApi", "java.lang.String", "java.util.Map", "java.util.Map", "java.util.List", "java.lang.String");
            if (method != null) {
                method.addInterceptor(NacosNamingProxyInterceptor.class);
            }

            return target.toBytecode();
        }

    }

    public interface ClassNames {

        String NACOS_NAMING_PROXY = "com.alibaba.nacos.client.naming.net.NamingProxy";
        String NACOS_NAMING_SERVICE = "com.alibaba.nacos.client.naming.NacosNamingService.NacosNamingService";

    }

}