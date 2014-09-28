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
package org.seasar.dbflute.helper.filesystem;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author jflute
 * @since 1.0.5K (2014/08/16 Saturday)
 */
public class FileURL {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The address of the URL. (NotNull) */
    protected final String _address;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public FileURL(String address) {
        if (address == null) {
            String msg = "The argument 'address' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        _address = address;
    }

    // ===================================================================================
    //                                                                            Download
    //                                                                            ========
    public void download(String toFilePath) {
        final URL url = createURL();
        doDownloadFileAndClose(url, toFilePath);
    }

    protected void doDownloadFileAndClose(URL url, String toFilePath) {
        InputStream in;
        try {
            in = url.openStream();
        } catch (IOException e) {
            String msg = "Failed to open stream: url=" + url;
            throw new IllegalStateException(msg, e);
        }
        doDownloadFileAndClose(in, toFilePath);
    }

    protected void doDownloadFileAndClose(InputStream ins, String toFilePath) {
        final byte[] bytes;
        try {
            bytes = toBytes(ins);
        } finally {
            try {
                ins.close();
            } catch (IOException e) {
                String msg = "Failed to close the input stream: ins=" + ins;
                throw new IllegalStateException(msg, e);
            }
        }
        final File outputFile = new File(toFilePath);
        final FileOutputStream ous;
        try {
            ous = new FileOutputStream(outputFile, false);
        } catch (FileNotFoundException e) {
            String msg = "Not found the file: toFilePath=" + toFilePath;
            throw new IllegalStateException(msg, e);
        }
        try {
            ous.write(bytes);
        } catch (IOException e) {
            String msg = "Failed to write the byte data: toFilePath=" + toFilePath;
            throw new IllegalStateException(msg, e);
        }
    }

    // ===================================================================================
    //                                                                          URL Helper
    //                                                                          ==========
    protected URL createURL() {
        try {
            return new URL(_address);
        } catch (MalformedURLException e) {
            String msg = "Failed to create the URL instance: address=" + _address;
            throw new IllegalStateException(msg, e);
        }
    }

    // ===================================================================================
    //                                                                       Stream Helper
    //                                                                       =============
    protected byte[] toBytes(InputStream ins) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        copy(ins, out);
        return out.toByteArray();
    }

    protected void copy(InputStream ins, OutputStream ous) {
        try {
            final byte[] bytes = new byte[256];
            int length = ins.read(bytes);
            while (length != -1 && length != 0) {
                ous.write(bytes, 0, length);
                length = ins.read(bytes);
            }
        } catch (IOException e) {
            String msg = "Failed to copy : ins=" + ins + ", ous=" + ous;
            throw new IllegalStateException(msg, e);
        }
    }
}
