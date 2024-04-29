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

package cn.polarismesh.agent.plugin.spring.cloud.interceptor;

import cn.polarismesh.agent.core.common.exception.PolarisAgentException;
import cn.polarismesh.agent.core.extension.interceptor.Interceptor;
import cn.polarismesh.agent.plugin.spring.cloud.common.Holder;
import com.tencent.cloud.common.metadata.MetadataContextHolder;
import org.springframework.util.ReflectionUtils;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public abstract class BaseInterceptor implements Interceptor {

	private static final AtomicBoolean initialize = new AtomicBoolean(false);

	@Override
	public final void before(Object target, Object[] args) {
		init();
		onBefore(target, args);
	}

	@Override
	public final void after(Object target, Object[] args, Object result, Throwable throwable) {
		init();
		onAfter(target, args, result, throwable);
	}

	public void onBefore(Object target, Object[] args) {

	}

	public void onAfter(Object target, Object[] args, Object result, Throwable throwable) {

	}


	private void init() {
		if (initialize.compareAndSet(false, true)) {
			try {
				Holder.init();

				Field field = MetadataContextHolder.class.getDeclaredField("metadataLocalProperties");
				field.setAccessible(true);
				ReflectionUtils.setField(field, null, Holder.getLocalProperties());

				field = MetadataContextHolder.class.getDeclaredField("staticMetadataManager");
				field.setAccessible(true);
				ReflectionUtils.setField(field, null, Holder.getStaticMetadataManager());
			}
			catch (Exception e) {
				throw new PolarisAgentException("setValueByFieldName", e);
			}
		}
	}
}
