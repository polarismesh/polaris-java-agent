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

package cn.polarismesh.agent.plugin.spring.cloud.interceptor.filter.servlet.ratelimit;

import cn.polarismesh.agent.plugin.spring.cloud.common.Holder;
import com.tencent.cloud.common.metadata.MetadataContext;
import com.tencent.cloud.polaris.ratelimit.resolver.RateLimitRuleArgumentServletResolver;
import com.tencent.cloud.polaris.ratelimit.utils.QuotaCheckUtils;
import com.tencent.polaris.api.utils.StringUtils;
import com.tencent.polaris.ratelimit.api.rpc.Argument;
import com.tencent.polaris.ratelimit.api.rpc.QuotaResponse;
import com.tencent.polaris.ratelimit.api.rpc.QuotaResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static com.tencent.cloud.polaris.ratelimit.constant.RateLimitConstant.LABEL_METHOD;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class RateLimitHandlerAdapter implements HandlerAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitHandlerAdapter.class);

	private final AtomicReference<RateLimitRuleArgumentServletResolver> reference = new AtomicReference<>();

	private final HandlerAdapter adapter;

	public RateLimitHandlerAdapter(HandlerAdapter adapter) {
		this.adapter = adapter;
	}

	@Override
	public boolean supports(Object handler) {
		return adapter.supports(handler);
	}

	@Override
	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		reference.compareAndSet(null, new RateLimitRuleArgumentServletResolver(Holder.newServiceRuleManager(), null));

		String localNamespace = MetadataContext.LOCAL_NAMESPACE;
		String localService = MetadataContext.LOCAL_SERVICE;

		Set<Argument> labels = getRequestLabels(request, localNamespace, localService);
		try {
			QuotaResponse quotaResponse = QuotaCheckUtils.getQuota(Holder.getContextManager().getLimitAPI(),
					localNamespace, localService, 1, labels, request.getRequestURI());

			if (quotaResponse.getCode() == QuotaResultCode.QuotaResultLimited) {
				response.setStatus(Holder.getRateLimitProperties().getRejectHttpCode());
				response.getWriter().println(Holder.getRateLimitProperties().getRejectRequestTips());
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

	private Set<Argument> getRequestLabels(HttpServletRequest request, String localNamespace, String localService) {
		Map<String, String> labels = new HashMap<>();

		// add build in labels
		String path = request.getRequestURI();
		if (StringUtils.isNotBlank(path)) {
			labels.put(LABEL_METHOD, path);
		}
		return reference.get().getArguments(request, localNamespace, localService);
	}

}