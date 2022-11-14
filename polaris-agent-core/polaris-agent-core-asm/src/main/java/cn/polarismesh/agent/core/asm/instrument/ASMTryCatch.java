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

package cn.polarismesh.agent.core.asm.instrument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

public class ASMTryCatch {

    private final MethodNode methodNode;
    private final LabelNode startLabelNode = new LabelNode();
    private final LabelNode endLabelNode = new LabelNode();

    public ASMTryCatch(final MethodNode methodNode) {
        this.methodNode = methodNode;

        final TryCatchBlockNode tryCatchBlockNode = new TryCatchBlockNode(this.startLabelNode, this.endLabelNode,
                this.endLabelNode, "java/lang/Throwable");
        if (this.methodNode.tryCatchBlocks == null) {
            this.methodNode.tryCatchBlocks = new ArrayList<>();
        }
        this.methodNode.tryCatchBlocks.add(tryCatchBlockNode);
    }

    public LabelNode getStartLabelNode() {
        return this.startLabelNode;
    }

    public LabelNode getEndLabelNode() {
        return this.endLabelNode;
    }

    public void sort() {
        if (this.methodNode.tryCatchBlocks == null) {
            return;
        }

        Collections.sort(this.methodNode.tryCatchBlocks, new Comparator<TryCatchBlockNode>() {
            @Override
            public int compare(TryCatchBlockNode o1, TryCatchBlockNode o2) {
                return Integer.compare(blockLength(o1), blockLength(o2));
            }

            private int blockLength(TryCatchBlockNode block) {
                final int startidx = methodNode.instructions.indexOf(block.start);
                final int endidx = methodNode.instructions.indexOf(block.end);
                return endidx - startidx;
            }
        });

        // Updates the 'target' of each try catch block annotation.
        for (int i = 0; i < this.methodNode.tryCatchBlocks.size(); i++) {
            this.methodNode.tryCatchBlocks.get(i).updateIndex(i);
        }
    }
}
