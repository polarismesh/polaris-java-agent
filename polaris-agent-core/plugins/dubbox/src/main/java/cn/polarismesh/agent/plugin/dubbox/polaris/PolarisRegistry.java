package cn.polarismesh.agent.plugin.dubbox.polaris;

import static com.alibaba.dubbo.common.Constants.PATH_KEY;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.registry.NotifyListener;
import com.alibaba.dubbo.registry.support.FailbackRegistry;
import com.alibaba.dubbo.rpc.Protocol;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 服务注册中心，提供服务注册，服务发现相关功能
 */
public class PolarisRegistry extends FailbackRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolarisDirectory.class);

    private static final int SUBSCRIBE_DELAY = 5;

    private static final Map<URL, ScheduledExecutorService> EXECUTOR_MAP = new ConcurrentHashMap<>();

    private final Map<String, Protocol> protocols = new HashMap<>();

    private final ScheduledExecutorService subscribeExecutor = Executors.newSingleThreadScheduledExecutor();

    public PolarisRegistry(URL url) {
        super(url);
        ExtensionLoader<Protocol> extensionLoader = ExtensionLoader.getExtensionLoader(Protocol.class);
        Set<String> supportedExtensions = extensionLoader.getSupportedExtensions();
        for (String supportedExtension : supportedExtensions) {
            protocols.put(supportedExtension, extensionLoader.getExtension(supportedExtension));
        }
    }

    private int parsePort(String protocolStr, int port) {
        if (port > 0) {
            return port;
        }
        Protocol protocol = protocols.get(protocolStr);
        if (null == protocol) {
            return 0;
        }
        return protocol.getDefaultPort();
    }

    @Override
    public void doRegister(URL url) {
        LOGGER.info("[polaris] register service to polaris: {}", url.toString());
        Map<String, String> metadata = new HashMap<>(url.getParameters());
        metadata.put(PATH_KEY, url.getPath());
        int port = parsePort(url.getProtocol(), url.getPort());
        if (port > 0) {
            PolarisSingleton.getPolarisOperation()
                    .register(url.getServiceInterface(), url.getHost(), port, url.getProtocol(), metadata);
        }
    }

    @Override
    public void doUnregister(URL url) {
        LOGGER.info("[polaris] unregister service from polaris: {}", url.toString());
        int port = parsePort(url.getProtocol(), url.getPort());
        if (port > 0) {
            PolarisSingleton.getPolarisOperation().deregister(url.getServiceInterface(), url.getHost(), url.getPort());
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        subscribeExecutor.shutdown();
        PolarisSingleton.getPolarisOperation().destroy();
    }

    @Override
    public void doSubscribe(final URL url, final NotifyListener listener) {
        LOGGER.info("[polaris] subscribe service: {}", url.toString());
        //String namespace = Properties.getInstance().getNamespace();
        String service = url.getServiceInterface();
        // 先更新一次
        SubscribeTask subscribeTask = new SubscribeTask(url, listener, service);
        subscribeTask.run();
        // 再让定时线程执行
        subscribeExecutor.scheduleWithFixedDelay(subscribeTask, 0, SUBSCRIBE_DELAY, TimeUnit.SECONDS);
        EXECUTOR_MAP.put(url, subscribeExecutor);
    }

    /**
     * 定时线程，实时更新instance信息
     */
    private class SubscribeTask implements Runnable {

        private final URL url;

        private final NotifyListener listener;

        private final String service;

        // private int cachedHashCode;

        private SubscribeTask(URL url, NotifyListener listener, String service) {
            this.url = url;
            this.listener = listener;
            this.service = service;
            //this.cachedHashCode = -1;
        }

        @Override
        public void run() {
            //cachedHashCode = serviceInstances.hashCode();
            List<?> instances = PolarisSingleton.getPolarisOperation().getAvailableInstances(service, null);
            if (null != instances) {
                // 刷新invoker信息
                LOGGER.debug("update instances count: {}, service: {}", instances.size(), service);
                List<URL> urls = new ArrayList<>();
                for (Object instance : instances) {
                    String host = PolarisSingleton.getPolarisOperation().getHost(instance);
                    int port = PolarisSingleton.getPolarisOperation().getPort(instance);
                    String protocolStr = PolarisSingleton.getPolarisOperation().getProtocol(instance);
                    Map<String, String> metadata = PolarisSingleton.getPolarisOperation().getMetadata(instance);
                    URL url = buildURL(host, port, protocolStr, metadata);
                    urls.add(url);
                }
                PolarisRegistry.this.notify(url, listener, urls);
            }
        }

        private URL buildURL(String host, int port, String protocol, Map<String, String> metadata) {
            return new URL(protocol,
                    host,
                    port,
                    metadata.get(PATH_KEY),
                    metadata);
        }
    }

    @Override
    public void doUnsubscribe(URL url, NotifyListener listener) {
        LOGGER.info("[polaris] unsubscribe service: {}", url.toString());
        EXECUTOR_MAP.get(url).shutdown();
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
