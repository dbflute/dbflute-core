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
package org.dbflute.infra.diffmap;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.dbflute.exception.DfPropFileReadFailureException;
import org.dbflute.exception.DfPropFileWriteFailureException;
import org.dbflute.helper.dfmap.DfMapFile;
import org.dbflute.helper.message.ExceptionMessageBuilder;

/**
 * The file handling for difference map.
 * @author jflute
 * @since 0.9.7.1 (2010/06/06 Sunday)
 */
public class DfDiffMapFile {

    // ===================================================================================
    //                                                                               Read
    //                                                                              ======
    public Map<String, Object> readMap(InputStream ins) {
        final DfMapFile mapFile = createMapFile();
        try {
            return mapFile.readMap(ins);
        } catch (Exception e) {
            throwDfPropFileReadFailureException(ins, e);
            return null; // unreachable
        }
    }

    protected void throwDfPropFileReadFailureException(InputStream ins, Exception e) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to read the diff-map file.");
        br.addItem("Advice");
        br.addElement("Make sure the map-string is correct in the file.");
        br.addElement("For exapmle, the number of start and end braces are the same.");
        br.addItem("DBFlute Property");
        br.addElement(ins);
        final String msg = br.buildExceptionMessage();
        throw new DfPropFileReadFailureException(msg, e);
    }

    // ===================================================================================
    //                                                                               Write
    //                                                                               =====
    public void writeMap(OutputStream ous, Map<String, Object> map) {
        final DfMapFile mapFile = createMapFile();
        try {
            mapFile.writeMap(ous, map);
        } catch (Exception e) {
            throwDfPropFileWriteFailureException(ous, e);
        }
    }

    protected void throwDfPropFileWriteFailureException(OutputStream ous, Exception e) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to write the diff-map file.");
        br.addItem("DBFlute Property");
        br.addElement(ous);
        final String msg = br.buildExceptionMessage();
        throw new DfPropFileWriteFailureException(msg, e);
    }

    // ===================================================================================
    //                                                                            Map File
    //                                                                            ========
    protected DfMapFile createMapFile() {
        return new DfMapFile(); // migrated for performance (2018/05/05) 
    }
}