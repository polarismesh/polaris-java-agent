package cn.polarismesh.agent.examples.alibaba.cloud.cloud;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "service-provider-2021", contextId = "fallback-from-polaris")
public interface ConsumerService {
    /**
     * Check circuit break.
     *
     * @return circuit break info
     */
    @GetMapping("/circuitBreak")
    String circuitBreak();
}
