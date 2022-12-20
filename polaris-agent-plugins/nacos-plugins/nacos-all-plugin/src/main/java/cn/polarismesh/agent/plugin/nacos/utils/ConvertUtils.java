package cn.polarismesh.agent.plugin.nacos.utils;


public class ConvertUtils {


    private static final String NULL_STR = "null";

    public static int toInt(String val) {
        return toInt(val, 0);
    }

    public static int toInt(String val, int defaultValue) {
        if (val == null) {
            return defaultValue;
        }
        val = val.trim();

        if (val.isEmpty()) {
            return defaultValue;
        }
        if (NULL_STR.equalsIgnoreCase(val)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }

}
