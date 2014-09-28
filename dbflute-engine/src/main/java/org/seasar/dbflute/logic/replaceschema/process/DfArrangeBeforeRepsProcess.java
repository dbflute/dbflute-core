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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.util.FileUtils;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.helper.process.ProcessResult;
import org.seasar.dbflute.helper.process.SystemScript;
import org.seasar.dbflute.helper.process.exception.SystemScriptUnsupportedScriptException;
import org.seasar.dbflute.properties.DfReplaceSchemaProperties;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.DfStringUtil;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 * @since 0.9.8.5 (2011/06/17 Friday)
 */
public class DfArrangeBeforeRepsProcess extends DfAbstractReplaceSchemaProcess {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log instance. */
    private static final Log _log = LogFactory.getLog(DfArrangeBeforeRepsProcess.class);

    // ===================================================================================
    //                                                                             Process
    //                                                                             =======
    public void arrangeBeforeReps() {
        final DfReplaceSchemaProperties prop = getReplaceSchemaProperties();
        final Map<String, String> copyMap = prop.getArrangeBeforeRepsCopyMap();
        if (!copyMap.isEmpty()) {
            _log.info("...Arranging resource files for ReplaceSchema");
        }
        for (Entry<String, String> entry : copyMap.entrySet()) {
            final String src = entry.getKey();
            final String dest = entry.getValue();
            arrangeCopy(src, dest);
        }
        final Map<String, String> scriptMap = prop.getArrangeBeforeRepsScriptMap();
        if (!scriptMap.isEmpty()) {
            _log.info("...Arranging by script files for ReplaceSchema");
        }
        for (Entry<String, String> entry : scriptMap.entrySet()) {
            final String path = entry.getKey();
            arrangeScript(path);
        }
    }

    // ===================================================================================
    //                                                                                Copy
    //                                                                                ====
    protected void arrangeCopy(String src, String dest) {
        boolean cleanOption = false;
        if (dest.contains("df:clean")) {
            dest = Srl.replace(dest, "df:clean", "").trim();
            cleanOption = true;
        }
        final File destFile = new File(dest);
        if (!src.contains("/")) {
            throwRepsArrangeCopySrcNotPathException(src, dest);
        }
        final String pureName = Srl.substringLastRear(src, "/");
        if (pureName.startsWith("*.")) { // e.g. ./foo/*.sql
            final String ext = Srl.substringFirstRear(pureName, "*.");
            final File srcDir = new File(Srl.substringLastFront(src, "/*."));
            final List<String> elementList = extractElementList(ext, srcDir);
            final String extSuffix = "." + ext;
            if (isDestDirectory(destFile)) { // copy to all files
                // /- - - - - - - - - - - - - - - - - - - - -
                // src=./foo/*.sql, dest=./bar/ (dest=./bar)
                // - - - - - - - - - -/
                if (!destFile.exists()) {
                    destFile.mkdirs();
                }
                final String destBaseDir;
                if (dest.endsWith("/")) {
                    destBaseDir = Srl.substringLastFront(dest, "/");
                } else {
                    destBaseDir = dest;
                }
                if (cleanOption) {
                    deleteFile(ext, new File(destBaseDir));
                }
                if (!elementList.isEmpty()) {
                    for (String element : elementList) {
                        if (!element.endsWith(extSuffix)) { // just in case
                            continue;
                        }
                        final String srcPath = srcDir.getPath() + "/" + element;
                        final String destPath = destBaseDir + "/" + element;
                        copyFile(new File(srcPath), new File(destPath));
                    }
                } else {
                    if (cleanOption) { // exception if clean option
                        throwRepsArrangeCleanCopySrcNotFoundException(src, dest);
                    } else {
                        _log.info("*Not found the correspoinding copy src file: " + src);
                    }
                }
            } else { // copy to only one file
                // /- - - - - - - - - - - - - - - - - - - - -
                // src=./foo/*.sql, dest=./bar/baz.sql
                // - - - - - - - - - -/
                String onlyOneElement = null;
                for (String element : elementList) {
                    if (!element.endsWith(extSuffix)) { // just in case
                        continue;
                    }
                    if (onlyOneElement == null) {
                        onlyOneElement = element; // found the only one file
                    } else { // duplicate
                        throwRepsArrangeCopySrcDuplicateFileException(src, dest, onlyOneElement, element);
                    }
                }
                if (onlyOneElement != null) {
                    final String srcPath = srcDir.getPath() + "/" + onlyOneElement;
                    copyFile(new File(srcPath), destFile);
                } else {
                    _log.info("*Not found the corresponding copy src file: " + src);
                }
            }
        } else {
            // /- - - - - - - - - - - - - - - - - - - - -
            // src=./foo/bar.sql, dest=./baz/qux.sql
            // - - - - - - - - - -/
            final File srcFile = new File(src);
            if (!srcFile.exists()) {
                _log.info("*Not existing the copy src file: " + src);
                return;
            }
            copyFile(srcFile, destFile);
        }
    }

