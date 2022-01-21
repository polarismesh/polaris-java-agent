package cn.polarismesh.agent.core.spring.cloud.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 日志工具类
 *
 * @author zhuyuhan
 */
public class LogUtils {

    private static final Logger log = LoggerFactory.getLogger(LogUtils.class);

    public static void logTargetFound(Object target) {
        log.info("target {} has been found", target);
    }

    public static void logTargetMethodFound(String method) {
        log.info("agent method is found: {} for instrumentation", method);
    }

    public static void logInvoke(Object invoker, String method) {
        log.info("agent method {} is invoked by {}", method, invoker);
    }

}
