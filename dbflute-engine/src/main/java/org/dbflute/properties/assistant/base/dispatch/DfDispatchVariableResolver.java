/*
 * Copyright 2014-2019 the original author or authors.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import org.dbflute.exception.DfIllegalPropertySettingException;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.util.Srl;
import org.dbflute.util.Srl.ScopeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 1.1.0 (2015/01/16 Friday)
 */
public class DfDispatchVariableResolver {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger _log = LoggerFactory.getLogger(DfDispatchVariableResolver.class);

    // ===================================================================================
    //                                                                   Dispatch Variable
    //                                                                   =================
    public String resolveDispatchVariable(String propTitle, String plainValue) {
        return doResolveDispatchVariable(propTitle, plainValue, new DfDispatchVariableCallback() {
            public void throwNotFoundException(String propTitle, String plainValue, File dispatchFile) {
                throwDispatchFileNotFoundException(propTitle, plainValue, dispatchFile);
            }
        });
    }

    protected void throwDispatchFileNotFoundException(String propTitle, String plainValue, File dispatchFile) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The dispatch file was not found.");
        br.addItem("Advice");
        br.addElement("Check your dispatch file existing.");
        br.addElement("And check the setting in DBFlute property.");
        br.addItem("Property");
        br.addElement(propTitle);
        br.addItem("Dispatch Setting");
        br.addElement(plainValue);
        br.addItem("Dispatch File");
        br.addElement(dispatchFile);
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg);
    }

    // ===================================================================================
    //                                                                   Password Variable
    //                                                                   =================
    public String resolvePasswordVariable(String propTitle, String user, String password) {
        final String resolved = doResolveDispatchVariable(propTitle, password, new DfDispatchVariableCallback() {
            public void throwNotFoundException(String propTitle, String plainValue, File dispatchFile) {
                throwDatabaseUserPasswordFileNotFoundException(propTitle, user, plainValue, dispatchFile);
            }
        });
        return resolved != null ? resolved : ""; // password not allowed to be null
    }

    protected void throwDatabaseUserPasswordFileNotFoundException(String propTitle, String user, String password, File pwdFile) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The password file for the user was not found.");
        br.addItem("Advice");
        br.addElement("Check your password file existing.");
        br.addElement("And check the setting in DBFlute property.");
        br.addItem("Property");
        br.addElement(propTitle);
        br.addItem("Database User");
        br.addElement(user);
        br.addItem("Password Setting");
        br.addElement(password);
        br.addItem("Password File");
        br.addElement(pwdFile);
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg);
    }

    // ===================================================================================
    //                                                                     Actual Resolver
    //                                                                     ===============
    protected static interface DfDispatchVariableCallback {
        void throwNotFoundException(String propTitle, String plainValue, File dispatchFile);
    }

    protected String doResolveDispatchVariable(String propTitle, String plainValue, DfDispatchVariableCallback callback) {
        if (Srl.is_Null_or_TrimmedEmpty(plainValue)) {
            return plainValue;
        }
        // first: resolve $$env:
        // second: resolve outside-file (contains env-resolved)
        final DfEnvironmentVariableInfo envInfo = handleEnvironmentVariable(propTitle, plainValue);
        final String envResolvedValue = envInfo != null ? envInfo.getEnvValue() : plainValue;
        return handleOutsideFileVariable(propTitle, envResolvedValue, callback);
    }

    // -----------------------------------------------------
    //                                  Environment Variable
    //                                  --------------------
    protected DfEnvironmentVariableInfo handleEnvironmentVariable(String propTitle, String plainValue) {
        final String prefix = "$$env:";
        final String suffix = "$$";
        if (!existsEnvironmentVariable(propTitle, plainValue, prefix, suffix)) {
            return null;
        }
        // e.g. $$env:DBFLUTE_MAIHAMADB_JDBC_URL$$
        final Map<String, String> envMap = extractEnvironmentMap();
        final ScopeInfo scopeInfo = Srl.extractScopeFirst(plainValue, prefix, suffix);
        final String envKey = scopeInfo.getContent().trim(); // e.g. DBFLUTE_MAIHAMADB_JDBC_URL
        final String envValue = envMap.get(envKey);
        final String realValue;
        if (envValue != null) { // switch variable to value
            final String front = Srl.ltrim(scopeInfo.substringInterspaceToPrevious());
            final String rear = Srl.rtrim(Srl.substringFirstFront(scopeInfo.substringInterspaceToNext(), "|"));
            realValue = front + envValue + rear;
        } else { // no environment
            final String interspaceToNext = scopeInfo.substringInterspaceToNext().trim();
            if (interspaceToNext.contains("|")) {
                // e.g. $$env:DBFLUTE_MAIHAMADB_JDBC_URL$$ | jdbc:mysql://localhost:3306/maihamadb
                realValue = Srl.substringFirstRear(interspaceToNext, "|").trim();
            } else {
                throwNotFoundEnvironmentVariableException(propTitle, plainValue, envKey, envMap);
                return null; // unreachable
            }
        }
        final DfEnvironmentVariableInfo info = new DfEnvironmentVariableInfo();
        info.setEnvName(envKey);
        info.setEnvValue(realValue);
        return info;
    }

    protected boolean existsEnvironmentVariable(String propTitle, String plainValue, String prefix, String suffix) {
        // e.g. $$env:DBFLUTE_MAIHAMADB_JDBC_URL$$
        //   or $$env:DBFLUTE_MAIHAMADB_JDBC_URL$$ | jdbc:mysql://localhost:3306/maihamadb
        return plainValue != null && plainValue.contains(prefix) && Srl.substringFirstRear(plainValue, prefix).contains(suffix);
    }

    protected Map<String, String> extractEnvironmentMap() {
        final ProcessBuilder pb = new ProcessBuilder();
        return pb.environment(); // not null (see source code)
    }

    protected void throwNotFoundEnvironmentVariableException(String propTitle, String definedValue, String envKey,
            Map<String, String> envMap) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the environment variable for the key");
        br.addItem("Property Title");
        br.addElement(propTitle);
        br.addItem("Defined in dfprop");
        br.addElement(definedValue);
        br.addItem("NotFound Key");
        br.addElement(envKey);
        br.addItem("Existing Variable");
        br.addElement(envMap.keySet());
        final String msg = br.buildExceptionMessage();
        throw new DfIllegalPropertySettingException(msg);
    }

    // -----------------------------------------------------
    //                                  OutsideFile Variable
    //                                  --------------------
    protected String handleOutsideFileVariable(String propTitle, String plainValue, DfDispatchVariableCallback callback) {
        final DfOutsideFileVariableInfo outsideFileInfo = analyzeOutsideFileVariable(plainValue);
        if (outsideFileInfo == null) {
            return plainValue;
        }
        final File dispatchFile = outsideFileInfo.getDispatchFile();
        final String defaultValue = outsideFileInfo.getNofileDefaultValue(); // means default value
        if (existsOutsideFile(dispatchFile)) {
            return readOutsideFileFirstLine(dispatchFile, defaultValue);
        } else {
            if (defaultValue == null) {
                callback.throwNotFoundException(propTitle, plainValue, dispatchFile);
            }
            return defaultValue; // no dispatch file
        }
    }

    public DfOutsideFileVariableInfo analyzeOutsideFileVariable(String plainValue) {
        final String prefix = "df:dfprop/";
        if (!plainValue.startsWith(prefix)) {
            return null;
        }
        final String fileName;
        final String defaultValue;
        {
            final String content = Srl.substringFirstRear(plainValue, prefix);
            if (content.contains("|")) {
                fileName = Srl.substringFirstFront(content, "|").trim();
                defaultValue = Srl.substringFirstRear(content, "|").trim();
            } else {
                fileName = content;
                defaultValue = null;
            }
        }
        final File dispatchFile = new File("./dfprop/" + fileName);
        final DfOutsideFileVariableInfo variableInfo = new DfOutsideFileVariableInfo();
        variableInfo.setDispatchFile(dispatchFile);
        variableInfo.setNofileDefaultValue(defaultValue);
        return variableInfo;
    }

    protected boolean existsOutsideFile(File dispatchFile) {
        return dispatchFile.exists();
    }

    protected String readOutsideFileFirstLine(File dispatchFile, String defaultValue) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(dispatchFile), "UTF-8"));
            final String line = br.readLine();
            return line; // first line in the dispatch file is value
        } catch (Exception continued) {
            _log.info("Failed to read the dispatch file: " + dispatchFile);
            return defaultValue; // e.g. no password
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ignored) {}
            }
        }
    }
}
