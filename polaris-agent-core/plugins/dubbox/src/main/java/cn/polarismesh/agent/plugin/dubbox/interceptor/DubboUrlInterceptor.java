package cn.polarismesh.agent.plugin.dubbox.interceptor;

import static com.alibaba.dubbo.common.Constants.CONSUMER_PROTOCOL;

import cn.polarismesh.agent.common.tools.ReflectionUtils;
import cn.polarismesh.agent.common.tools.ReflectionUtils.FieldCallback;
import cn.polarismesh.agent.common.tools.ReflectionUtils.FieldFilter;
import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class DubboUrlInterceptor implements AbstractInterceptor {

    @Override
    public void before(Object target, Object[] args) {

    }

    private void setAnyValue(Map<String, String> parameters, String key) {
        if (!parameters.containsKey(key)) {
            parameters.put(key, Constants.ANY_VALUE);
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        ReflectionUtils.doWithFields(URL.class, new FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                String protocol = (String) args[0];
                if (!protocol.equals(CONSUMER_PROTOCOL)) {
                    return;
                }
                ReflectionUtils.makeAccessible(field);
                Map<String, String> parameters = (Map<String, String>) ReflectionUtils.getField(field, target);
                if (parameters.containsKey(Constants.VERSION_KEY) && parameters.containsKey(Constants.GROUP_KEY)
                        && parameters.containsKey(Constants.CLASSIFIER_KEY)) {
                    return;
                }
                Map<String, String> nParameters = new HashMap<>(parameters);
                setAnyValue(nParameters, Constants.VERSION_KEY);
                setAnyValue(nParameters, Constants.GROUP_KEY);
                setAnyValue(nParameters, Constants.CLASSIFIER_KEY);
                ReflectionUtils.setField(field, target, nParameters);
            }
        }, new FieldFilter() {
            @Override
            public boolean matches(Field field) {
                if (field.getName().equals("parameters")) {
                    return true;
                }
                return false;
            }
        });
    }
}
