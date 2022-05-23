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

package cn.polarismesh.agent.plugin.dubbo2.polaris;

import com.tencent.polaris.api.utils.StringUtils;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.ExtensionLoader;
import org.apache.dubbo.registry.ListenerRegistryWrapper;
import org.apache.dubbo.registry.Registry;
import org.apache.dubbo.registry.RegistryFactory;
import org.apache.dubbo.registry.RegistryServiceListener;

import java.util.Collections;

public class PolarisRegistryFactory implements RegistryFactory {

    private final RegistryFactory registryFactory;

    public PolarisRegistryFactory(RegistryFactory registryFactory) {
        this.registryFactory = registryFactory;
    }

    @Override
    public Registry getRegistry(URL url) {
        String protocol = url.getProtocol();

        boolean isPolaris = StringUtils.equals(protocol, "polaris");

        if (isPolaris) {
            return new ListenerRegistryWrapper(this.registryFactory.getRegistry(url),
                    Collections.unmodifiableList(ExtensionLoader.getExtensionLoader(RegistryServiceListener.class)
                            .getActivateExtension(url, "registry.listeners")));
        }

        return new PolarisRegistry(url);


    }
}
