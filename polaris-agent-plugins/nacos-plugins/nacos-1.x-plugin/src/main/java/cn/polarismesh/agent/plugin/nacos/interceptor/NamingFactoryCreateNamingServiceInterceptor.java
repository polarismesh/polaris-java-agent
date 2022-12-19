package cn.polarismesh.agent.plugin.nacos.interceptor;

import cn.polarismesh.agent.core.common.utils.ReflectionUtils;
import cn.polarismesh.agent.core.extension.interceptor.Interceptor;
import cn.polarismesh.agent.plugin.nacos.asm.AsmDynamicClassLoader;
import cn.polarismesh.agent.plugin.nacos.constants.NacosConstants;

import java.util.Properties;

public class NamingFactoryCreateNamingServiceInterceptor implements Interceptor {
    @Override
    public void before(Object target, Object[] args) {

    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {

        try {
            //构造NacosNamingProxy对象
            Properties properties = (Properties) args[0];
            String namespace = (String) ReflectionUtils.getObjectByFieldName(result, NacosConstants.NAMESPACE);
            String endpoint = (String) ReflectionUtils.getObjectByFieldName(result, NacosConstants.ENDPOINT);
            String serverList = (String) ReflectionUtils.getObjectByFieldName(result, NacosConstants.SERVER_LIST);
            //String namespaceId, String endpoint, String serverList, Properties properties

            ClassLoader classLoader = Class.forName("com.alibaba.nacos.client.naming.net.NamingProxy").getClassLoader();
            Class<?> aClass = new AsmDynamicClassLoader(classLoader).loadClass(NacosConstants.NAMING_PROXY);
            Object nacosNamingProxy = aClass.getConstructors()[0].newInstance(namespace, endpoint, serverList, properties);
//            Object nacosNamingProxy = Class.forName(NacosConstants.NAMING_PROXY).getConstructors()[0].newInstance(namespace, endpoint, serverList, properties);
//        Object nacosNamingProxy = new NacosNamingProxy(namespace, endpoint, serverList, properties);
//            nacosNamingProxy = new DynamicNamingProxyAfter130(namespace, endpoint, serverList, properties);

            // 强行替换 BeatReactor 的 serverProxy 为 nacosNamingProxy
            Object beatReactor = ReflectionUtils.getObjectByFieldName(result, NacosConstants.BEAT_REACTOR);
            ReflectionUtils.setValueByFieldName(beatReactor, NacosConstants.SERVER_PROXY, nacosNamingProxy);

            // 强行替换 HostReactor 的 serverProxy 为 nacosNamingProxy
            Object hostReactor = ReflectionUtils.getObjectByFieldName(result, NacosConstants.HOST_REACTOR);
            ReflectionUtils.setValueByFieldName(hostReactor, NacosConstants.SERVER_PROXY, nacosNamingProxy);

            // 强行替换 NacosNamingService 的 serverProxy 为 nacosNamingProxy
            ReflectionUtils.setValueByFieldName(result, NacosConstants.SERVER_PROXY, nacosNamingProxy);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
