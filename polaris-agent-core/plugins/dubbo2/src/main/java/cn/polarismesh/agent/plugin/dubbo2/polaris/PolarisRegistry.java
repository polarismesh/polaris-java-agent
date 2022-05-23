package cn.polarismesh.agent.plugin.dubbo2.polaris;

import cn.polarismesh.agent.plugin.dubbo2.constants.PolarisConstants;
import cn.polarismesh.agent.plugin.dubbo2.entity.InstanceInvoker;
import com.tencent.polaris.api.exception.PolarisException;
import com.tencent.polaris.api.listener.ServiceListener;
import com.tencent.polaris.api.pojo.Instance;
import com.tencent.polaris.api.pojo.ServiceChangeEvent;
import com.tencent.polaris.client.util.NamedThreadFactory;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.apache.dubbo.common.constants.CommonConstants.PATH_KEY;
import static org.apache.dubbo.common.constants.CommonConstants.VERSION_KEY;
import static org.apache.dubbo.rpc.cluster.Constants.DEFAULT_WEIGHT;
import static org.apache.dubbo.rpc.cluster.Constants.WEIGHT_KEY;

/**
 * 服务注册中心，提供服务注册，服务发现相关功能
 */
public class PolarisRegistry extends FailbackRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolarisRegistry.class);

    private static final TaskScheduler taskScheduler = new TaskScheduler();

    private final Map<String, Integer> protocols = new HashMap<>();

    private final Set<URL> registeredInstances = new ConcurrentHashSet<>();

    private final AtomicBoolean destroyed = new AtomicBoolean(false);

    private final Map<NotifyListener, ServiceListener> serviceListeners = new ConcurrentHashMap<>();

    /**
     * dubbo=org.apache.dubbo.rpc.protocol.dubbo.DubboProtocol
     * injvm=org.apache.dubbo.rpc.protocol.injvm.InjvmProtocol
     * http=org.apache.dubbo.rpc.protocol.http.HttpProtocol
     * rmi=org.apache.dubbo.rpc.protocol.rmi.RmiProtocol
     * hessian=org.apache.dubbo.rpc.protocol.hessian.HessianProtocol
     * webservice=org.apache.dubbo.rpc.protocol.webservice.WebServiceProtocol
     * thrift=org.apache.dubbo.rpc.protocol.thrift.ThriftProtocol
     * native-thrift=org.apache.dubbo.rpc.protocol.nativethrift.ThriftProtocol
     * memcached=org.apache.dubbo.rpc.protocol.memcached.MemcachedProtocol
     * redis=org.apache.dubbo.rpc.protocol.redis.RedisProtocol
     * rest=org.apache.dubbo.rpc.protocol.rest.RestProtocol
     * xmlrpc=org.apache.dubbo.xml.rpc.protocol.xmlrpc.XmlRpcProtocol
     * grpc=org.apache.dubbo.rpc.protocol.grpc.GrpcProtocol
     */
    {
        protocols.put("dubbo", 20880);
        protocols.put("injvm", 0);
        protocols.put("http", 80);
        protocols.put("rmi", 1099);
        protocols.put("hessian", 80);
        protocols.put("webservice", 80);
        protocols.put("thrift", 40880);
        protocols.put("native-thrift", 40880);
        protocols.put("memcached", 11211);
        protocols.put("redis", 9090);
        protocols.put("rest", 80);
        protocols.put("xmlrpc", 80);
        protocols.put("grpc", 50051);
    }

    public PolarisRegistry(URL url) {
        super(url);
    }

    private int parsePort(String protocolStr, int port) {
        if (port > 0) {
            return port;
        }
        Integer protocolPort = protocols.get(protocolStr);
        if (null == protocolPort) {
            return 0;
        }
        return protocolPort;
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
            PolarisSingleton.getPolarisWatcher()
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
            PolarisSingleton.getPolarisWatcher()
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
            PolarisSingleton.getPolarisWatcher().destroy();
        }
    }

    @Override
    public void doSubscribe(final URL url, final NotifyListener listener) {
        String service = url.getServiceInterface();
        Instance[] instances = PolarisSingleton.getPolarisWatcher().getAvailableInstances(service);
        onInstances(url, listener, instances);
        LOGGER.info("[POLARIS] submit watch task for service {}", service);
        taskScheduler.submitWatchTask(new WatchTask(url, listener, service));
    }

    private class WatchTask implements Runnable {

        private final String service;

        private final ServiceListener serviceListener;

        private final NotifyListener listener;

        private final FetchTask fetchTask;

        public WatchTask(URL url, NotifyListener listener, String service) {
            this.service = service;
            this.listener = listener;
            fetchTask = new FetchTask(url, listener);
            serviceListener = new ServiceListener() {
                @Override
                public void onEvent(ServiceChangeEvent event) {
                    PolarisRegistry.taskScheduler.submitFetchTask(fetchTask);
                }
            };
        }

        @Override
        public void run() {
            boolean result = PolarisSingleton.getPolarisWatcher().watchService(service, serviceListener);
            if (result) {
                serviceListeners.put(listener, serviceListener);
                PolarisRegistry.taskScheduler.submitFetchTask(fetchTask);
                return;
            }
            PolarisRegistry.taskScheduler.submitWatchTask(this);
        }
    }

    private class FetchTask implements Runnable {

        private final String service;

        private final URL url;

        private final NotifyListener listener;

        public FetchTask(URL url, NotifyListener listener) {
            this.service = url.getServiceInterface();
            this.url = url;
            this.listener = listener;
        }

        @Override
        public void run() {
            Instance[] instances;
            try {
                instances = PolarisSingleton.getPolarisWatcher().getAvailableInstances(service);
            } catch (PolarisException e) {
                LOGGER.error("[POLARIS] fail to fetch instances for service {}: {}", service, e.toString());
                return;
            }
            onInstances(url, listener, instances);
        }
    }

    private void onInstances(URL url, NotifyListener listener, Instance[] instances) {
        LOGGER.info("[POLARIS] update instances count: {}, service: {}", null == instances ? 0 : instances.length,
                url.getServiceInterface());
        List<URL> urls = new ArrayList<>();
        if (null != instances) {
            for (Instance instance : instances) {
                urls.add(instanceToURL(instance));
            }
        }
        PolarisRegistry.this.notify(url, listener, urls);
    }

    private static URL instanceToURL(Instance instance) {
        Map<String, String> newMetadata = new HashMap<>(instance.getMetadata());
        boolean hasWeight = false;
        if (newMetadata.containsKey(WEIGHT_KEY)) {
            String weightStr = newMetadata.get(WEIGHT_KEY);
            try {
                int weightValue = Integer.parseInt(weightStr);
                if (weightValue == instance.getWeight()) {
                    hasWeight = true;
                }
            } catch (Exception ignored) {
            }
        }
        if (!hasWeight) {
            newMetadata.put(WEIGHT_KEY, Integer.toString(instance.getWeight()));
        }
        newMetadata.put(PolarisConstants.KEY_ID, instance.getId());
        newMetadata.put(PolarisConstants.KEY_HEALTHY, Boolean.toString(instance.isHealthy()));
        newMetadata.put(PolarisConstants.KEY_ISOLATED, Boolean.toString(instance.isIsolated()));
        newMetadata.put(PolarisConstants.KEY_CIRCUIT_BREAKER, InstanceInvoker.circuitBreakersToString(instance));
        return new URL(instance.getProtocol(),
                instance.getHost(),
                instance.getPort(),
                newMetadata.get(PATH_KEY),
                newMetadata);
    }

    private static class TaskScheduler {

        private final ExecutorService fetchExecutor = Executors
                .newSingleThreadExecutor(new NamedThreadFactory("agent-fetch"));

        private final ExecutorService watchExecutor = Executors
                .newSingleThreadExecutor(new NamedThreadFactory("agent-retry-watch"));

        private final AtomicBoolean executorDestroyed = new AtomicBoolean(false);

        private final Object lock = new Object();

        void submitFetchTask(Runnable fetchTask) {
            if (executorDestroyed.get()) {
                return;
            }
            synchronized (lock) {
                if (executorDestroyed.get()) {
                    return;
                }
                fetchExecutor.submit(fetchTask);
            }
        }

        void submitWatchTask(Runnable watchTask) {
            if (executorDestroyed.get()) {
                return;
            }
            synchronized (lock) {
                if (executorDestroyed.get()) {
                    return;
                }
                watchExecutor.submit(watchTask);
            }
        }


        boolean isDestroyed() {
            return executorDestroyed.get();
        }

        void destroy() {
            synchronized (lock) {
                if (executorDestroyed.compareAndSet(false, true)) {
                    fetchExecutor.shutdown();
                    watchExecutor.shutdown();
                }
            }
        }
    }

    @Override
    public void doUnsubscribe(URL url, NotifyListener listener) {
        LOGGER.info("[polaris] unsubscribe service: {}", url.toString());
        taskScheduler.submitWatchTask(new Runnable() {
            @Override
            public void run() {
                ServiceListener serviceListener = serviceListeners.get(listener);
                if (null != serviceListener) {
                    PolarisSingleton.getPolarisWatcher().unwatchService(url.getServiceInterface(), serviceListener);
                }
            }
        });
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
