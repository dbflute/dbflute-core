/*
 * Copyright 2014-2017 the original author or authors.
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
package org.dbflute.properties.assistant.classification;

import java.util.Map;

/**
 * @author jflute
 */
public class DfClassificationJavaNameFilter {

    public void prepareNameFromTo(Map<String, String> _nameFromToMap) {
        // basic unexpected marks
        _nameFromToMap.put(" ", "_");
        _nameFromToMap.put("!", "_");
        _nameFromToMap.put("\"", "");
        _nameFromToMap.put("#", "_");
        _nameFromToMap.put("%", "_PERCENT_");
        _nameFromToMap.put("&", "_AND_");
        _nameFromToMap.put("'", "");
        _nameFromToMap.put("(", "_");
        _nameFromToMap.put(")", "_");
        _nameFromToMap.put("@", "_");
        _nameFromToMap.put("+", "_");
        _nameFromToMap.put("*", "_");
        _nameFromToMap.put(",", "_");
        _nameFromToMap.put(".", "_");
        _nameFromToMap.put("/", "_");
        _nameFromToMap.put("<", "_");
        _nameFromToMap.put(">", "_");
        _nameFromToMap.put("?", "_");
        _nameFromToMap.put("\n", "_");
        _nameFromToMap.put("\t", "_");

        // basic full-width marks
        _nameFromToMap.put("\uff05", "_PERCENT_");
        _nameFromToMap.put("\uff06", "_AND_");
        _nameFromToMap.put("\uff08", "_");
        _nameFromToMap.put("\uff09", "_");

        // pinpoint full-width
        _nameFromToMap.put("\u3000", "_"); // full-width space
        _nameFromToMap.put("\u3001", "_"); // Japanese touten
        _nameFromToMap.put("\u3002", "_"); // Japanese kuten

        // non-compilable hyphens
        _nameFromToMap.put("\u2010", "_");
        _nameFromToMap.put("\u2212", "_");
        _nameFromToMap.put("\uff0d", "_");
    }
}
