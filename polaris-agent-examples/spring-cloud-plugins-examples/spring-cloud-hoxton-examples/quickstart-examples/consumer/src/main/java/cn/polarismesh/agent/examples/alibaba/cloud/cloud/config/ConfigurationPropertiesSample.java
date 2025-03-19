package cn.polarismesh.agent.examples.alibaba.cloud.cloud.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
//@RefreshScope //如果使用反射模式，则不需要加这个注解
@ConfigurationProperties(prefix = "properties")
public class ConfigurationPropertiesSample {
	private String name = "properties-default-name";

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
