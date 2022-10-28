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

package cn.polarismesh.agent.bootstrap;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Formatter;
import java.util.Objects;

public class BootLogger {
    private static final String FORMAT = "%tm-%<td %<tT.%<tL %-5s %-35.35s : %s";
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private final String loggerName;
    private final PrintStream out;
    private final PrintStream err;

    public BootLogger(String loggerName) {
        this(loggerName, System.out, System.err);
    }

    BootLogger(String loggerName, PrintStream out, PrintStream err) {
        this.loggerName = Objects.requireNonNull(loggerName, "loggerName");
        this.out = out;
        this.err = err;
    }

    public static BootLogger getLogger(Class clazz) {
        return new BootLogger(clazz.getSimpleName());
    }

    public static BootLogger getLogger(String loggerName) {
        return new BootLogger(loggerName);
    }

    private String format(String logLevel, String msg, Throwable throwable) {
        final long now = System.currentTimeMillis();

        StringBuilder buffer = new StringBuilder(64);
        Formatter formatter = new Formatter(buffer);
        formatter.format(FORMAT, now, logLevel, loggerName, msg);
        if (throwable != null) {
            StringBuffer exceptionMessage = getStackTrace(throwable);
            buffer.append(exceptionMessage);
        } else {
            buffer.append(LINE_SEPARATOR);
        }
        return formatter.toString();
    }

    public void info(String msg) {
        String formatMessage = format("INFO", msg, null);
        this.out.print(formatMessage);
    }


    public void warn(String msg) {
        warn(msg, null);
    }

    public void warn(String msg, Throwable throwable) {
        String formatMessage = format("WARN", msg, throwable);
        this.err.print(formatMessage);
    }

    public void error(String msg) {
        String formatMessage = format("ERROR", msg, null);
        this.err.print(formatMessage);
    }

    private StringBuffer getStackTrace(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        StringWriter sw = new StringWriter(512);
        PrintWriter pw = new PrintWriter(sw);
        pw.println();
        throwable.printStackTrace(pw);
        pw.close();
        return sw.getBuffer();
    }


}
