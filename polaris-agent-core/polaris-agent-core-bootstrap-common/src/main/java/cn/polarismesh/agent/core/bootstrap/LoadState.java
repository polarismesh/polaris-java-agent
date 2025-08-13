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

package cn.polarismesh.agent.core.bootstrap;

import java.util.concurrent.atomic.AtomicBoolean;

public class LoadState {

    private static final boolean STATE_NONE = false;
    private static final boolean STATE_STARTED = true;

    private final AtomicBoolean state = new AtomicBoolean(STATE_NONE);

    // for test
    boolean getState() {
        return state.get();
    }

    public boolean start() {
        return state.compareAndSet(STATE_NONE, STATE_STARTED);
    }

}
