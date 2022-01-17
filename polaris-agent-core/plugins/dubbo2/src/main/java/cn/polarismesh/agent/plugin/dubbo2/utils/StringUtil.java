package cn.polarismesh.agent.plugin.dubbo2.utils;

/**
 * 字符串相关工具类
 */
public class StringUtil {
    /**
     * 将host、port转为host:port形式
     */
    public static String buildAdress(String host, int port) {
        if (port <= 0) {
            return host;
        }
        return host + ":" + port;
    }

    /**
     * 判断字符串是否为数字
     */
    public static boolean isNumeric(String s) {
        if (s != null && !"".equals(s.trim()))
            return s.matches("^[0-9]*$");
        else
            return false;
    }
}