    protected boolean isDestDirectory(File destFile) {
        if (destFile.exists() && destFile.isDirectory()) {
            return true;
        }
        final String path = destFile.getPath();
        if (path.endsWith("/")) {
            return true;
        }
        final String pureName = Srl.substringLastRear(path, "/");
        if (!pureName.contains(".")) {
            return true;
        }
        return false;
    }

    protected List<String> extractElementList(String ext, File baseDir) {
        final String extSuffix = "." + ext;
        final String[] elementList = baseDir.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return Srl.endsWith(name, extSuffix);
            }
        });
        if (elementList == null) { // no file in the directory
            _log.info("*Not found the file in the copy src directory: " + baseDir.getPath());
            return DfCollectionUtil.emptyList();
        }
        return DfCollectionUtil.newArrayList(elementList);
    }

    protected void deleteFile(String ext, File baseDir) {
        final String extSuffix = "." + ext;
        final File[] elementList = baseDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return Srl.endsWith(name, extSuffix);
            }
        });
        if (elementList != null) {
            _log.info("  clean " + baseDir.getPath() + "/*" + extSuffix);
            for (File file : elementList) {
                file.delete();
            }
        }
    }

    protected void copyFile(File src, File dest) {
        _log.info("  copy " + src.getPath() + " to " + dest.getPath());
        if (dest.exists()) {
            dest.delete();
        }
        try {
            FileUtils.getFileUtils().copyFile(src, dest);
        } catch (IOException e) {
            String msg = "Failed to copy file: " + src + " to " + dest;
            throw new IllegalStateException(msg, e);
        }
    }

    protected void throwRepsArrangeCopySrcNotPathException(String src, String dest) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The path in src was not a path expression.");
        br.addItem("Advice");
        br.addElement("The path in src should be a path expression.");
        br.addElement("For example, './foo.txt' (should contain '/')");
        br.addItem("Source");
        br.addElement(src);
        br.addItem("Destination");
        br.addElement(dest);
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg);
    }

    protected void throwRepsArrangeCleanCopySrcNotFoundException(String src, String dest) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The src directory or file was not found in spite of clean copy.");
        br.addItem("Advice");
        br.addElement("The src directory or file should exist if clean copy.");
        br.addElement("Make sure your src path setting.");
        br.addItem("Source");
        br.addElement(src);
        br.addItem("Destination");
        br.addElement(dest);
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg);
    }

    protected void throwRepsArrangeCopySrcDuplicateFileException(String src, String dest, String first, String second) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The file corresponding to the extension was duplicate.");
        br.addItem("Advice");
        br.addElement("The file that has the extension should be the only one");
        br.addElement("when you use wild-card, e.g. '/*.sql', in copy src");
        br.addElement("and you specify a file in desc");
        br.addItem("Source");
        br.addElement(src);
        br.addItem("Destination");
        br.addElement(dest);
        br.addItem("Found Files");
        br.addElement(first);
        br.addElement(second);
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg);
    }

    // ===================================================================================
    //                                                                              Script
    //                                                                              ======
    protected void arrangeScript(String path) {
        final SystemScript script = new SystemScript();
        final String baseDir = Srl.substringLastFront(path, "/");
        final String scriptName = Srl.substringLastRear(path, "/");
        _log.info("...Executing the script: " + path);
        final ProcessResult processResult;
        try {
            processResult = script.execute(new File(baseDir), scriptName);
        } catch (SystemScriptUnsupportedScriptException ignored) {
            _log.info("Skipped the script for system mismatch: " + scriptName);
            return;
        }
        final String console = processResult.getConsole();
        if (Srl.is_NotNull_and_NotTrimmedEmpty(console)) {
            _log.info("Catched the console for " + scriptName + ":" + ln() + console);
        }
        final int exitCode = processResult.getExitCode();
        if (exitCode != 0) {
            throwRepsArrangeScriptFailureException(path, exitCode);
        }
    }

    protected void throwRepsArrangeScriptFailureException(String path, int exitCode) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to execute the script for ArrangeBeforeReps.");
        br.addItem("Path");
        br.addElement(path);
        br.addItem("Exit Code");
        br.addElement(exitCode);
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg);
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String replaceString(String text, String fromText, String toText) {
        return DfStringUtil.replace(text, fromText, toText);
    }
}
