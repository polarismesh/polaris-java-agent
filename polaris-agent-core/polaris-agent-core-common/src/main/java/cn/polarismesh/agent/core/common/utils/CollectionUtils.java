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

package cn.polarismesh.agent.core.common.utils;

import java.util.Collection;

public final class CollectionUtils {

    private CollectionUtils() {
    }

    public static int nullSafeSize(final Collection<?> collection) {
        return nullSafeSize(collection, 0);
    }

    public static int nullSafeSize(final Collection<?> collection, final int nullValue) {
        if (collection == null) {
            return nullValue;
        }
        return collection.size();
    }

    public static boolean isEmpty(final Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean hasLength(final Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }


}
