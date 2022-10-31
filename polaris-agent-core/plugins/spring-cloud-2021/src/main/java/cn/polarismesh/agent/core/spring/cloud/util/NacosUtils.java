package cn.polarismesh.agent.core.spring.cloud.util;

import cn.polarismesh.agent.common.exception.PolarisAgentException;
import cn.polarismesh.agent.common.tools.ReflectionUtils;
import cn.polarismesh.common.polaris.PolarisReflectConst;
import com.tencent.polaris.api.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.client.serviceregistry.Registration;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class NacosUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(NacosUtils.class);

	public static final String DEFAULT_GROUP = "DEFAULT_GROUP";

	public static String resolveGroupName(Registration registration) {
		try {
			Object nacosDiscoveryProperties = ReflectionUtils
					.invokeMethodByName(registration, "getNacosDiscoveryProperties", null);
			if (null == nacosDiscoveryProperties) {
				return "";
			}
			return (String) ReflectionUtils.invokeMethodByName(nacosDiscoveryProperties, "getGroup", null);
		}
		catch (PolarisAgentException e) {
			LOGGER.error("fail to resolve nacos group name", e);
		}
		return "";
	}

	public static String resolveNamespace(Registration registration) {
		try {
			Object nacosDiscoveryProperties = ReflectionUtils
					.invokeMethodByName(registration, "getNacosDiscoveryProperties", null);
			if (null == nacosDiscoveryProperties) {
				return "";
			}
			String namespace = (String) ReflectionUtils.invokeMethodByName(nacosDiscoveryProperties, "getNamespace", null);
			return namespace;
		}
		catch (PolarisAgentException e) {
			LOGGER.error("fail to resolve nacos namespace", e);
		}
		return "";
	}


	public static int resolveWeight(Registration registration) {
		try {
			float registerWeight = (float) ReflectionUtils
					.invokeMethodByName(registration, "getRegisterWeight", null);
			return (int) (registerWeight * PolarisReflectConst.POLARIS_DEFAULT_WEIGHT);
		}
		catch (PolarisAgentException e) {
			LOGGER.error("fail to resolve nacos weight", e);
		}
		return PolarisReflectConst.POLARIS_DEFAULT_WEIGHT;
	}

}
