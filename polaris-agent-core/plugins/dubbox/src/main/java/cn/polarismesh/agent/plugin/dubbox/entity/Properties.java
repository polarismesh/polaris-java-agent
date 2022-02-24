package cn.polarismesh.agent.plugin.dubbox.entity;

import cn.polarismesh.agent.plugin.dubbox.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static cn.polarismesh.agent.plugin.dubbox.constants.PolarisConstants.*;

/**
 * 启动参数实体类,单例
 */
public class Properties {
    private static final Logger LOGGER = LoggerFactory.getLogger(Properties.class);

    private static Properties instance = new Properties();

    private final String address;
    private final String namespace;
    private final int ttl;
    private final String loadbalance;

    private Properties() {
        address = System.getProperty(ADDRESS_KEY, DEFAULT_ADDRESS);
        namespace = System.getProperty(NAMESPACE_KEY, DEFAULT_NAMESPACE);
        String ttlStr = System.getProperty(TTL_KEY);
        if (StringUtil.isNumeric(ttlStr)) {
            ttl = Integer.parseInt(ttlStr);
        } else {
            LOGGER.error("invalid ttl value: {}, use default ttl={} instead", ttlStr, DEFAULT_TTL);
            ttl = DEFAULT_TTL;
        }
        loadbalance = System.getProperty(LOADBALANCE_KEY, DEFAULT_LOADBALANCE);
    }

    public static Properties getInstance() {
        return instance;
    }

    public String getAddress() {
        return address;
    }

    public String getNamespace() {
        return namespace;
    }

    public int getTtl() {
        return ttl;
    }

    public String getLoadbalance() {
        return loadbalance;
    }
}
