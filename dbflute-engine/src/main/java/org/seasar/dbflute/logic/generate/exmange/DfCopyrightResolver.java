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
package org.seasar.dbflute.logic.generate.exmange;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

/**
 * @author jflute
 */
public class DfCopyrightResolver {

    protected final String _sourceEncoding;
    protected final String _sourceLn;

    public DfCopyrightResolver(String sourceEncoding, String sourceLn) {
        _sourceEncoding = sourceEncoding;
        _sourceLn = sourceLn;
    }

    public void reflectAllExCopyright(String path, String copyright) {
        if (copyright == null || copyright.trim().length() == 0) {
            return;
        }
        final File exfile = new File(path);
        final String encoding = _sourceEncoding;
        final BufferedReader br;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(exfile), encoding));
        } catch (UnsupportedEncodingException e) {
            String msg = "The encoding is unsupported: encoding=" + encoding;
            throw new IllegalStateException(msg, e);
        } catch (FileNotFoundException e) {
            String msg = "The file of extended class was NOT found: exfile=" + exfile;
            throw new IllegalStateException(msg, e);
        }
        final StringBuilder sb = new StringBuilder();
        final String sourceCodeLn = _sourceLn;
        String line = null;
        int index = 0;
        try {
            while (true) {
                line = br.readLine();
                if (line == null) {
                    break;
                }
                if (index == 0) { // first line
                    if (!line.trim().startsWith("package ")) { // unsupported
                        return;
                    }
                    sb.append(copyright);
                }
                sb.append(line);
                sb.append(sourceCodeLn);
                ++index;
            }
        } catch (IOException e) {
            String msg = "bufferedReader.readLine() threw the exception: current line=" + line;
            throw new IllegalStateException(msg, e);
        } finally {
            try {
                br.close();
            } catch (IOException ignored) {
            }
        }
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(exfile), encoding));
            bw.write(sb.toString());
            bw.flush();
        } catch (UnsupportedEncodingException e) {
            String msg = "The encoding is unsupported: encoding=" + encoding;
            throw new IllegalStateException(msg, e);
        } catch (FileNotFoundException e) {
            String msg = "The file of base behavior was not found: bsbhvFile=" + exfile;
            throw new IllegalStateException(msg, e);
        } catch (IOException e) {
            String msg = "bufferedWriter.write() threw the exception: bsbhvFile=" + exfile;
            throw new IllegalStateException(msg, e);
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException ignored) {
                }
            }
        }
    }
}
