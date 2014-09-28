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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.seasar.dbflute.helper.process.exception.SystemScriptFailureException;
import org.seasar.dbflute.helper.process.exception.SystemScriptUnsupportedScriptException;
import org.seasar.dbflute.util.DfCollectionUtil;

/**
 * @author jflute
 * @since 0.9.8.3 (2011/05/03 Tuesday)
 */
public class SystemScript {

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
    protected Map<String, String> _environmentMap;

    // ===================================================================================
    //                                                                      Execute Script
    //                                                                      ==============
    public ProcessResult execute(File baseDir, String scriptName, String... args) {
        final ProcessResult result = new ProcessResult(scriptName);
        final List<String> cmdList = prepareCommandList(scriptName, args);
        if (cmdList.isEmpty()) {
            String msg = "Unsupported script: " + scriptName;
            throw new SystemScriptUnsupportedScriptException(msg);
        }
        final ProcessBuilder builder = prepareProcessBuilder(baseDir, cmdList);
        final Process process = startProcess(scriptName, builder);
        return handleProcessResult(scriptName, result, process);
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

    protected ProcessConsoleReader createProcessConsoleReader(InputStream stdin, final String encoding) {
        return new ProcessConsoleReader(stdin, encoding);
    }

    protected ProcessResult handleProcessResult(String scriptName, final ProcessResult result, final Process process) {
        InputStream stdin = null;
        try {
            stdin = process.getInputStream();
            final String encoding = _consoleEncoding != null ? _consoleEncoding : "UTF-8";
            final ProcessConsoleReader reader = createProcessConsoleReader(stdin, encoding);
            reader.start();
            final int exitCode = process.waitFor();
            reader.join();
            final String console = reader.read();
            result.setConsole(console);
            result.setExitCode(exitCode);
            return result;
        } catch (InterruptedException e) {
            String msg = "The execution was interrupted: " + scriptName;
            throw new SystemScriptFailureException(msg, e);
        }
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getConsoleEncoding() {
        return _consoleEncoding;
    }

    public void setConsoleEncoding(String consoleEncoding) {
        this._consoleEncoding = consoleEncoding;
    }

    public Map<String, String> getEnvironmentMap() {
        return _environmentMap;
    }

    public void setEnvironmentMap(Map<String, String> environmentMap) {
        this._environmentMap = environmentMap;
    }
}
