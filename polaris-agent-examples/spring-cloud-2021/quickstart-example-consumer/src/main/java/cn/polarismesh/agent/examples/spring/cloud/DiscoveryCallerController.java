package cn.polarismesh.agent.examples.spring.cloud;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @author zhuyuhan
 */
@RestController
@RequestMapping("/discovery/service/caller")
public class DiscoveryCallerController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DiscoveryCalleeService discoveryCalleeService;

    @Configuration
    @LoadBalancerClient(value = "service-provider")
    class DiscoveryCallerConfiguration {

    }

    /**
     * 获取相加完的结果
     *
     * @param value1 值1
     * @param value2 值2
     * @return 总值
     */
    @GetMapping("/feign")
    public int feign(@RequestParam int value1, @RequestParam int value2) {
        return discoveryCalleeService.sum(value1, value2);
    }

    /**
     * 获取被调服务信息
     *
     * @return 信息
     */
    @GetMapping("/rest")
    public String rest() {
        return restTemplate.getForObject("http://DiscoveryCalleeService/discovery/service/callee/info", String.class);
    }

}
