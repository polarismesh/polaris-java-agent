/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.polarismesh.agent.core.bootstrap.util;


import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;

public final class PropertyUtils {

    public static final String DEFAULT_ENCODING = StandardCharsets.UTF_8.name();

    public interface InputStreamFactory {
        InputStream openInputStream() throws IOException;
    }

    private PropertyUtils() {
    }

    public static Properties loadProperty(Properties properties, InputStreamFactory inputStreamFactory) throws IOException {
        Objects.requireNonNull(properties, "properties");
        Objects.requireNonNull(inputStreamFactory, "inputStreamFactory");

        InputStream in = null;
        Reader reader = null;
        try {
            in = inputStreamFactory.openInputStream();
            reader = new InputStreamReader(in, DEFAULT_ENCODING);
            properties.load(reader);
        } finally {
            closeQuietly(reader);
            closeQuietly(in);
        }
        return properties;
    }

    public static class FileInputStreamFactory implements  InputStreamFactory {
        private final String filePath;

        public FileInputStreamFactory(String filePath) {
            this.filePath = Objects.requireNonNull(filePath, "filePath");
        }

        @Override
        public InputStream openInputStream() throws IOException {
            return new FileInputStream(filePath);
        }
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignore) {
                // skip
            }
        }
    }

}
