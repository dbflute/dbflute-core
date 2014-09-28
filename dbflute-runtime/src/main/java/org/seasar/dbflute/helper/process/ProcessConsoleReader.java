/*
 * Copyright 2004-2014 the Seasar Foundation and the Others.
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
package org.seasar.dbflute.helper.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * @author jflute
 * @since 0.9.8.3 (2011/05/03 Tuesday)
 */
public class ProcessConsoleReader extends Thread {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final BufferedReader _reader;
    protected final StringBuilder _consoleSb = new StringBuilder();

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public ProcessConsoleReader(InputStream ins, String encoding) {
        encoding = encoding != null ? encoding : "UTF-8";
        try {
            _reader = new BufferedReader(new InputStreamReader(ins, encoding));
        } catch (UnsupportedEncodingException e) {
            String msg = "Failed to create a reader by the encoding: " + encoding;
            throw new IllegalStateException(msg);
        }
    }

    // ===================================================================================
    //                                                                                Read
    //                                                                                ====
    public String read() {
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
                if (sb.length() > 0) {
                    sb.append("\n");
                }
                sb.append(line);
            }
        } catch (IOException e) {
            String msg = "Failed to read the stream: " + _reader;
            throw new IllegalStateException(msg, e);
        } finally {
            try {
                _reader.close();
            } catch (IOException ignored) {
            }
        }
    }
}
