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

package cn.polarismesh.agent.core.common.logger;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Objects;

public class StdoutCommonLogger implements CommonLogger {

    private final String messagePattern;
    private final PrintStream out;
    private final PrintStream err;

    public StdoutCommonLogger(String loggerName) {
        this(loggerName, System.out, System.err);
    }

    private StdoutCommonLogger(String loggerName, PrintStream out, PrintStream err) {
        Objects.requireNonNull(loggerName, "loggerName");
        this.out = Objects.requireNonNull(out, "out");
        this.err = Objects.requireNonNull(err, "err");
        this.messagePattern = "{0,date,yyyy-MM-dd HH:mm:ss} [{1}](" + loggerName + ") {2}{3}";
    }

    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    @Override
    public void trace(String msg) {

    }

    @Override
    public boolean isDebugEnabled() {
        return true;
    }

    @Override
    public void debug(String msg) {

    }

    @Override
    public void info(String msg) {
        String message = format("INFO ", msg, "");
        this.out.println(message);
    }

    private String format(String logLevel, String msg, String exceptionMessage) {
        exceptionMessage = defaultString(exceptionMessage, "");

        MessageFormat messageFormat = new MessageFormat(messagePattern);

        final long date = System.currentTimeMillis();
        Object[] parameter = {date, logLevel, msg, exceptionMessage};
        return messageFormat.format(parameter);
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    @Override
    public void warn(String msg) {
        warn(msg, null);
    }

    @Override
    public void warn(String msg, Throwable throwable) {
        String exceptionMessage = toString(throwable);
        String message = format("WARN ", msg, exceptionMessage);
        this.err.println(message);
    }

    @Override
    public void error(String msg) {
        warn(msg, null);
    }

    @Override
    public void error(String msg, Throwable throwable) {
        String exceptionMessage = toString(throwable);
        String message = format("ERROR ", msg, exceptionMessage);
        this.err.println(message);
    }

    private String toString(Throwable throwable) {
        if (throwable == null) {
            return "";
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println();
        throwable.printStackTrace(pw);
        pw.close();
        return sw.toString();
    }

    private String defaultString(String exceptionMessage, String defaultValue) {
        if (exceptionMessage == null) {
            return defaultValue;
        }
        return exceptionMessage;
    }
}
