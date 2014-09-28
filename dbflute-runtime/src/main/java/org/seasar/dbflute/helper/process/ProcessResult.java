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
package org.seasar.dbflute.helper.process;

/**
 * @author jflute
 * @since 0.9.8.3 (2011/05/03 Tuesday)
 */
public class ProcessResult {

    protected final String _processName;
    protected String _console;
    protected int _exitCode;

    public ProcessResult(String processName) {
        _processName = processName;
    }

    public String getProcessName() {
        return _processName;
    }

    public String getConsole() {
        return _console;
    }

    public void setConsole(String console) {
        this._console = console;
    }

    public int getExitCode() {
        return _exitCode;
    }

    public void setExitCode(int exitCode) {
        this._exitCode = exitCode;
    }
}
