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
package org.seasar.dbflute.helper.token.line;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 */
public class LineToken {

    // ===================================================================================
    //                                                                       Tokenize Line
    //                                                                       =============
    public List<String> tokenize(String lineString, LineTokenizingOption lineTokenizingOption) {
        final String delimiter = lineTokenizingOption.getDelimiter();
        final List<String> list = new ArrayList<String>();
        int elementIndex = 0;
        int delimiterIndex = lineString.indexOf(delimiter);
        while (delimiterIndex >= 0) {
            final String value = lineString.substring(elementIndex, delimiterIndex);
            list.add(filterHandlingEmptyAsNull(value, lineTokenizingOption));
            elementIndex = delimiterIndex + delimiter.length();
            delimiterIndex = lineString.indexOf(delimiter, elementIndex);
        }
        final String lastElement = lineString.substring(elementIndex);
        list.add(filterHandlingEmptyAsNull(lastElement, lineTokenizingOption));
        return list;
    }

    protected String filterHandlingEmptyAsNull(String target, LineTokenizingOption lineTokenizingOption) {
        if (target == null) {
            return null;
        }
        if (lineTokenizingOption.isHandleEmtpyAsNull() && "".equals(target)) {
            return null;
        }
        return target;
    }

    // ===================================================================================
    //                                                                           Make Line
    //                                                                           =========
    public String make(Collection<String> valueList, LineMakingOption lineMakingOption) {
        assertObjectNotNull("valueList", valueList);
        assertObjectNotNull("lineMakingOption", lineMakingOption);
        final String delimiter = lineMakingOption.getDelimiter();
        assertObjectNotNull("lineMakingOption.getDelimiter()", delimiter);
        return createLineString(valueList, delimiter, lineMakingOption.isQuoteAll(),
                lineMakingOption.isQuoteMinimally(), lineMakingOption.isTrimSpace());
    }

    protected String createLineString(Collection<String> valueList, String delimiter, boolean quoteAll,
            boolean quoteMinimamlly, boolean trimSpace) {
        final StringBuilder sb = new StringBuilder();
        for (String value : valueList) {
            value = (value != null ? value : "");
            if (trimSpace) {
                value = value.trim();
            }
            if (quoteAll) {
                value = Srl.replace(value, "\"", "\"\"");
                sb.append(delimiter).append("\"").append(value).append("\"");
            } else if (quoteMinimamlly && needsQuote(value, delimiter)) {
                value = Srl.replace(value, "\"", "\"\"");
                sb.append(delimiter).append("\"").append(value).append("\"");
            } else {
                sb.append(delimiter).append(value);
            }
        }
        sb.delete(0, delimiter.length());
        return sb.toString();
    }

    protected boolean needsQuote(String value, String delimiter) {
        return value.contains("\"") || value.contains("\r") || value.contains("\n") || value.contains(delimiter);
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    /**
     * Assert that the object is not null.
     * @param variableName Variable name. (NotNull)
     * @param value Value. (NotNull)
     * @exception IllegalArgumentException
     */
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

    /**
     * Assert that the entity is not null and not trimmed empty.
     * @param variableName Variable name. (NotNull)
     * @param value Value. (NotNull)
     */
    protected void assertStringNotNullAndNotTrimmedEmpty(String variableName, String value) {
        assertObjectNotNull("variableName", variableName);
        assertObjectNotNull(variableName, value);
        if (value.trim().length() == 0) {
            String msg = "The value should not be empty: variableName=" + variableName + " value=" + value;
            throw new IllegalArgumentException(msg);
        }
    }
}