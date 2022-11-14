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

package cn.polarismesh.agent.core.asm.instrument.plugin;

import java.util.List;
import java.util.Objects;

public class ClassNameFilterChain implements ClassNameFilter {

	private final ClassNameFilter[] filterChain;

	public ClassNameFilterChain(List<ClassNameFilter> filterChain) {
		Objects.requireNonNull(filterChain, "filterChain");
		this.filterChain = filterChain.toArray(new ClassNameFilter[0]);
	}


	@Override
	public boolean accept(String className) {
		for (ClassNameFilter classNameFilter : this.filterChain) {
			if (!classNameFilter.accept(className)) {
				return REJECT;
			}
		}
		return ACCEPT;
	}
}
