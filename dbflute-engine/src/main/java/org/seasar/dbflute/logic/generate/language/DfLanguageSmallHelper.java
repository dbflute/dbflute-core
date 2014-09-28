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
package org.seasar.dbflute.logic.generate.language;

import org.seasar.dbflute.util.Srl;
import org.seasar.dbflute.util.Srl.ScopeInfo;

/**
 * @author jflute
 */
public class DfLanguageSmallHelper {

    // ===================================================================================
    //                                                                             Grammar
    //                                                                             =======
    public boolean hasGenericClassElement(String className, String genericExp, String begin, String end) {
        return Srl.extractScopeWide(genericExp, className + begin, end) != null;
    }

    public String extractGenericClassElement(String className, String genericExp, String begin, String end) {
        final ScopeInfo scopeInfo = Srl.extractScopeWide(genericExp, className + begin, end);
        if (scopeInfo == null) {
            String msg = "Not found the generic element: " + className + ", " + genericExp;
            throw new IllegalStateException(msg);
        }
        return scopeInfo.getContent();
    }
}
