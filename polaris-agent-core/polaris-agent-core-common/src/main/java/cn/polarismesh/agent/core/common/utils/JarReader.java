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

import cn.polarismesh.agent.core.common.logger.CommonLogger;
import cn.polarismesh.agent.core.common.logger.StdoutCommonLoggerFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarReader {

    private final CommonLogger logger = StdoutCommonLoggerFactory.INSTANCE.getLogger(getClass().getName());

    private static final int BUFFER_SIZE = 1024 * 4;

    private final JarFile jarFile;

    public JarReader(JarFile jarFile) {
        this.jarFile = Objects.requireNonNull(jarFile, "jarFile");
    }

    public InputStream getInputStream(String name) throws IOException {
        final JarEntry jarEntry = this.jarFile.getJarEntry(name);
        if (jarEntry != null) {
            return this.jarFile.getInputStream(jarEntry);
        }

        return null;
    }

    public List<FileBinary> read(JarEntryFilter jarEntryFilter) throws IOException {
        Objects.requireNonNull(jarEntryFilter, "jarEntryFilter");

        final BufferedContext bufferedContext = new BufferedContext();

        Enumeration<JarEntry> entries = jarFile.entries();
        List<FileBinary> fileBinaryList = new ArrayList<>();
        while (entries.hasMoreElements()) {
            final JarEntry jarEntry = entries.nextElement();
            if (jarEntryFilter.filter(jarEntry)) {
                FileBinary fileBinary = newFileBinary(bufferedContext, jarEntry);
                fileBinaryList.add(fileBinary);
            }
        }
        return fileBinaryList;
    }

    private FileBinary newFileBinary(BufferedContext bufferedContext, JarEntry jarEntry) throws IOException {
        byte[] binary = bufferedContext.read(jarEntry);
        return new FileBinary(jarEntry.getName(), binary);
    }

    private class BufferedContext {

        private final byte[] buffer = new byte[BUFFER_SIZE];
        private final ByteArrayOutputStream output = new ByteArrayOutputStream(BUFFER_SIZE);

        private BufferedContext() {
        }

        private byte[] read(JarEntry jarEntry) throws IOException {
            InputStream inputStream = null;
            try {
                inputStream = jarFile.getInputStream(jarEntry);
                if (inputStream == null) {
                    logger.warn(
                            String.format("jarEntry not found. jarFile:%s jarEntry %s", jarFile.getName(), jarEntry));
                    return null;
                }
                return read(inputStream);
            } catch (IOException ioe) {
                logger.warn(String.format("jarFile read error jarFile:%s jarEntry %s %s", jarFile.getName(), jarEntry,
                        ioe.getMessage()), ioe);
                throw ioe;
            } finally {
                IOUtils.closeQuietly(inputStream);
            }
        }

        public byte[] read(InputStream input) throws IOException {
            this.output.reset();
            IOUtils.copy(input, output, buffer);
            return output.toByteArray();
        }

    }
}
