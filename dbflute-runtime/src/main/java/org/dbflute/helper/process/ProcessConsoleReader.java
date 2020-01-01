/*
 * Copyright 2014-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.dbflute.helper.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.function.Consumer;

/**
 * @author jflute
 * @since 0.9.8.3 (2011/05/03 Tuesday)
 */
public class ProcessConsoleReader extends Thread { // basically memorable code...

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final BufferedReader _reader; // not null
    protected final Consumer<String> _liner; // null allowed
    protected final StringBuilder _consoleSb = new StringBuilder(); // not null

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public ProcessConsoleReader(InputStream ins, String encoding, Consumer<String> liner) {
        assertObjectNotNull("ins", ins);
        assertStringNotNullAndNotTrimmedEmpty("encoding", encoding);
        try {
            _reader = new BufferedReader(new InputStreamReader(ins, encoding));
            _liner = liner;
        } catch (UnsupportedEncodingException e) {
            String msg = "Failed to create a reader by the encoding: " + encoding;
            throw new IllegalStateException(msg);
        }
    }

    // ===================================================================================
    //                                                                                Read
    //                                                                                ====
    public String read() {
        try {
            join(); // needs to use non-thread-safe StringBuilder
        } catch (InterruptedException e) {
            String msg = "The join() was interrupted.";
            throw new IllegalStateException(msg, e);
        }
        return _consoleSb.toString();
    }

    // ===================================================================================
    //                                                                              Thread
    //                                                                              ======
    @Override
    public void run() {
        final StringBuilder sb = _consoleSb;
        try {
            while (true) {
                final String line = _reader.readLine();
                if (line == null) {
                    break;
                }
                handleLine(sb, line);
            }
        } catch (IOException e) {
            String msg = "Failed to read the stream: " + _reader;
            throw new IllegalStateException(msg, e);
        } finally {
            try {
                _reader.close();
            } catch (IOException ignored) {}
        }
    }

    protected void handleLine(StringBuilder sb, String line) {
        if (_liner != null) {
            _liner.accept(line);
        }
        if (sb.length() > 0) {
            sb.append("\n");
        }
        sb.append(line);
    }

    // ===================================================================================
    //                                                                       Assert Helper
    //                                                                       =============
    // -----------------------------------------------------
    //                                         Assert Object
    //                                         -------------
    protected void assertObjectNotNull(String variableName, Object value) {
        if (variableName == null) {
            String msg = "The value should not be null: variableName=null value=" + value;
            throw new IllegalArgumentException(msg);
        }
        if (value == null) {
            String msg = "The value should not be null: variableName=" + variableName;
            throw new IllegalArgumentException(msg);
        }
    }

    // -----------------------------------------------------
    //                                         Assert String
    //                                         -------------
    protected void assertStringNotNullAndNotTrimmedEmpty(String variableName, String value) {
        assertObjectNotNull("variableName", variableName);
        assertObjectNotNull(variableName, value);
        if (value.trim().length() == 0) {
            String msg = "The value should not be empty: variableName=" + variableName + " value=" + value;
            throw new IllegalArgumentException(msg);
        }
    }

}
