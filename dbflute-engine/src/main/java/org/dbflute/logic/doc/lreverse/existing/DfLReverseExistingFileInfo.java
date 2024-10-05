/*
 * Copyright 2014-2024 the original author or authors.
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
package org.dbflute.logic.doc.lreverse.existing;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author jflute
 * @since 1.2.9 (2024/10/04 at ichihara)
 */
public interface DfLReverseExistingFileInfo {

    /**
     * @return The map of existing file's table names. (NotNull)
     */
    Map<File, List<String>> getExistingFileTableListMap();

    /**
     * @return The map of table name's all existing files. (NotNull)
     */
    Map<String, List<File>> getTableAllExistingFileListMap();

    /**
     * @return The map of table name's first existing files. (NotNull)
     */
    Map<String, File> getTableFirstExistingFileMap();
}
