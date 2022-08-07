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

package cn.polarismesh.agent.core.nacos.v1.interceptor;

import cn.polarismesh.agent.core.nacos.v1.constants.NacosConstants;
import cn.polarismesh.agent.core.nacos.v1.handler.HandlerManager;
import cn.polarismesh.common.interceptor.AbstractInterceptor;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * nacos sdk1.x版本NamingProxy拦截类
 *
 * @author bruceppeng
 */
public class NacosV1NamingProxyInterceptor implements AbstractInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(NacosV1NamingProxyInterceptor.class);

    private HandlerManager handlerManager;

    public NacosV1NamingProxyInterceptor() {
        handlerManager = new HandlerManager();

    }

    @Override
    public void before(Object target, Object[] args) {

    }

    @Override
    @SuppressWarnings("unchecked")
    public void after(Object target, Object[] args, Object result, Throwable throwable) {

        //仅针对no exception场景进行后续处理
        if (Objects.isNull(throwable)) {
            int argsLen = args.length;
            //组装请求api以及HttpMethod生成handlerName
            String handlerName = args[0] + NacosConstants.LINK_FLAG + args[argsLen-1];
            try {
                handlerManager.handler(handlerName,target,args,result);
            }catch (Exception exp){
                LOGGER.error("NacosV1NamingProxyInterceptor after error, handlerName:{},errMsg:{}", handlerName, exp.getMessage());
            }
        }
    }
}
