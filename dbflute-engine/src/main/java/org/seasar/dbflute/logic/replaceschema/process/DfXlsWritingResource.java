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
package org.seasar.dbflute.logic.replaceschema.process;

/**
 * @author jflute
 */
public class DfXlsWritingResource {

    protected boolean _application;
    protected boolean _commonType;
    protected boolean _firstXls;
    protected boolean _reverseXls;

    public boolean isApplication() {
        return _application;
    }

    public DfXlsWritingResource application() {
        _application = true;
        return this;
    }

    public boolean isCommonType() {
        return _commonType;
    }

    public DfXlsWritingResource commonType() {
        _commonType = true;
        return this;
    }

    public boolean isFirstXls() {
        return _firstXls;
    }

    public DfXlsWritingResource firstXls() {
        _firstXls = true;
        return this;
    }

    public boolean isReverseXls() {
        return _reverseXls;
    }

    public DfXlsWritingResource reverseXls() {
        _reverseXls = true;
        return this;
    }
}
