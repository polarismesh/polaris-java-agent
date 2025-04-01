package cn.polarismesh.agent.examples.alibaba.cloud.cloud;


import cn.polarismesh.agent.examples.alibaba.cloud.cloud.config.ConfigurationPropertiesSample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;


@RestController
@RequestMapping("/")
public class ConsumerController {

	private static final Logger LOG = LoggerFactory.getLogger(ConsumerController.class);

	@Autowired
	@Qualifier("restTemplate")
	private RestTemplate template;

	@Autowired
	@Qualifier("defaultRestTemplate")
	private RestTemplate defaultRestTemplate;

    @Autowired
	private ConsumerService consumerService;

	@Autowired
	private CircuitBreakerFactory circuitBreakerFactory;

	@Autowired
	private ConfigurationPropertiesSample configurationPropertiesSample;

	@GetMapping("/config/name")
	public String getConfigName() {
		LOG.info("getConfigName:{}", configurationPropertiesSample.getName());
		return configurationPropertiesSample.getName();
	}

	@GetMapping("/rest/circuitBreak")
	public String circuitBreakRestTemplate() {
		return circuitBreakerFactory
				.create("service-provider-hoxton#/circuitBreak")
				.run(() -> defaultRestTemplate.getForObject("/circuitBreak", String.class),
						throwable -> "trigger the refuse for service callee."
				);
	}

	@GetMapping("/echo/{str}")
	public ResponseEntity<String> rest(@PathVariable String str) {
		ResponseEntity<String> response = template.getForEntity("http://service-provider-hoxton/echo/" + str,
				String.class);
		LOG.info("response:{}", response);
		return response;
	}

	@GetMapping("/feign/circuitBreak/fallbackFromCode")
	public String circuitBreakFeignFallbackFromCode() {
		return consumerService.circuitBreak();
	}

}
