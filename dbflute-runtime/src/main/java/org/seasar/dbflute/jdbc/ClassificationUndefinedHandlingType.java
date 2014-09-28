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
package org.seasar.dbflute.jdbc;

import java.util.HashMap;
import java.util.Map;

/**
 * The handling type of undefined classification code. <br />
 * It's an internal type for DBFlute runtime.
 * @author jflute
 * @since 1.0.5K (2014/08/20 Wednesday)
 */
public enum ClassificationUndefinedHandlingType {

    /** throw exception */
    EXCEPTION("EXCEPTION", true, false),

    /** info logging (DBFlute default) */
    LOGGING("LOGGING", true, true),

    /** do nothing */
    ALLOWED("ALLOWED", false, true);

    private static final Map<String, ClassificationUndefinedHandlingType> _codeValueMap = new HashMap<String, ClassificationUndefinedHandlingType>();
    static {
        for (ClassificationUndefinedHandlingType value : values()) {
            _codeValueMap.put(value.code().toLowerCase(), value);
        }
    }

    private final String _code;
    private final boolean _checked;
    private final boolean _continued;

    private ClassificationUndefinedHandlingType(String code, boolean checked, boolean continued) {
        _code = code;
        _checked = checked;
        _continued = continued;
    }

    public static ClassificationUndefinedHandlingType codeOf(Object code) {
        if (code == null) {
            return null;
        }
        if (code instanceof ClassificationUndefinedHandlingType) {
            return (ClassificationUndefinedHandlingType) code;
        }
        return _codeValueMap.get(code.toString().toLowerCase());
    }

    public String code() {
        return _code;
    }

    public boolean isChecked() {
        return _checked;
    }

    public boolean isCheckedAbort() {
        return _checked && !_continued;
    }

    public boolean isCheckedContinue() {
        return _checked && _continued;
    }

    public boolean isContinued() {
        return _continued;
    }
}
