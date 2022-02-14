package cn.polarismesh.agent.examples.spring.cloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author zhuyuhan
 */
@SpringBootApplication
public class DiscoveryCalleeService {

    public static void main(String[] args) {
        SpringApplication.run(DiscoveryCalleeService.class, args);
    }
}
