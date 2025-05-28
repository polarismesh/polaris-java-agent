package cn.polarismesh.agent.examples.alibaba.cloud.cloud;

import org.springframework.stereotype.Component;

@Component
public class ConsumerServiceFallback implements ConsumerServiceWithFallback {
    @Override
    public String circuitBreak() {
        return "fallback: trigger the refuse for service callee.";
    }
}
