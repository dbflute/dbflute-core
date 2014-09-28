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
package org.seasar.dbflute.logic.doc.lreverse;

import java.util.List;
import java.util.Map;

import org.seasar.dbflute.helper.jdbc.facade.DfJFadCursorCallback;

/**
 * @author jflute
 * @since 0.9.8.3 (2011/04/25 Monday)
 */
public class DfLReverseDataResult {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final List<Map<String, String>> _resultList;
    protected final DfJFadCursorCallback _cursorCallback;

    // ===================================================================================
    //                                                                         Easy-to-Use
    //                                                                         ===========
    public boolean isLargeData() {
        return _resultList == null && _cursorCallback != null;
    }

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfLReverseDataResult(List<Map<String, String>> resultList) {
        _resultList = resultList;
        _cursorCallback = null;
    }

    public DfLReverseDataResult(DfJFadCursorCallback cursorCallback) {
        _resultList = null;
        _cursorCallback = cursorCallback;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public List<Map<String, String>> getResultList() {
        return _resultList;
    }

    public DfJFadCursorCallback getCursorCallback() {
        return _cursorCallback;
    }
}
