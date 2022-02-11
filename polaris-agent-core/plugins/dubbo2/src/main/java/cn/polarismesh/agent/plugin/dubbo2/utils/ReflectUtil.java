package cn.polarismesh.agent.plugin.dubbo2.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * 反射相关工具类
 */
public class ReflectUtil {
    /**
     * 根据属性名返回对象的属性
     *
     * @param target    对象
     * @param fieldName 对象的属性名
     * @return 获取到的对象属性
     */
    public static Object getObjectByFieldName(Object target, String fieldName) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据属性名返回对象父类的属性
     *
     * @param target 对象
     * @param fieldName 对象父类的属性名
     * @return 获取到的对象父类的属性
     */
    public static Object getSuperObjectByFieldName(Object target, String fieldName) {
        try {
            Field field = target.getClass().getSuperclass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据属性名重新设置对象的属性
     *
     * @param target    对象
     * @param fieldName 对象的属性名
     * @param value     新值
     */
    public static void setValueByFieldName(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            setValue(target, field, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据属性名重新设置对象父类的属性
     *
     * @param target    对象
     * @param fieldName 对象父类的属性名
     * @param value     新值
     */
    public static void setSuperValueByFieldName(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getSuperclass().getDeclaredField(fieldName);
            setValue(target, field, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void setValue(Object target, Field field, Object value) {
        try {
            Field modifiers = Field.class.getDeclaredField("modifiers");
            modifiers.setAccessible(true);
            modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据方法名和参数执行方法
     *
     * @param target 拥有该方法的对象
     * @param methodName 方法名
     * @param arg 方法入参
     * @return 方法返回值
     */
    public static Object invokeMethodByName(Object target, String methodName, Object arg) {
        try {
            Method m;
            if (arg == null) {
                m = target.getClass().getDeclaredMethod(methodName);
            } else {
                m = target.getClass().getDeclaredMethod(methodName, arg.getClass());
            }
            m.setAccessible(true);
            return arg == null ? m.invoke(target) : m.invoke(target, arg);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
