/*
 * Copyright 2014-2021 the original author or authors.
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
package org.dbflute.properties.assistant.document.stylesheet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.dbflute.DfBuildProperties;
import org.dbflute.properties.DfBasicProperties;
import org.dbflute.properties.DfDocumentProperties;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 1.2.5 split from DfDocumentProperties (2021/07/04 Sunday at roppongi japanese)
 */
public class DfDocStyleSheetReader {

    public String readEmbeddedStyleSheet(String styleSheet) {
        final String purePath = Srl.substringFirstRear(styleSheet, DfDocumentProperties.STYLE_SHEET_EMBEDDED_MARK);
        final File cssFile = new File(purePath);
        BufferedReader br = null;
        try {
            final String encoding = getBasicProperties().getTemplateFileEncoding();
            final String separator = getBasicProperties().getSourceCodeLineSeparator();
            br = new BufferedReader(new InputStreamReader(new FileInputStream(cssFile), encoding));
            final StringBuilder sb = new StringBuilder();
            while (true) {
                final String line = br.readLine();
                if (line == null) {
                    break;
                }
                sb.append(line).append(separator);
            }
            return sb.toString();
        } catch (IOException e) {
            String currentPath = null;
            try {
                currentPath = new File(".").getCanonicalPath();
            } catch (IOException ignored) {}
            final String currentExp = currentPath != null ? " (current: " + currentPath + ")" : "";
            String msg = "Failed to read the CSS file: " + cssFile + currentExp;
            throw new IllegalStateException(msg, e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ignored) {}
            }
        }
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfBasicProperties getBasicProperties() {
        return DfBuildProperties.getInstance().getBasicProperties();
    }
}
