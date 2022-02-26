package cn.polarismesh.agent.core.spring.cloud.context;


/**
 * 调用上下文Holder
 *
 * @author zhuyuhan
 */
public class InvokeContextHolder {

    /**
     * init Context for current thread
     */
    private static ThreadLocal<InvokeContext> local = ThreadLocal.withInitial(InvokeContext::new);

    private InvokeContextHolder() {
    }

    public static InvokeContext get() {
        if (local.get() == null) {
            local.set(new InvokeContext());
        }
        return local.get();
    }

    public static void clear() {
        local.remove();
    }

    public static void remove() {
        local.remove();
    }

}
