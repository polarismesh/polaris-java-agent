package cn.polarismesh.agent.examples.alibaba.cloud.cloud;


import cn.polarismesh.agent.examples.alibaba.cloud.cloud.config.ConfigurationPropertiesSample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class ConfigController {

	private static final Logger LOG = LoggerFactory.getLogger(ConfigController.class);

	@Value("${spring.application.name:}")
	private String applicationName;

	@Value("${spring.cloud.client.ip-address:127.0.0.1}")
	private String ip;

	@Autowired
	private ConfigurationPropertiesSample configurationPropertiesSample;

	@RequestMapping(value = "/config/reflect", method = RequestMethod.GET)
	public String reflect() {
		String result = String.format("from application:%s, host-ip: %s, configurationPropertiesSample: %s",
				applicationName, ip, configurationPropertiesSample.getName());
		LOG.info(result);
		return result;
	}

}
