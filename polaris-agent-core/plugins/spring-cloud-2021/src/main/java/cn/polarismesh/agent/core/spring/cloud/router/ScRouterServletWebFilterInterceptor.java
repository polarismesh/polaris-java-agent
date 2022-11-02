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


package cn.polarismesh.agent.core.spring.cloud.router;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.polarismesh.agent.core.spring.cloud.BaseInterceptor;
import com.tencent.cloud.common.metadata.MetadataContextHolder;
import com.tencent.cloud.common.util.JacksonUtils;
import com.tencent.cloud.metadata.core.CustomTransitiveMetadataResolver;
import com.tencent.cloud.metadata.core.TransHeadersTransfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.util.StringUtils;

import static com.tencent.cloud.common.constant.ContextConstant.UTF_8;
import static com.tencent.cloud.common.constant.MetadataConstant.HeaderName.CUSTOM_DISPOSABLE_METADATA;
import static com.tencent.cloud.common.constant.MetadataConstant.HeaderName.CUSTOM_METADATA;


/**
 * hack {@link org.springframework.boot.web.servlet.RegistrationBean#onStartup(ServletContext)}
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class ScRouterServletWebFilterInterceptor extends BaseInterceptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScRouterServletWebFilterInterceptor.class);

	/**
	 * 针对入参的 List<WebFilter> filters 进行处理，添加自定义收集流量标签的 WebFilter
	 *
	 * @param target
	 * @param args
	 */
	@Override
	public void before(Object target, Object[] args) {
		HttpServletRequest request = (HttpServletRequest) args[0];
		HttpServletResponse response = (HttpServletResponse) args[1];

		LOGGER.debug("[PolarisAgent] {} begin exec transfer metadata ability", target.getClass().getCanonicalName());
		Map<String, String> internalTransitiveMetadata = getInternalMetadata(request, CUSTOM_METADATA);
		Map<String, String> customTransitiveMetadata = CustomTransitiveMetadataResolver.resolve(request);

		Map<String, String> mergedTransitiveMetadata = new HashMap<>();
		mergedTransitiveMetadata.putAll(internalTransitiveMetadata);
		mergedTransitiveMetadata.putAll(customTransitiveMetadata);

		Map<String, String> internalDisposableMetadata = getInternalMetadata(request, CUSTOM_DISPOSABLE_METADATA);
		Map<String, String> mergedDisposableMetadata = new HashMap<>(internalDisposableMetadata);

		MetadataContextHolder.init(mergedTransitiveMetadata, mergedDisposableMetadata);

		TransHeadersTransfer.transfer(request);
		LOGGER.debug("[PolarisAgent] {} finished exec transfer metadata ability", target.getClass().getCanonicalName());
	}

	@Override
	public void after(Object target, Object[] args, Object result, Throwable throwable) {
	}

	/**
	 * TrafficServletFilter
	 */
	private static Map<String, String> getInternalMetadata(HttpServletRequest httpServletRequest, String headerName) {
		// Get custom metadata string from http header.
		String customMetadataStr = httpServletRequest.getHeader(headerName);
		try {
			if (StringUtils.hasText(customMetadataStr)) {
				customMetadataStr = URLDecoder.decode(customMetadataStr, UTF_8);
			}
		}
		catch (UnsupportedEncodingException e) {
			LOGGER.error("Runtime system does not support utf-8 coding.", e);
		}
		LOGGER.debug("Get upstream metadata string: {}", customMetadataStr);

		// create custom metadata.
		return JacksonUtils.deserialize2Map(customMetadataStr);
	}

}
