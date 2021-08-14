/*
 * Copyright 2014-2021 the original author or authors.
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

import java.util.List;

/**
 * @author jflute
 * @since 1.2.5 extracted from DfDelimiterDataWriterImpl (2021/01/20 Wednesday at roppongi japanese)
 */
public class DfDelimiterDataValueLineInfo {

    // set later so cannot be final
    protected List<String> _valueList;
    protected boolean _continueNextLine;

    public List<String> getValueList() {
        return _valueList;
    }

    public void setValueList(List<String> valueList) {
        _valueList = valueList;
    }

    public boolean isContinueNextLine() {
        return _continueNextLine;
    }

    public void setContinueNextLine(boolean continueNextLine) {
        _continueNextLine = continueNextLine;
    }
}
