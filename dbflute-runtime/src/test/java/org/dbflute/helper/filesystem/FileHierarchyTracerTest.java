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
package org.dbflute.helper.filesystem;

import java.io.File;
import java.util.List;

import org.dbflute.unit.RuntimeTestCase;
import org.dbflute.util.DfResourceUtil;
import org.dbflute.util.Srl;

/**
 * @author jflute
 */
public class FileHierarchyTracerTest extends RuntimeTestCase {

    public void test_trace() throws Exception {
        // ## Arrange ##
        FileHierarchyTracer tracer = new FileHierarchyTracer();
        String srcPathMark = "src/test/java";
        File buildDir = DfResourceUtil.getBuildDir(getClass());
        String buildPath = DfResourceUtil.getCanonicalPath(buildDir);
        File srcDir = new File(buildPath + "/../../" + srcPathMark);
        assertTrue(srcDir.exists());
        final List<String> fileNameList = newArrayList();
        final StringBuilder sb = new StringBuilder();

        // ## Act ##
        tracer.trace(srcDir, new FileHierarchyTracingHandler() {
            public boolean isTargetFileOrDir(File currentFile) {
                return currentFile.isDirectory() || currentFile.getName().endsWith("Test.java");
            }

            public void handleFile(File currentFile) {
                final String canonicalPath = DfResourceUtil.getCanonicalPath(currentFile);
                sb.append("\n").append(canonicalPath);
                fileNameList.add(Srl.substringLastFront(currentFile.getName(), ".java"));
            }
        });

        // ## Assert ##
        log(sb.toString());
        log("file count: " + fileNameList.size());
        assertTrue(fileNameList.contains(getClass().getSimpleName()));
    }
}
