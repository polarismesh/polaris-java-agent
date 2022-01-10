package cn.polarismesh.plugin.dubbo2.utils;

public class StringUtil {
    public static String buildAddress(String host, int port) {
        if (port <= 0) {
            return host;
        }
        return host + ":" + port;
    }

    public static boolean isNumeric(String s) {
        if (s != null && !"".equals(s.trim()))
            return s.matches("^[0-9]*$");
        else
            return false;
    }
}
