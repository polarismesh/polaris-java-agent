package cn.polarismesh.agent.core.spring.cloud.constant;

/**
 * Polaris服务注册常量
 *
 * @author zhuyuhan
 */
public class PolarisServiceConstants {

    /**
     * 端口号
     */
    public static String PORT;

    /**
     * 服务名
     */
    public static String SERVICE;

    /**
     * 主机
     */
    public static String HOST;

    public interface HeaderName {

        /**
         * Polaris Agent路由元信息header key
         */
        String METADATA_HEADER = "POLARIS-AGENT";
    }
}
