package cn.polarismesh.agent.examples.spring.cloud;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhuyuhan
 */
@RestController
@RequestMapping("/discovery/service/callee")
public class DiscoveryCalleeController {

    /**
     * 获取当前服务的信息
     *
     * @return 返回服务信息
     */
    @GetMapping("/info")
    public String info() {
        return "Discovery Service Callee With Polaris Java Agent";
    }

    /**
     * 获取相加完的结果
     *
     * @param value1 值1
     * @param value2 值2
     * @return 总值
     */
    @GetMapping("/sum")
    public int sum(@RequestParam int value1, @RequestParam int value2) {
        return value1 + value2;
    }

}
