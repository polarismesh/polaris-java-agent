package cn.polarismesh.agent.examples.alibaba.cloud.cloud;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "service-provider-hoxton", contextId = "fallback-from-code",
        fallback = ConsumerServiceFallback.class)
public interface ConsumerService {

    /**
     * Check circuit break.
     *
     * @return circuit break info
     */
    @GetMapping("/circuitBreak")
    String circuitBreak();
}
