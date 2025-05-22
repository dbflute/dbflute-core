/*
 * Copyright 2014-2025 the original author or authors.
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
package org.dbflute.logic.replaceschema.loaddata.delimiter.line;

import java.util.ArrayList;
import java.util.List;

import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 1.2.5 extracted from DfDelimiterDataWriterImpl (2021/01/20 Wednesday at roppongi japanese)
 */
public class DfDelimiterDataValueLineAnalyzer {

    public DfDelimiterDataValueLineInfo analyzeValueLine(String lineString, String delimiter) {
        // Don't use String.split() because the method
        // does not match this (detail) specification!
        //final String[] values = lineString.split(delimiter);
        final List<String> valueList = Srl.splitList(lineString, delimiter);
        return arrangeValueList(valueList, delimiter);
    }

    protected DfDelimiterDataValueLineInfo arrangeValueList(List<String> valueList, String delimiter) {
        final DfDelimiterDataValueLineInfo valueLineInfo = new DfDelimiterDataValueLineInfo();
        final ArrayList<String> resultList = new ArrayList<String>();
        String preString = "";
        for (int i = 0; i < valueList.size(); i++) {
            final String value = valueList.get(i);
            if (value == null) { // basically no way (valueList does not contain null)
                continue;
            }
            if (i == valueList.size() - 1) { // last loop at the line
                if (preString.equals("")) {
                    if (isFrontQOnly(value)) {
                        valueLineInfo.setContinueNextLine(true);
                        resultList.add(value);
                    } else if (isRearQOnly(value)) {
                        resultList.add(value);
                    } else if (isNotBothQ(value)) {
                        resultList.add(value);
                    } else {
                        resultList.add(resolveDoubleQuotation(value));
                    }
                } else { // continued
                    if (endsQuote(value, false)) {
                        resultList.add(resolveDoubleQuotation(connectPreString(preString, delimiter, value)));
                    } else {
                        valueLineInfo.setContinueNextLine(true);
                        resultList.add(connectPreString(preString, delimiter, value));
                    }
                }
                break; // because it's the last loop
            }

            if (preString.equals("")) {
                if (isFrontQOnly(value)) {
                    preString = value;
                    continue;
                } else if (isRearQOnly(value)) {
                    preString = value;
                    continue;
                } else if (isNotBothQ(value)) {
                    resultList.add(value);
                } else {
                    resultList.add(resolveDoubleQuotation(value));
                }
            } else { // continued
                if (endsQuote(value, false)) {
                    resultList.add(resolveDoubleQuotation(connectPreString(preString, delimiter, value)));
                } else {
                    preString = connectPreString(preString, delimiter, value);
                    continue;
                }
            }
            preString = "";
        }
        valueLineInfo.setValueList(resultList);
        return valueLineInfo;
    }

    protected String connectPreString(String preString, String delimiter, String value) {
        if (preString.equals("")) {
            return value;
        } else {
            return preString + delimiter + value;
        }
    }

    protected boolean isNotBothQ(final String value) {
        return !isQQ(value) && !value.startsWith("\"") && !endsQuote(value, false);
    }

    protected boolean isRearQOnly(final String value) {
        return !isQQ(value) && !value.startsWith("\"") && (endsQuote(value, false));
    }

    protected boolean isFrontQOnly(final String value) {
        return !isQQ(value) && value.startsWith("\"") && !endsQuote(value, true);
    }

    protected boolean isQQ(final String value) {
        return value.equals("\"\"");
    }

    protected boolean endsQuote(String value, boolean startsQuote) {
        value = startsQuote ? value.substring(1) : value;
        final int length = value.length();
        int count = 0;
        for (int i = 0; i < length; i++) {
            char ch = value.charAt(length - (i + 1));
            if (ch == '\"') {
                ++count;
            } else {
                break;
            }
        }
        return count > 0 && isOddNumber(count);
    }

    protected boolean isOddNumber(int number) {
        return (number % 2) != 0;
    }

    protected String resolveDoubleQuotation(String value) {
        if (value.length() > 1 && value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1);
            value = value.substring(0, value.length() - 1);
            // resolve escaped quotation : "" -> "
            value = Srl.replace(value, "\"\"", "\"");
        }
        return value;
    }
}
