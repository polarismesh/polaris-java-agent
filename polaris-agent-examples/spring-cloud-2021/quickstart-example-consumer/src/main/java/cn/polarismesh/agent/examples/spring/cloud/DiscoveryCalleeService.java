package cn.polarismesh.agent.examples.spring.cloud;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author zhuyuhan
 */
@FeignClient("DiscoveryCalleeService")
public interface DiscoveryCalleeService {

    /**
     * 求和计算
     *
     * @param value1 值1
     * @param value2 值2
     * @return 总值
     */
    @GetMapping("/discovery/service/callee/sum")
    int sum(@RequestParam("value1") final int value1, @RequestParam("value2") final int value2);
}
