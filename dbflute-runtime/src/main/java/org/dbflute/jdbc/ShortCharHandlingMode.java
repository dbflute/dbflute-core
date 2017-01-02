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
package org.dbflute.jdbc;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jflute
 * @since 1.1.0 (2014/10/18 Thursday)
 */
public enum ShortCharHandlingMode {

    RFILL("R"), LFILL("L"), EXCEPTION("E"), NONE("N");
    private static final Map<String, ShortCharHandlingMode> _codeValueMap = new HashMap<String, ShortCharHandlingMode>();
    static {
        for (ShortCharHandlingMode value : values()) {
            _codeValueMap.put(value.code().toLowerCase(), value);
        }
    }
    protected final String _code;

    private ShortCharHandlingMode(String code) {
        _code = code;
    }

    public static ShortCharHandlingMode codeOf(Object code) {
        if (code == null) {
            return null;
        }
        if (code instanceof ShortCharHandlingMode) {
            return (ShortCharHandlingMode) code;
        }
        return _codeValueMap.get(code.toString().toLowerCase());
    }

    public String code() {
        return _code;
    }
}
