package cn.polarismesh.agent.core.spring.cloud.filter.router;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tencent.cloud.common.metadata.MetadataContextHolder;
import com.tencent.cloud.common.util.JacksonUtils;
import com.tencent.cloud.metadata.core.CustomTransitiveMetadataResolver;
import com.tencent.cloud.metadata.core.TransHeadersTransfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;

import static com.tencent.cloud.common.constant.ContextConstant.UTF_8;
import static com.tencent.cloud.common.constant.MetadataConstant.HeaderName.CUSTOM_DISPOSABLE_METADATA;
import static com.tencent.cloud.common.constant.MetadataConstant.HeaderName.CUSTOM_METADATA;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class ScRouterHandlerAdapter implements HandlerAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScRouterHandlerAdapter.class);

	private final HandlerAdapter adapter;

	public ScRouterHandlerAdapter(HandlerAdapter adapter) {
		this.adapter = adapter;
	}

	@Override
	public boolean supports(Object handler) {
		return adapter.supports(handler);
	}

	@Override
	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		Map<String, String> internalTransitiveMetadata = getInternalMetadata(request, CUSTOM_METADATA);
		Map<String, String> customTransitiveMetadata = CustomTransitiveMetadataResolver.resolve(request);

		Map<String, String> mergedTransitiveMetadata = new HashMap<>();
		mergedTransitiveMetadata.putAll(internalTransitiveMetadata);
		mergedTransitiveMetadata.putAll(customTransitiveMetadata);

		Map<String, String> internalDisposableMetadata = getInternalMetadata(request, CUSTOM_DISPOSABLE_METADATA);
		Map<String, String> mergedDisposableMetadata = new HashMap<>(internalDisposableMetadata);

		MetadataContextHolder.init(mergedTransitiveMetadata, mergedDisposableMetadata);

		TransHeadersTransfer.transfer(request);
		return adapter.handle(request, response, handler);
	}

	@Override
	public long getLastModified(HttpServletRequest request, Object handler) {
		return adapter.getLastModified(request, handler);
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
