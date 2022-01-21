package cn.polarismesh.agent.core.spring.cloud.util;

import cn.polarismesh.agent.core.spring.cloud.constant.PolarisServiceConstants;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 实例host工具类
 *
 * @author zhuyuhan
 */
public class HostUtils {

    private static final String DEFAULT_ADDRESS = "127.0.0.1";

    public static String getHost() {
        if (!StringUtils.isEmpty(PolarisServiceConstants.host))
            return PolarisServiceConstants.host;
        InetUtils inetUtils = new InetUtils(null);
        try {
            return inetUtils.findFirstNonLoopbackHostInfo().getIpAddress();
        } catch (Exception e) {
            try {
                return InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException ex) {
                return DEFAULT_ADDRESS;
            }
        }
    }
}
