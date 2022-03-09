package cn.polarismesh.agent.plugin.dubbo2.polaris;

import cn.polarismesh.common.polaris.PolarisConfig;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.ExtensionLoader;
import org.apache.dubbo.common.utils.ConcurrentHashSet;
import org.apache.dubbo.registry.NotifyListener;
import org.apache.dubbo.registry.support.FailbackRegistry;
import org.apache.dubbo.rpc.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.apache.dubbo.common.constants.CommonConstants.PATH_KEY;
import static org.apache.dubbo.common.constants.CommonConstants.VERSION_KEY;
import static org.apache.dubbo.rpc.cluster.Constants.DEFAULT_WEIGHT;
import static org.apache.dubbo.rpc.cluster.Constants.WEIGHT_KEY;

/**
 * 服务注册中心，提供服务注册，服务发现相关功能
 */
public class PolarisRegistry extends FailbackRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolarisDirectory.class);

    private static final Map<URL, ScheduledExecutorService> EXECUTOR_MAP = new ConcurrentHashMap<>();

    private final Map<String, Protocol> protocols = new HashMap<>();

    private final ScheduledExecutorService subscribeExecutor = Executors.newSingleThreadScheduledExecutor();

    private final PolarisConfig polarisConfig;

    private final Set<URL> registeredInstances = new ConcurrentHashSet<>();

    private final AtomicBoolean destroyed = new AtomicBoolean(false);

    public PolarisRegistry(URL url) {
        super(url);
        ExtensionLoader<Protocol> extensionLoader = ExtensionLoader.getExtensionLoader(Protocol.class);
        Set<String> supportedExtensions = extensionLoader.getSupportedExtensions();
        for (String supportedExtension : supportedExtensions) {
            protocols.put(supportedExtension, extensionLoader.getExtension(supportedExtension));
        }
        polarisConfig = new PolarisConfig();
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
        LOGGER.info("[POLARIS] register service to polaris: {}", url.toString());
        Map<String, String> metadata = new HashMap<>(url.getParameters());
        metadata.put(PATH_KEY, url.getPath());
        int port = parsePort(url.getProtocol(), url.getPort());
        if (port > 0) {
            int weight = url.getParameter(WEIGHT_KEY, DEFAULT_WEIGHT);
            String version = url.getParameter(VERSION_KEY, "");
            PolarisSingleton.getPolarisOperation()
                    .register(url.getServiceInterface(), url.getHost(), port, url.getProtocol(), version, weight,
                            metadata);
            registeredInstances.add(url);
        }
    }

    @Override
    public void doUnregister(URL url) {
        LOGGER.info("[POLARIS] unregister service from polaris: {}", url.toString());
        int port = parsePort(url.getProtocol(), url.getPort());
        if (port > 0) {
            PolarisSingleton.getPolarisOperation()
                    .deregister(url.getServiceInterface(), url.getHost(), url.getPort());
            registeredInstances.remove(url);
        }
    }

    @Override
    public void destroy() {
        if (destroyed.compareAndSet(false, true)) {
            super.destroy();
            Collection<URL> urls = Collections.unmodifiableCollection(registeredInstances);
            for (URL url : urls) {
                doUnregister(url);
            }
            subscribeExecutor.shutdown();
            PolarisSingleton.getPolarisOperation().destroy();
        }
    }

    @Override
    public void doSubscribe(final URL url, final NotifyListener listener) {
        //String namespace = Properties.getInstance().getNamespace();
        String service = url.getServiceInterface();
        // 先更新一次
        SubscribeTask subscribeTask = new SubscribeTask(url, listener, service);
        subscribeTask.run();
        // 再让定时线程执行
        LOGGER.info("[POLARIS] subscribe task scheduled with interval {}, url {}", polarisConfig.getRefreshInterval(),
                url);
        subscribeExecutor
                .scheduleWithFixedDelay(subscribeTask, 0, polarisConfig.getRefreshInterval(), TimeUnit.SECONDS);
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
                LOGGER.debug("[POLARIS] update instances count: {}, service: {}", instances.size(), service);
                List<URL> urls = new ArrayList<>();
                for (Object instance : instances) {
                    String host = PolarisSingleton.getPolarisOperation().getHost(instance);
                    int port = PolarisSingleton.getPolarisOperation().getPort(instance);
                    int weight = PolarisSingleton.getPolarisOperation().getWeight(instance);
                    String protocolStr = PolarisSingleton.getPolarisOperation().getProtocol(instance);
                    Map<String, String> metadata = PolarisSingleton.getPolarisOperation().getMetadata(instance);
                    URL url = buildURL(host, port, protocolStr, weight, metadata);
                    urls.add(url);
                }
                PolarisRegistry.this.notify(url, listener, urls);
            }
        }

        private URL buildURL(String host, int port, String protocol, int weight, Map<String, String> metadata) {
            Map<String, String> newMetadata = metadata;
            boolean hasWeight = false;
            if (metadata.containsKey(WEIGHT_KEY)) {
                String weightStr = metadata.get(WEIGHT_KEY);
                try {
                    int weightValue = Integer.parseInt(weightStr);
                    if (weightValue == weight) {
                        hasWeight = true;
                    }
                } catch (Exception ignored) {
                }
            }
            if (!hasWeight) {
                newMetadata = new HashMap<>(metadata);
                newMetadata.put(WEIGHT_KEY, Integer.toString(weight));
            }
            return new URL(protocol,
                    host,
                    port,
                    newMetadata.get(PATH_KEY),
                    newMetadata);
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
