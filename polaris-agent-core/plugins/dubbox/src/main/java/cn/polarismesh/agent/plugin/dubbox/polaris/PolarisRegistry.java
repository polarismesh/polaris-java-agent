package cn.polarismesh.agent.plugin.dubbox.polaris;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.utils.ConcurrentHashSet;
import com.alibaba.dubbo.registry.NotifyListener;
import com.alibaba.dubbo.registry.support.FailbackRegistry;
import com.alibaba.dubbo.rpc.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.alibaba.dubbo.common.Constants.PATH_KEY;

/**
 * 服务注册中心，提供服务注册，服务发现相关功能
 */
public class PolarisRegistry extends FailbackRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolarisDirectory.class);

    private final Map<String, Protocol> protocols = new HashMap<>();

    private final ScheduledExecutorService subscribeExecutor = Executors.newSingleThreadScheduledExecutor();

    private final Set<URL> registeredInstances = new ConcurrentHashSet<>();

    private final AtomicBoolean destroyed = new AtomicBoolean(false);

    private static final String LISTENER_METHOD = "onEvent";

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
        LOGGER.info("[POLARIS] register service to polaris: {}", url.toString());
        Map<String, String> metadata = new HashMap<>(url.getParameters());
        metadata.put(PATH_KEY, url.getPath());
        int port = parsePort(url.getProtocol(), url.getPort());
        if (port > 0) {
            int weight = url.getParameter(Constants.WEIGHT_KEY, Constants.DEFAULT_WEIGHT);
            String version = url.getParameter(Constants.VERSION_KEY, "");
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
        String service = url.getServiceInterface();
        ServiceListener serviceListener = new ServiceListener(service, url, listener);
        Method method;
        try {
            method = ServiceListener.class.getMethod(LISTENER_METHOD, Object.class);
        } catch (Exception e) {
            LOGGER.error("[POLARIS] get method {} fail, exception is: {}", LISTENER_METHOD, e.getMessage());
            return;
        }
        List<?> instances = PolarisSingleton.getPolarisOperation().watchService(service, serviceListener, method);
        instancesNotify(instances, url, listener, service);
    }

    public class ServiceListener {
        private String service;
        private URL url;
        private NotifyListener listener;

        public ServiceListener(String service, URL url, NotifyListener listener) {
            this.service = service;
            this.url = url;
            this.listener = listener;
        }

        public void onEvent(Object event) {
            List<?> instances = PolarisSingleton.getPolarisOperation().getAllInstances(service);
            LOGGER.info("[POLARIS] onEvent invoke, instances count is: {}", instances.size());
            instancesNotify(instances, url, listener, service);
        }
    }

    private void instancesNotify(List<?> instances, URL url, NotifyListener listener, String service) {
        if (null == instances) {
            return;
        }
        // 刷新invoker信息
        LOGGER.debug("[POLARIS] update instances count: {}, service: {}", instances.size(), service);
        List<URL> urls = new ArrayList<>();
        for (Object instance : instances) {
            String host = PolarisSingleton.getPolarisOperation().getHost(instance);
            int port = PolarisSingleton.getPolarisOperation().getPort(instance);
            int weight = PolarisSingleton.getPolarisOperation().getWeight(instance);
            String protocolStr = PolarisSingleton.getPolarisOperation().getProtocol(instance);
            Map<String, String> metadata = PolarisSingleton.getPolarisOperation().getMetadata(instance);
            URL providerUrl = buildURL(host, port, protocolStr, weight, metadata);
            urls.add(providerUrl);
        }
        PolarisRegistry.this.notify(url, listener, urls);
    }

    private URL buildURL(String host, int port, String protocol, int weight, Map<String, String> metadata) {
        Map<String, String> newMetadata = metadata;
        boolean hasWeight = false;
        if (metadata.containsKey(Constants.WEIGHT_KEY)) {
            String weightStr = metadata.get(Constants.WEIGHT_KEY);
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
            newMetadata.put(Constants.WEIGHT_KEY, Integer.toString(weight));
        }
        return new URL(protocol,
                host,
                port,
                newMetadata.get(PATH_KEY),
                newMetadata);
    }

    @Override
    public void doUnsubscribe(URL url, NotifyListener listener) {
        LOGGER.info("[POLARIS] unsubscribe service: {}", url.toString());
        boolean result = PolarisSingleton.getPolarisOperation().unWatchService(url.getServiceInterface());
        if (!result) {
            LOGGER.error("[POLARIS] unsubscribe service {} fail", url.getServiceInterface());
        }
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
