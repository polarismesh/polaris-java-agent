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

package cn.polarismesh.agent.plugin.spring.cloud.interceptor.aware;

import cn.polarismesh.agent.plugin.spring.cloud.common.Holder;
import cn.polarismesh.agent.plugin.spring.cloud.interceptor.BaseInterceptor;
import com.tencent.cloud.common.util.ApplicationContextAwareUtils;
import com.tencent.polaris.api.utils.StringUtils;
import com.tencent.polaris.logging.LoggingConsts;
import com.tencent.polaris.logging.PolarisLogging;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * {@link org.springframework.context.support.ApplicationContextAwareProcessor#ApplicationContextAwareProcessor(ConfigurableApplicationContext)}
 */
public class ApplicationContextAwareInterceptor extends BaseInterceptor {

	@Override
	public void onAfter(Object target, Object[] args, Object result, Throwable throwable) {
		ConfigurableApplicationContext context = (ConfigurableApplicationContext) args[0];
		ApplicationContextAwareUtils utils = new ApplicationContextAwareUtils();

		// MetadataContext 需要读取到 agent 配置的内容
		AnnotationConfigApplicationContext tmpCtx = new AnnotationConfigApplicationContext((DefaultListableBeanFactory) context.getBeanFactory());
		tmpCtx.setEnvironment((ConfigurableEnvironment) Holder.getEnvironment());
		utils.setApplicationContext(tmpCtx);

		// 设置为真正的 ApplicationContext
		utils.setApplicationContext(context);

		// init polaris logging
		String loggingPath = Holder.getEnvironment().getProperty("spring.cloud.polaris.logging.path");
		if (StringUtils.isNotBlank(loggingPath)) {
			System.setProperty(LoggingConsts.LOGGING_PATH_PROPERTY, loggingPath);
		}
		PolarisLogging.getInstance().loadConfiguration();
	}
}
