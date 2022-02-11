package cn.polarismesh.agent.pinpoint.dubbo2.Interceptor;

import cn.polarismesh.agent.plugin.dubbo2.interceptor.AbstractInterceptor;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

class InterceptorFactory {
    private static final Map<Class<?>, AbstractInterceptor> interceptorCache = new HashMap<>();

    static {
        addURLClassloader();
        interceptorCache.put(DubboInvokerInterceptor.class, new cn.polarismesh.agent.plugin.dubbo2.interceptor.DubboInvokerInterceptor());
        interceptorCache.put(DubboProviderInterceptor.class, new cn.polarismesh.agent.plugin.dubbo2.interceptor.DubboProviderInterceptor());
        interceptorCache.put(DubboClusterInvokerInterceptor.class, new cn.polarismesh.agent.plugin.dubbo2.interceptor.DubboClusterInvokerInterceptor());
        interceptorCache.put(DubboInvokeInterceptor.class, new cn.polarismesh.agent.plugin.dubbo2.interceptor.DubboInvokeInterceptor());
        interceptorCache.put(DubboLoadBalanceInterceptor.class, new cn.polarismesh.agent.plugin.dubbo2.interceptor.DubboLoadBalanceInterceptor());
    }

    /**
     * 将/lib/polaris下的jar包添加到URLClassLoader的搜索路径下
     */
    private static void addURLClassloader() {
        URLClassLoader classLoader = (URLClassLoader) InterceptorFactory.class.getClassLoader();
        Method addURL = initAddMethod();
        String agentLibPath = getLibPath();
        File file = new File(agentLibPath);
        File[] fileArray = file.listFiles();
        for (File f : Objects.requireNonNull(fileArray)) {
            String libFilePath = f.getAbsolutePath();
            try {
                URL jarURL = new URL("file:" + libFilePath);
                addURL.invoke(classLoader, jarURL);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 反射获取URLClassLoader的addURL方法
     *
     * @return void addURL(URL url)
     */
    private static Method initAddMethod() {
        try {
            Method add = URLClassLoader.class.getDeclaredMethod("addURL", java.net.URL.class);
            add.setAccessible(true);
            return add;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取/lib/polaris路径
     *
     * @return /lib/polaris路径
     */
    private static String getLibPath() {
        List<String> inputArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
        for (String argument : inputArguments) {
            if (argument.startsWith("-javaagent")) {
                //-javaagent:xxx\xxx\xxx.jar
                String targetDir = argument.replaceFirst("-javaagent:", "").replaceFirst("pinpoint-bootstrap.jar", "");
                return targetDir + "/lib/polaris";
            }
        }
        return "";
    }

    static AbstractInterceptor getInterceptor(Class<?> clazz) {
        return interceptorCache.get(clazz);
    }
}
