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


package cn.polarismesh.agent.core.spring.cloud.filter.ratelimit;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.polarismesh.agent.core.spring.cloud.filter.router.ScRouterServletWebFilterInterceptor;
import cn.polarismesh.common.polaris.PolarisSingleton;
import com.tencent.cloud.common.metadata.MetadataContext;
import com.tencent.cloud.common.util.expresstion.ServletExpressionLabelUtils;
import com.tencent.cloud.polaris.context.ServiceRuleManager;
import com.tencent.cloud.polaris.ratelimit.RateLimitRuleLabelResolver;
import com.tencent.cloud.polaris.ratelimit.utils.QuotaCheckUtils;
import com.tencent.polaris.api.utils.StringUtils;
import com.tencent.polaris.ratelimit.api.rpc.QuotaResponse;
import com.tencent.polaris.ratelimit.api.rpc.QuotaResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;

import static com.tencent.cloud.polaris.ratelimit.constant.RateLimitConstant.LABEL_METHOD;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class ScLimitHandlerAdapter implements HandlerAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScLimitHandlerAdapter.class);

	private final AtomicReference<RateLimitRuleLabelResolver> reference = new AtomicReference<>();

	private final HandlerAdapter adapter;

	public ScLimitHandlerAdapter(HandlerAdapter adapter) {
		this.adapter = adapter;
	}

	@Override
	public boolean supports(Object handler) {
		return adapter.supports(handler);
	}

	@Override
	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		reference.compareAndSet(null, new RateLimitRuleLabelResolver(new ServiceRuleManager(PolarisSingleton.getPolarisOperator()
				.getSdkContext())));

		String localNamespace = MetadataContext.LOCAL_NAMESPACE;
		String localService = MetadataContext.LOCAL_SERVICE;

		Map<String, String> labels = getRequestLabels(request, localNamespace, localService);

		try {
			QuotaResponse quotaResponse = QuotaCheckUtils.getQuota(PolarisSingleton.getPolarisOperator().getLimitAPI(),
					localNamespace, localService, 1, labels, request.getRequestURI());

			if (quotaResponse.getCode() == QuotaResultCode.QuotaResultLimited) {
				response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
				return null;
			}
			// Unirate
			if (quotaResponse.getCode() == QuotaResultCode.QuotaResultOk && quotaResponse.getWaitMs() > 0) {
				LOGGER.debug("The request of [{}] will waiting for {}ms.", request.getRequestURI(), quotaResponse.getWaitMs());
				Thread.sleep(quotaResponse.getWaitMs());
			}

		}
		catch (Throwable t) {
			// An exception occurs in the rate limiting API call,
			// which should not affect the call of the business process.
			LOGGER.error("fail to invoke getQuota, service is " + localService, t);
		}

		return adapter.handle(request, response, handler);
	}

	@Override
	public long getLastModified(HttpServletRequest request, Object handler) {
		return adapter.getLastModified(request, handler);
	}

	private Map<String, String> getRequestLabels(HttpServletRequest request, String localNamespace, String localService) {
		Map<String, String> labels = new HashMap<>();

		// add build in labels
		String path = request.getRequestURI();
		if (StringUtils.isNotBlank(path)) {
			labels.put(LABEL_METHOD, path);
		}

		// add rule expression labels
		Map<String, String> expressionLabels = getRuleExpressionLabels(request, localNamespace, localService);
		labels.putAll(expressionLabels);

		return labels;
	}

	private Map<String, String> getRuleExpressionLabels(HttpServletRequest request, String namespace, String service) {
		Set<String> expressionLabels = reference.get().getExpressionLabelKeys(namespace, service);
		return ServletExpressionLabelUtils.resolve(request, expressionLabels);
	}
}
