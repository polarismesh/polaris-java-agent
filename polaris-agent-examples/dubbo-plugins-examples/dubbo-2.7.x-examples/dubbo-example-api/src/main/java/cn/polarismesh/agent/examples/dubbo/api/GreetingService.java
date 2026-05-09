/*
 * Tencent is pleased to support the open source community by making polaris-java-agent available.
 *
 * Copyright (C) 2021 Tencent. All rights reserved.
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

package cn.polarismesh.agent.examples.dubbo.api;

/**
 * Dubbo 示例服务接口
 */
public interface GreetingService {

    /**
     * 问候方法
     *
     * @param name 名字
     * @return 问候语
     */
    String sayHello(String name);

    /**
     * 打招呼方法（用于标签路由演示）
     *
     * @param name 名字
     * @return 问候语（含标签信息）
     */
    String sayHi(String name);
}
