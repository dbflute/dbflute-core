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
package org.seasar.dbflute.helper.stacktrace;

import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 */
public class InvokeNameResult {

    // ==========================================================================================
    //                                                                                  Attribute
    //                                                                                  =========
    protected String _simpleClassName;
    protected String _methodName;
    protected int _lineNumber;
    protected int _foundIndex;
    protected int _foundFirstIndex;

    // ==========================================================================================
    //                                                                                Invoke Name
    //                                                                                ===========
    public String buildInvokeName(boolean useTestShortName) {
        final String methodName;
        if (useTestShortName && hasTestSuffix()) {
            methodName = buildFilterMethodName();
        } else {
            methodName = _methodName;
        }
        final String baseName = _simpleClassName + "." + methodName + "()";
        final String invokeName;
        if (_lineNumber > 0) {
            invokeName = baseName + ":" + _lineNumber + " -> ";
        } else {
            invokeName = baseName + " -> ";
        }
        return invokeName;
    }

    public boolean hasTestSuffix() {
        return _simpleClassName != null && _simpleClassName.endsWith("Test");
    }

    protected String buildFilterMethodName() {
        final int limitSize = 10;
        final int reversePointSize = 20;
        final int reverseIndex = 5;
        String methodName = _methodName;
        if (_methodName != null && _methodName.length() > limitSize) {
            String suffix = "";
            if (_methodName.length() > reversePointSize) {
                suffix = Srl.rearstring(_methodName, reverseIndex);
            }
            methodName = _methodName.substring(0, limitSize) + "..." + suffix;
        }
        return methodName;
    }

    // ==========================================================================================
    //                                                                               Manipulation
    //                                                                               ============
    public int getNextStartIndex() {
        return _foundIndex + 1;
    }

    // ==========================================================================================
    //                                                                              Determination
    //                                                                              =============
    public boolean isEmptyResult() {
        return _simpleClassName == null;
    }

    // ==========================================================================================
    //                                                                                   Accessor
    //                                                                                   ========
    public String getSimpleClassName() {
        return _simpleClassName;
    }

    public void setSimpleClassName(String simpleClassName) {
        _simpleClassName = simpleClassName;
    }

    public String getMethodName() {
        return _methodName;
    }

    public void setMethodName(String methodName) {
        _methodName = methodName;
    }

    public int getLineNumber() {
        return _lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this._lineNumber = lineNumber;
    }

    public int getFoundIndex() {
        return _foundIndex;
    }

    public void setFoundIndex(int foundIndex) {
        _foundIndex = foundIndex;
    }

    public int getFoundFirstIndex() {
        return _foundFirstIndex;
    }

    public void setFoundFirstIndex(int foundFirstIndex) {
        _foundFirstIndex = foundFirstIndex;
    }
}