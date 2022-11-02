/*
 * Tencent is pleased to support the open source community by making Polaris available.
 *
 * Copyright (C) 2019 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package cn.polarismesh.agent.core.spring.cloud;

import com.tencent.cloud.common.metadata.StaticMetadataManager;
import com.tencent.cloud.common.metadata.config.MetadataLocalProperties;
import com.tencent.cloud.polaris.context.config.PolarisContextProperties;

import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class Holder {

	private static final MetadataLocalProperties localProperties = new MetadataLocalProperties();

	private static final StaticMetadataManager staticMetadataManager = new StaticMetadataManager(localProperties, null);

	private static final PolarisContextProperties polarisContextProperties = new PolarisContextProperties();

	static {
		try (InetUtils utils = new InetUtils(new InetUtilsProperties())) {
			polarisContextProperties.setLocalIpAddress(utils.findFirstNonLoopbackHostInfo().getIpAddress());
		}
	}

	public static MetadataLocalProperties getLocalProperties() {
		return localProperties;
	}

	public static StaticMetadataManager getStaticMetadataManager() {
		return staticMetadataManager;
	}

	public static PolarisContextProperties getPolarisContextProperties() {
		return polarisContextProperties;
	}

}
