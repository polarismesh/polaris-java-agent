package cn.polarismesh.agent.plugin.nacos.interceptor;

import cn.polarismesh.agent.core.common.utils.ReflectionUtils;
import cn.polarismesh.agent.core.extension.interceptor.Interceptor;
import cn.polarismesh.agent.plugin.nacos.asm.AsmDynamicClassLoader;
import cn.polarismesh.agent.plugin.nacos.constants.NacosConstants;
import cn.polarismesh.agent.plugin.nacos.exception.UnsupportedNacosClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

public class NamingFactoryCreateNamingServiceInterceptor implements Interceptor {
    Logger logger = LoggerFactory.getLogger(NamingFactoryCreateNamingServiceInterceptor.class);

    @Override
    public void before(Object target, Object[] args) {

    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (result == null) {
            return;
        }

        try {
            // 如果可以获取 NamingClientProxy 类信息,则认为是 2.x 版本
            Class.forName("com.alibaba.nacos.client.naming.remote.NamingClientProxy");
            proxy2x(target, args, result, throwable);
            return;
        } catch (ClassNotFoundException e) {
            try {
                // 如果可以获取 NamingProxy 类信息,则认为是 1.x 版本
                Class.forName("com.alibaba.nacos.client.naming.net.NamingProxy");
                proxy1x(target, args, result, throwable);
                return;
            } catch (ClassNotFoundException ex) {

            }

        }
        throw new UnsupportedNacosClientException();

    }

    private void proxy1x(Object target, Object[] args, Object result, Throwable throwable) {
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

            // 强行替换 BeatReactor 的 serverProxy 为 nacosNamingProxy
            Object beatReactor = ReflectionUtils.getObjectByFieldName(result, NacosConstants.BEAT_REACTOR);
            ReflectionUtils.setValueByFieldName(beatReactor, NacosConstants.SERVER_PROXY, nacosNamingProxy);

            // 强行替换 HostReactor 的 serverProxy 为 nacosNamingProxy
            Object hostReactor = ReflectionUtils.getObjectByFieldName(result, NacosConstants.HOST_REACTOR);
            ReflectionUtils.setValueByFieldName(hostReactor, NacosConstants.SERVER_PROXY, nacosNamingProxy);

            // 强行替换 NacosNamingService 的 serverProxy 为 nacosNamingProxy
            ReflectionUtils.setValueByFieldName(result, NacosConstants.SERVER_PROXY, nacosNamingProxy);
        } catch (Exception e) {
            logger.error("nacos plugin run failed:", e);
            throw new UnsupportedNacosClientException();
        }
    }

    private void proxy2x(Object target, Object[] args, Object result, Throwable throwable) {

        try {
            Properties properties = (Properties) args[0];
            String namespace = (String) ReflectionUtils.getObjectByFieldName(result, NacosConstants.NAMESPACE);
            Object serviceInfoHolder = ReflectionUtils.getObjectByFieldName(result, NacosConstants.SERVICE_INFO_HOLDER);
            Object changeNotifier = ReflectionUtils.getObjectByFieldName(result, NacosConstants.CHANGE_NOTIFIER);
            ClassLoader classLoader = Class.forName("com.alibaba.nacos.client.naming.remote.NamingClientProxy").getClassLoader();
            Class<?> aClass = new AsmDynamicClassLoader(classLoader).loadClass(NacosConstants.NAMING_CLIENT_PROXY);
            Object clientProxy = aClass.getConstructors()[0].newInstance(namespace, serviceInfoHolder, properties, changeNotifier);

            //给nacosNamingService对象重新设置属性HostReactor对象
            ReflectionUtils.setValueByFieldName(result, NacosConstants.CLIENT_PROXY, clientProxy);

        } catch (Exception e) {
            logger.error("nacos plugin run failed:", e);
            throw new UnsupportedNacosClientException();
        }
    }
}
