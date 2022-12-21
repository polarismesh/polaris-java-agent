package cn.polarismesh.agent.plugin.nacos.adapter;

import cn.polarismesh.agent.core.common.utils.ReflectionUtils;
import cn.polarismesh.agent.plugin.nacos.exception.UnsupportedNacosClientException;

import java.lang.reflect.InvocationTargetException;

/**
 * 适配 com.alibaba.nacos.client.naming.utils.UtilAndComs 不同版本变量名
 */
public class UtilAndComsAdapter {

    public static final int REQUEST_DOMAIN_RETRY_COUNT = 3;

    public static final String BEFORE_130_NACOS_URL_INSTANCE = "NACOS_URL_INSTANCE";
    public static final String AFTER_130_NACOS_URL_INSTANCE = "nacosUrlInstance";

    public static final String BEFORE_130_NACOS_URL_BASE = "NACOS_URL_BASE";
    public static final String AFTER_130_NACOS_URL_BASE = "nacosUrlBase";

    public static final String NACOS_URL_INSTANCE;
    public static final String NACOS_URL_BASE;

    static {
//        UtilAndComs target = new UtilAndComs();
        try {
            Object target = Class.forName("com.alibaba.nacos.client.naming.utils.UtilAndComs").getConstructors()[0].newInstance();
            NACOS_URL_INSTANCE = (String) getValueFromDifVersion(target, AFTER_130_NACOS_URL_INSTANCE, BEFORE_130_NACOS_URL_INSTANCE);
            NACOS_URL_BASE = (String) getValueFromDifVersion(target, AFTER_130_NACOS_URL_BASE, BEFORE_130_NACOS_URL_BASE);
        } catch (Exception e) {
            throw new UnsupportedNacosClientException(e);
        }
    }

    private static Object getValueFromDifVersion(Object obj, String... versions) {
        Object ret;
        for (String version : versions) {
            try {
                ret = ReflectionUtils.getObjectByFieldName(obj, version);
                if (ret != null) {
                    return ret;
                }
            } catch (Exception ignored) {
            }
        }
        throw new UnsupportedNacosClientException();
    }

}
