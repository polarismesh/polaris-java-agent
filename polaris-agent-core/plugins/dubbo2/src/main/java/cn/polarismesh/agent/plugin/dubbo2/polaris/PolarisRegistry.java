package cn.polarismesh.agent.plugin.dubbo2.polaris;

import cn.polarismesh.agent.plugin.dubbo2.entity.Properties;
import cn.polarismesh.agent.plugin.dubbo2.utils.PolarisUtil;
import com.tencent.polaris.api.exception.PolarisException;
import com.tencent.polaris.api.pojo.Instance;
import com.tencent.polaris.api.pojo.ServiceInstances;
import com.tencent.polaris.api.utils.StringUtils;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.utils.ConcurrentHashSet;
import org.apache.dubbo.registry.NotifyListener;
import org.apache.dubbo.registry.support.FailbackRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static cn.polarismesh.agent.plugin.dubbo2.constants.PolarisConstants.FILTERED_PARAMS_IF_EMPTY;

/**
 * 服务注册中心，提供服务注册，服务发现相关功能
 */
public class PolarisRegistry extends FailbackRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolarisDirectory.class);

    private static final int SUBSCRIBE_DELAY = 5;

    private static final Set<String> SUBSCRIBE_SET = new ConcurrentHashSet<>();

    private static final Map<URL, ScheduledExecutorService> EXECUTOR_MAP = new ConcurrentHashMap<>();

    public PolarisRegistry(URL url) {
        super(url);
    }

    @Override
    public void doRegister(URL url) {
        LOGGER.info("[polaris] register service to polaris: {}", url.toString());
        PolarisUtil.register(url);
    }

    @Override
    public void doUnregister(URL url) {
        LOGGER.info("[polaris] unregister service from polaris: {}", url.toString());
        PolarisUtil.shutdown(url);
    }

    @Override
    public void doSubscribe(final URL url, final NotifyListener listener) {
        LOGGER.info("[polaris] subscribe service: {}", url.toString());
        String namespace = Properties.getInstance().getNamespace();
        String service = url.getServiceInterface();
        //  如果目标service已经有线程负责了，则无需新起一个线程
        if (SUBSCRIBE_SET.contains(service)) {
            LOGGER.info("service: {} has subscribed", service);
            return;
        }
        ScheduledExecutorService subscribeExecutor = Executors.newSingleThreadScheduledExecutor();
        subscribeExecutor.scheduleWithFixedDelay(new PolarisRegistry.SubscribeTask(url, listener, namespace, service), 0, SUBSCRIBE_DELAY, TimeUnit.SECONDS);
        SUBSCRIBE_SET.add(service);
        EXECUTOR_MAP.put(url, subscribeExecutor);
    }

    /**
     * 定时线程，实时更新instance信息
     */
    private class SubscribeTask implements Runnable {

        private final URL url;

        private final NotifyListener listener;

        private final String namespace;

        private final String service;

        private int cachedHashCode;

        private SubscribeTask(URL url, NotifyListener listener, String namespace, String service) {
            this.url = url;
            this.listener = listener;
            this.namespace = namespace;
            this.service = service;
            this.cachedHashCode = -1;
        }

        @Override
        public void run() {
            LOGGER.info("[polaris] update instances info, namespace: {}, service: {}", namespace, service);
            ServiceInstances serviceInstances;
            try {
                serviceInstances = PolarisUtil.getAllInstances(namespace, service);
            } catch (PolarisException e) {
                LOGGER.error("update instance fail, exception: {}", e.getMessage());
                return;
            }
            // TODO hashCode相同，说明两次更新Instance未发生变化，不做任何操作
//            if (cachedHashCode == serviceInstances.hashCode()) {
//                LOGGER.info("instances has no change");
//                return;
//            }
            // 记录hashCode
            cachedHashCode = serviceInstances.hashCode();
            // 刷新invoker信息
            LOGGER.info("update instances count: {}", serviceInstances.getInstances().size());
            List<URL> urls = new ArrayList<>();
            for (Instance instance : serviceInstances.getInstances()) {
                urls.add(buildURL(instance));
            }
            PolarisRegistry.this.notify(url, listener, urls);
        }

        private URL buildURL(Instance instance) {
            Map<String, String> metadata = new HashMap<>(instance.getMetadata());
            // 清理protocol version的键值信息如果对应的value值为空
            for (String key : FILTERED_PARAMS_IF_EMPTY) {
                if (StringUtils.isEmpty(metadata.get(key))) {
                    metadata.remove(key);
                }
            }

            return new URL(instance.getProtocol(),
                    null,
                    null,
                    instance.getHost(),
                    instance.getPort(),
                    instance.getService(),
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
