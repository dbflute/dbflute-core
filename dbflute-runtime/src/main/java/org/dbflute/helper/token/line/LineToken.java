/*
 * Copyright 2014-2023 the original author or authors.
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
package org.dbflute.helper.token.line;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dbflute.util.Srl;

/**
 * @author jflute
 */
public class LineToken {

    // ===================================================================================
    //                                                                       Tokenize Line
    //                                                                       =============
    /**
     * Tokenize the line.
     * @param lineString The line string to tokenize. (NotNull)
     * @param opLambda The callback for the option of line-tokenizing. (NotNull)
     * @return The list of token. (NotNull)
     */
    public List<String> tokenize(String lineString, LineTokenOptionCall<LineTokenizingOption> opLambda) {
        final LineTokenizingOption option = createLineTokenizingOption(opLambda);
        final String delimiter = option.getDelimiter();
        final List<String> list = new ArrayList<String>();
        int elementIndex = 0;
        int delimiterIndex = lineString.indexOf(delimiter);
        while (delimiterIndex >= 0) {
            final String value = lineString.substring(elementIndex, delimiterIndex);
            list.add(filterHandlingEmptyAsNull(value, option));
            elementIndex = delimiterIndex + delimiter.length();
            delimiterIndex = lineString.indexOf(delimiter, elementIndex);
        }
        final String lastElement = lineString.substring(elementIndex);
        list.add(filterHandlingEmptyAsNull(lastElement, option));
        return list;
    }

    protected LineTokenizingOption createLineTokenizingOption(LineTokenOptionCall<LineTokenizingOption> opLambda) {
        assertObjectNotNull("opLambda", opLambda);
        final LineTokenizingOption op = newLineTokenizingOption();
        opLambda.callback(op);
        return op;
    }

    protected LineTokenizingOption newLineTokenizingOption() {
        return new LineTokenizingOption();
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
    /**
     * Make the line by the value list.
     * @param valueList The list of value to be line. (NotNull)
     * @param opLambda The callback for the option of line-making. (NotNull)
     * @return The line string from the value list. (NotNull)
     */
    public String make(Collection<String> valueList, LineTokenOptionCall<LineMakingOption> opLambda) {
        final LineMakingOption option = createLineMakingOption(opLambda);
        assertObjectNotNull("valueList", valueList);
        assertObjectNotNull("lineMakingOption", option);
        final String delimiter = option.getDelimiter();
        assertObjectNotNull("lineMakingOption.getDelimiter()", delimiter);
        return createLineString(valueList, delimiter, option.isQuoteAll(), option.isQuoteMinimally(), option.isTrimSpace());
    }

    protected LineMakingOption createLineMakingOption(LineTokenOptionCall<LineMakingOption> opLambda) {
        assertObjectNotNull("opLambda", opLambda);
        final LineMakingOption op = newLineMakingOption();
        opLambda.callback(op);
        return op;
    }

    protected LineMakingOption newLineMakingOption() {
        return new LineMakingOption();
    }

    protected String createLineString(Collection<String> valueList, String delimiter, boolean quoteAll, boolean quoteMinimamlly,
            boolean trimSpace) {
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
     * @param variableName The check name of variable for message. (NotNull)
     * @param value The checked value. (NotNull)
     * @throws IllegalArgumentException When the argument is null.
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
     * @param variableName The check name of variable for message. (NotNull)
     * @param value The checked value. (NotNull)
     * @throws IllegalArgumentException When the argument is null or empty.
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