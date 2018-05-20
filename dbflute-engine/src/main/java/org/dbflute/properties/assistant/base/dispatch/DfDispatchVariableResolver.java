/*
 * Copyright 2014-2018 the original author or authors.
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
    public String resolvePasswordVariable(final String propTitle, final String user, String password) {
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
        final DfEnvironmentVariableInfo envInfo = handleEnvironmentVariable(propTitle, plainValue);
        if (envInfo != null) {
            return envInfo.getEnvValue();
        }
        return handleOutsideFileVariable(propTitle, plainValue, callback);
    }

    // -----------------------------------------------------
    //                                  Environment Variable
    //                                  --------------------
    protected DfEnvironmentVariableInfo handleEnvironmentVariable(String propTitle, String plainValue) {
        final String prefix = "$$env:";
        final String suffix = "$$";
        if (plainValue != null && plainValue.startsWith(prefix) && plainValue.endsWith(suffix)) {
            final ScopeInfo scopeInfo = Srl.extractScopeWide(plainValue, prefix, suffix);
            final ProcessBuilder pb = new ProcessBuilder();
            final Map<String, String> map = pb.environment();
            if (map != null) { // might be no way, just in case
                final String key = scopeInfo.getContent().trim();
                final String realValue = map.get(key);
                if (realValue != null) {
                    final DfEnvironmentVariableInfo info = new DfEnvironmentVariableInfo();
                    info.setEnvName(key);
                    info.setEnvValue(realValue);
                    return info;
                } else {
                    throwNotFoundEnvironmentVariableException(propTitle, plainValue, key, map);
                }
            }
        }
        return null;
    }

    protected void throwNotFoundEnvironmentVariableException(String propTitle, String definedValue, String key, Map<String, String> map) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the environment variable for the key");
        br.addItem("Property Title");
        br.addElement(propTitle);
        br.addItem("Defined in dfprop");
        br.addElement(definedValue);
        br.addItem("NotFound Key");
        br.addElement(key);
        br.addItem("Existing Variable");
        br.addElement(map.keySet());
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
        final String resolved = outsideFileInfo.getOutsideValue();
        if (!dispatchFile.exists()) {
            if (resolved == null) {
                callback.throwNotFoundException(propTitle, plainValue, dispatchFile);
            }
            return resolved; // no dispatch file
        }
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(dispatchFile), "UTF-8"));
            final String line = br.readLine();
            return line; // first line in the dispatch file is value
        } catch (Exception continued) {
            _log.info("Failed to read the dispatch file: " + dispatchFile);
            return resolved; // e.g. no password
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ignored) {}
            }
        }
    }

    public DfOutsideFileVariableInfo analyzeOutsideFileVariable(String plainValue) {
        final String prefix = "df:dfprop/";
        if (!plainValue.startsWith(prefix)) {
            return null;
        }
        final String fileName;
        final String outsideValue;
        {
            final String content = Srl.substringFirstRear(plainValue, prefix);
            if (content.contains("|")) {
                fileName = Srl.substringFirstFront(content, "|");
                outsideValue = Srl.substringFirstRear(content, "|");
            } else {
                fileName = content;
                outsideValue = null;
            }
        }
        final File dispatchFile = new File("./dfprop/" + fileName);
        final DfOutsideFileVariableInfo variableInfo = new DfOutsideFileVariableInfo();
        variableInfo.setDispatchFile(dispatchFile);
        variableInfo.setOutsideValue(outsideValue);
        return variableInfo;
    }
}
