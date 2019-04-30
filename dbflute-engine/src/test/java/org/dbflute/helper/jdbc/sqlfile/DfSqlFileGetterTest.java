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
package org.dbflute.helper.jdbc.sqlfile;

import java.io.File;
import java.util.List;

import org.dbflute.helper.jdbc.sqlfile.DfSqlFileGetter;
import org.junit.Assert;
import org.dbflute.unit.EngineTestCase;
import org.dbflute.util.DfResourceUtil;

/**
 * @author jflute
 * @since 0.5.7 (2007/11/03 Saturday)
 */
public class DfSqlFileGetterTest extends EngineTestCase {

    public void test_getSqlFileList() throws Exception {
        final DfSqlFileGetter getter = new DfSqlFileGetter();
        final File buildDir = DfResourceUtil.getBuildDir(getClass());
        final String canonicalPath = DfResourceUtil.getCanonicalPath(buildDir);

        final List<File> sqlFileList = getter.getSqlFileList(canonicalPath);
        if (sqlFileList.size() < 2) {
            Assert.fail();
        }
        for (File file : sqlFileList) {
            log(file);
        }
    }
}
