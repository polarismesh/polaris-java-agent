/*
 * Copyright 2002-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.polarismesh.agent.core.spring.cloud.util;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import cn.polarismesh.agent.core.spring.cloud.model.PolarisServiceInstance;
import com.tencent.polaris.api.pojo.DefaultInstance;
import com.tencent.polaris.api.pojo.DefaultServiceInstances;
import com.tencent.polaris.api.pojo.Instance;
import com.tencent.polaris.api.pojo.ServiceInstances;
import com.tencent.polaris.api.pojo.ServiceKey;
import com.tencent.polaris.api.utils.CollectionUtils;
import reactor.core.publisher.Flux;

import org.springframework.cloud.client.ServiceInstance;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class LoadbalancerUtils {

	public static ServiceInstances toServiceInstances(Flux<List<ServiceInstance>> servers) {
		AtomicReference<List<Instance>> instances = new AtomicReference<>();
		servers.subscribe(serviceInstances -> {
			instances.set(serviceInstances.stream().map(serviceInstance -> {
				DefaultInstance instance = new DefaultInstance();
				instance.setNamespace(serviceInstance.getMetadata().get("namespace"));
				instance.setService(serviceInstance.getServiceId());
				instance.setProtocol(serviceInstance.getScheme());
				instance.setId(serviceInstance.getInstanceId());
				instance.setHost(serviceInstance.getHost());
				instance.setPort(serviceInstance.getPort());
				instance.setWeight(100);
				instance.setMetadata(serviceInstance.getMetadata());

				if (serviceInstance instanceof PolarisServiceInstance) {
					PolarisServiceInstance polarisServiceInstance = (PolarisServiceInstance) serviceInstance;
					instance.setRegion(polarisServiceInstance.getPolarisInstance().getRegion());
					instance.setZone(polarisServiceInstance.getPolarisInstance().getZone());
					instance.setCampus(polarisServiceInstance.getPolarisInstance().getCampus());
				}

				return instance;
			}).collect(Collectors.toList()));
		});

		String serviceName = null;
		if (CollectionUtils.isEmpty(instances.get())) {
			instances.set(Collections.emptyList());
		}
		else {
			serviceName = instances.get().get(0).getService();
		}

		ServiceKey serviceKey = new ServiceKey("", serviceName);
		return new DefaultServiceInstances(serviceKey, instances.get());
	}

}
