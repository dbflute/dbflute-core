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
package org.dbflute.properties.assistant.base.dispatch;

import java.io.File;

/**
 * @author jflute
 * @since 1.1.0 (2015/01/16 Friday)
 */
public class DfOutsideFileVariableInfo {

    protected File _dispatchFile;
    protected String _nofileDefaultValue;

    public File getDispatchFile() {
        return _dispatchFile;
    }

    public void setDispatchFile(File dispatchFile) {
        _dispatchFile = dispatchFile;
    }

    public String getNofileDefaultValue() {
        return _nofileDefaultValue;
    }

    public void setNofileDefaultValue(String nofileDefaultValue) {
        _nofileDefaultValue = nofileDefaultValue;
    }
}
