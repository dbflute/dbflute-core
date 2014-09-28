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
package org.seasar.dbflute.helper.filesystem;

import java.io.File;
import java.io.IOException;

/**
 * The handler of file hierarchy.
 * <pre>
 * tracer.trace(srcDir, new FileHierarchyTracingHandler() {
 *     public boolean isTargetFileOrDir(File currentFile) {
 *         return currentFile.isDirectory() || currentFile.getName().endsWith(".java");
 *     }
 *     public void handleFile(File currentFile) {
 *         ...
 *     }
 * }
 * </pre>
 * @author jflute
 */
public interface FileHierarchyTracingHandler {

    /**
     * Is the file or directory trace target?
     * @param currentFile The object of current file, file or directory. (NotNull)
     * @return The determination, true or false.
     */
    boolean isTargetFileOrDir(File currentFile);

    /**
     * Handle the file found by tracing.
     * @param currentFile The object of current file, always file (not directory). (NotNull)
     * @throws IOException When it fails by the IO failure.
     */
    void handleFile(File currentFile) throws IOException;
}
