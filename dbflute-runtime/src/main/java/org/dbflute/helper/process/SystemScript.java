/*
 * Copyright 2014-2022 the original author or authors.
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
package org.dbflute.helper.process;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.dbflute.helper.process.exception.SystemScriptFailureException;
import org.dbflute.helper.process.exception.SystemScriptUnsupportedScriptException;
import org.dbflute.util.DfCollectionUtil;

/**
 * @author jflute
 * @since 0.9.8.3 (2011/05/03 Tuesday)
 */
public class SystemScript { // basically memorable code...

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String WINDOWS_BATCH_EXT = ".bat";
    public static final String SHELL_SCRIPT_EXT = ".sh";

    protected static final List<String> SUPPORTED_EXT_LIST;
    static {
        final List<String> tmpList = DfCollectionUtil.newArrayList(WINDOWS_BATCH_EXT, SHELL_SCRIPT_EXT);
        SUPPORTED_EXT_LIST = Collections.unmodifiableList(tmpList);
    }

    public static List<String> getSupportedExtList() {
        return SUPPORTED_EXT_LIST;
    }

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String _consoleEncoding;
    protected Consumer<String> _consoleLiner;
    protected Map<String, String> _environmentMap;

    // ===================================================================================
    //                                                                              Option
    //                                                                              ======
    public SystemScript consoleEncoding(String encoding) {
        assertStringNotNullAndNotTrimmedEmpty("encoding", encoding);
        _consoleEncoding = encoding;
        return this;
    }

    public SystemScript consoleLiner(Consumer<String> liner) {
        assertObjectNotNull("liner", liner);
        _consoleLiner = liner;
        return this;
    }

    public SystemScript env(String key, String value) {
        assertStringNotNullAndNotTrimmedEmpty("key", key);
        assertObjectNotNull("value", value); // empty allowed: possible? e.g. "SEA="
        if (_environmentMap == null) {
            _environmentMap = new LinkedHashMap<String, String>();
        }
        _environmentMap.put(key, value);
        return this;
    }

    // ===================================================================================
    //                                                                      Execute Script
    //                                                                      ==============
    public ProcessResult execute(File baseDir, String scriptName, String... args) {
        assertObjectNotNull("baseDir", baseDir);
        assertStringNotNullAndNotTrimmedEmpty("scriptName", scriptName);
        final List<String> cmdList = prepareCommandList(scriptName, args);
        if (cmdList.isEmpty()) {
            String msg = "Unsupported script: " + scriptName;
            throw new SystemScriptUnsupportedScriptException(msg);
        }
        final ProcessBuilder builder = prepareProcessBuilder(baseDir, cmdList);
        final Process process = startProcess(scriptName, builder);
        return handleProcessResult(scriptName, process);
    }

    protected List<String> prepareCommandList(String scriptName, String... args) {
        final List<String> cmdList = new ArrayList<String>();
        if (isSystemWindowsOS()) {
            if (scriptName.endsWith(WINDOWS_BATCH_EXT)) {
                cmdList.add("cmd.exe");
                cmdList.add("/c");
                cmdList.add(scriptName);
            }
        } else {
            if (scriptName.endsWith(SHELL_SCRIPT_EXT)) {
                cmdList.add("sh");
                cmdList.add(scriptName);
            }
        }
        if (cmdList.isEmpty()) {
            return DfCollectionUtil.emptyList();
        }
        if (args != null && args.length > 0) {
            for (String arg : args) {
                cmdList.add(arg);
            }
        }
        return cmdList;
    }

    protected boolean isSystemWindowsOS() {
        final String osName = System.getProperty("os.name");
        return osName != null && osName.toLowerCase().contains("windows");
    }

    protected ProcessBuilder prepareProcessBuilder(File baseDir, final List<String> cmdList) {
        final ProcessBuilder builder = createProcessBuilder(cmdList);
        if (_environmentMap != null && !_environmentMap.isEmpty()) {
            builder.environment().putAll(_environmentMap);
        }
        builder.directory(baseDir).redirectErrorStream(true);
        return builder;
    }

    protected ProcessBuilder createProcessBuilder(final List<String> cmdList) {
        return new ProcessBuilder(cmdList);
    }

    protected Process startProcess(String scriptName, final ProcessBuilder builder) {
        final Process process;
        try {
            process = builder.start();
        } catch (IOException e) {
            String msg = "Failed to execute the command: " + scriptName;
            throw new IllegalStateException(msg, e);
        }
        return process;
    }

    protected ProcessResult handleProcessResult(String scriptName, final Process process) {
        InputStream stdin = null; // closed in reader
        try {
            stdin = process.getInputStream();
            final ProcessConsoleReader reader = createProcessConsoleReader(stdin);
            reader.start();
            final int exitCode = process.waitFor();
            final String console = reader.read();
            return new ProcessResult(scriptName, console, exitCode);
        } catch (InterruptedException e) {
            String msg = "The execution was interrupted: " + scriptName;
            throw new SystemScriptFailureException(msg, e);
        }
    }

    protected ProcessConsoleReader createProcessConsoleReader(InputStream stdin) {
        return newProcessConsoleReader(stdin, _consoleEncoding != null ? _consoleEncoding : "UTF-8", _consoleLiner);
    }

    protected ProcessConsoleReader newProcessConsoleReader(InputStream stdin, String encoding, Consumer<String> liner) {
        return new ProcessConsoleReader(stdin, encoding, liner);
    }

    // ===================================================================================
    //                                                                       Assert Helper
    //                                                                       =============
    // -----------------------------------------------------
    //                                         Assert Object
    //                                         -------------
    protected void assertObjectNotNull(String variableName, Object value) {
        if (variableName == null) {
            String msg = "The value should not be null: variableName=null value=" + value;
            throw new IllegalArgumentException(msg);
        }
        if (value == null) {
            String msg = "The value should not be null: variableName=" + variableName;
            throw new IllegalArgumentException(msg);
        }
    }

    // -----------------------------------------------------
    //                                         Assert String
    //                                         -------------
    protected void assertStringNotNullAndNotTrimmedEmpty(String variableName, String value) {
        assertObjectNotNull("variableName", variableName);
        assertObjectNotNull(variableName, value);
        if (value.trim().length() == 0) {
            String msg = "The value should not be empty: variableName=" + variableName + " value=" + value;
            throw new IllegalArgumentException(msg);
        }
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getConsoleEncoding() {
        return _consoleEncoding;
    }

    @Deprecated
    public void setConsoleEncoding(String consoleEncoding) { // old style
        this._consoleEncoding = consoleEncoding;
    }

    public Map<String, String> getEnvironmentMap() {
        return _environmentMap;
    }

    @Deprecated
    public void setEnvironmentMap(Map<String, String> environmentMap) { // old style
        this._environmentMap = environmentMap;
    }
}
