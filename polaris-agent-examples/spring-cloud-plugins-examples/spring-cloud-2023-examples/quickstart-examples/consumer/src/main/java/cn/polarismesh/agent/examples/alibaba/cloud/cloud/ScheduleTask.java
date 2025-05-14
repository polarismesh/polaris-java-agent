package cn.polarismesh.agent.examples.alibaba.cloud.cloud;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@EnableScheduling
@Service
public class ScheduleTask {
    private static final Logger LOG = LoggerFactory.getLogger(ScheduleTask.class);

    @Autowired
    @Qualifier("restTemplate")
    private RestTemplate template;

    @Autowired
    private CircuitBreakerFactory circuitBreakerFactory;

    @Value("${consumer.auto.test.enabled:false}")
    private Boolean autoTest;

    @Scheduled(fixedDelayString = "${consumer.auto.test.interval:30000}")
    public void autoSendRequest() {
        if (!autoTest) {
            LOG.info("自动请求关闭");
            return;
        }

        String[] testStrings = {"test1", "test2", "auto-test"};
        var circuitBreaker = circuitBreakerFactory.create("autoRequestCircuitBreaker");

        for (String str : testStrings) {
            try {
                ResponseEntity<String> response = circuitBreaker.run(
                        () -> template.getForEntity("http://service-provider-2023/echo/" + str, String.class),
                        throwable -> {
                            LOG.error("自动请求失败: {}", throwable.getMessage(), throwable);
                            return ResponseEntity.status(503).body("服务暂不可用");
                        });
                LOG.info("自动请求[{}]响应: {}", str, response.getBody());
            } catch (Exception e) {
                LOG.error("自动请求异常: {}", e.getMessage(), e);
            }
        }
    }

}
