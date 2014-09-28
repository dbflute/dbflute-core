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
package org.seasar.dbflute.manage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.seasar.dbflute.unit.core.PlainTestCase;
import org.seasar.dbflute.util.DfResourceUtil;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 * @since 0.9.9.8 (2012/09/14 Friday)
 */
public class ManageRuntimeLicenseTest extends PlainTestCase {

    private static final String COPYRIGHT = "Copyright 2004-2014 the Seasar Foundation and the Others.";

    public void test_licensed_main() {
        // ## Arrange ##
        String srcPathMark = "src/main/java";
        File buildDir = DfResourceUtil.getBuildDir(getClass());
        String buildPath = DfResourceUtil.getCanonicalPath(buildDir);
        File srcDir = new File(buildPath + "/../../" + srcPathMark);
        assertTrue(srcDir.exists());
        List<File> unlicensedList = newArrayList();

        // ## Act ##
        checkUnlicensed(srcDir, unlicensedList);

        // ## Assert ##
        StringBuilder sb = new StringBuilder();
        for (File unlicensedFile : unlicensedList) {
            String path = Srl.replace(unlicensedFile.getPath(), "\\", "/");
            String rear = Srl.substringFirstRear(path, srcPathMark + "/");
            sb.append(ln()).append(rear);
        }
        sb.append(ln()).append(" count: ").append(unlicensedList.size());
        log(sb.toString());
        assertTrue(unlicensedList.isEmpty());
    }

    public void test_licensed_test() {
        // ## Arrange ##
        String srcPathMark = "src/test/java";
        File buildDir = DfResourceUtil.getBuildDir(getClass());
        String buildPath = DfResourceUtil.getCanonicalPath(buildDir);
        File srcDir = new File(buildPath + "/../../" + srcPathMark);
        assertTrue(srcDir.exists());
        List<File> unlicensedList = newArrayList();

        // ## Act ##
        checkUnlicensed(srcDir, unlicensedList);

        // ## Assert ##
        StringBuilder sb = new StringBuilder();
        for (File unlicensedFile : unlicensedList) {
            String path = Srl.replace(unlicensedFile.getPath(), "\\", "/");
            String rear = Srl.substringFirstRear(path, srcPathMark + "/");
            sb.append(ln()).append(rear);
        }
        sb.append(ln()).append(" count: ").append(unlicensedList.size());
        log(sb.toString());
        assertTrue(unlicensedList.isEmpty());
    }

    protected void checkUnlicensed(File currentFile, List<File> unlicensedList) {
        if (isPackageDir(currentFile)) {
            File[] subFiles = currentFile.listFiles(new FileFilter() {
                public boolean accept(File file) {
                    return isPackageDir(file) || isSourceFile(file);
                }
            });
            if (subFiles == null || subFiles.length == 0) {
                return;
            }
            for (File subFile : subFiles) {
                checkUnlicensed(subFile, unlicensedList);
            }
        } else if (isSourceFile(currentFile)) {
            doCheckUnlicensed(currentFile, unlicensedList);
        } else { // no way
            throw new IllegalStateException("Unknown file: " + currentFile);
        }
    }

    protected boolean isPackageDir(File file) {
        return file.isDirectory() && !file.getName().startsWith(".");
    }

    protected boolean isSourceFile(File file) {
        return file.getName().endsWith(".java");
    }

    protected void doCheckUnlicensed(File srcFile, List<File> unlicensedList) {
        if (srcFile == null) {
            String msg = "The argument 'targetFile' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        if (!srcFile.isFile()) {
            String msg = "The argument 'targetFile' should be file: " + srcFile;
            throw new IllegalArgumentException(msg);
        }
        BufferedReader br = null;
        boolean contains = false;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(srcFile), "UTF-8"));
            while (true) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }
                if (line.contains(COPYRIGHT)) {
                    contains = true;
                    break;
                }
            }
        } catch (IOException e) {
            String msg = "Failed to read the file: " + srcFile;
            throw new IllegalStateException(msg, e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ignored) {
                }
            }
        }
        if (!contains) {
            unlicensedList.add(srcFile);
        }
    }
}
