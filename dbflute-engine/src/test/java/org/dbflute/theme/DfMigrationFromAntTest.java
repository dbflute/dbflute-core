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
package org.dbflute.theme;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.apache.tools.ant.util.FileUtils;
import org.dbflute.helper.filesystem.FileTextIO;
import org.dbflute.unit.EngineTestCase;

/**
 * @author jflute
 */
public class DfMigrationFromAntTest extends EngineTestCase {

    // ===================================================================================
    //                                                                           FileUtils
    //                                                                           =========
    public void test_FileUtils_to_Path() throws IOException {
        // ## Arrange ##
        String dirPath = getTestCaseBuildDir().getCanonicalPath() + "/fromant";
        File testDir = new File(dirPath);
        if (testDir.exists()) {
            testDir.delete();
        }
        testDir.mkdir();
        File srcFile = new File(dirPath + "/fromant-src.txt");
        File destByAntFile = new File(dirPath + "/fromant-desc-by-ant.txt");
        File destByNioFile = new File(dirPath + "/fromant-desc-by-nio.txt");
        String writtenText = "sea in \u821e\u6d5c"; // sea in maihama(as kanji)
        FileTextIO textIO = new FileTextIO().encodeAsUTF8();
        textIO.write(new FileOutputStream(srcFile), writtenText);
        assertTrue(srcFile.exists());
        try {
            // ## Act ##
            FileUtils.getFileUtils().copyFile(srcFile, destByAntFile); // AlterCheck uses
            Files.copy(srcFile.toPath(), destByNioFile.toPath(), StandardCopyOption.COPY_ATTRIBUTES);

            // ## Assert ##
            assertTrue(destByAntFile.exists());
            assertTrue(destByNioFile.exists());
            String byAntText = textIO.read(new FileInputStream(destByAntFile));
            String byNioText = textIO.read(new FileInputStream(destByNioFile));
            assertEquals(byAntText, byNioText);
        } finally {
            if (srcFile.exists()) {
                srcFile.delete();
            }
            if (destByAntFile.exists()) {
                destByAntFile.delete();
            }
            if (destByNioFile.exists()) {
                destByNioFile.delete();
            }
        }
    }
}
