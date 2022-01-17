package cn.polarismesh.agent.plugin.dubbo2.entity;

import org.apache.dubbo.rpc.Invoker;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 用于在Invoker对象创建的时候记录host:port与Invoker对象的映射关系
 */
public class InvokerMap {
    private static final ConcurrentMap<String, Invoker<?>> map = new ConcurrentHashMap<>();

    public static Invoker get(String key) {
        return map.get(key);
    }

    public static void put(String key, Invoker invoker) {
        map.put(key, invoker);
    }

    public static void remove(String key) {
        map.remove(key);
    }
}
