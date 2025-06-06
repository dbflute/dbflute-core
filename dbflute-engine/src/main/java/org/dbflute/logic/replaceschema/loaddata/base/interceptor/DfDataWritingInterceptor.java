/*
 * Copyright 2014-2025 the original author or authors.
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
package org.dbflute.logic.replaceschema.loaddata.base.interceptor;

import java.util.Map;

import org.dbflute.logic.jdbc.metadata.info.DfColumnMeta;
import org.dbflute.logic.replaceschema.loaddata.base.DfLoadedSchemaTable;

/**
 * @author jflute
 */
public interface DfDataWritingInterceptor {

    void processBeforeHandlingTable(DfLoadedSchemaTable schemaTable, Map<String, DfColumnMeta> columnInfoMap);

    void processFinallyHandlingTable(DfLoadedSchemaTable schemaTable, Map<String, DfColumnMeta> columnInfoMap);
}
