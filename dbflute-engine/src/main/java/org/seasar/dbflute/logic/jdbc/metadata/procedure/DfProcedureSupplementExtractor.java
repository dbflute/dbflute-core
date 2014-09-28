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
package org.seasar.dbflute.logic.jdbc.metadata.procedure;

import java.util.Map;

import org.apache.torque.engine.database.model.UnifiedSchema;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfProcedureSourceInfo;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfTypeArrayInfo;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfTypeStructInfo;

/**
 * @author jflute
 * @since 0.9.7.6 (2010/11/19 Friday)
 */
public interface DfProcedureSupplementExtractor {

    /**
     * Extract the map of overload info. <br />
     * Same name and different type parameters of overload are unsupported.
     * @param unifiedSchema The unified schema to extract. (NotNull)
     * @return The map of parameter's overload info. (NotNull)
     */
    Map<String, Integer> extractParameterOverloadInfoMap(UnifiedSchema unifiedSchema);

    /**
     * Extract the map of array info. <br />
     * Same name and different type parameters of overload are unsupported.
     * @param unifiedSchema The unified schema to extract. (NotNull)
     * @return The map of array info. (NotNull)
     */
    Map<String, DfTypeArrayInfo> extractParameterArrayInfoMap(UnifiedSchema unifiedSchema);

    /**
     * Extract the map of struct info for procedure. <br />
     * (but it contains structs that are not defined as procedure parameters)
     * @param unifiedSchema The unified schema to extract. (NotNull)
     * @return The map of array info. (NotNull)
     */
    Map<String, DfTypeStructInfo> extractStructInfoMap(UnifiedSchema unifiedSchema);

    /**
     * @param catalog The catalog for procedure. (NullAllowed)
     * @param procedureName The name of procedure. (NotNull)
     * @param parameterName The name of parameter. (NotNull)
     * @return The map key for parameter info. (NotNull)
     */
    String generateParameterInfoMapKey(String catalog, String procedureName, String parameterName);

    /**
     * @param unifiedSchema The unified schema to extract. (NotNull)
     * @return The map for source info. The key is pure procedure name. e.g. SP_FOO (NotNull)
     */
    Map<String, DfProcedureSourceInfo> extractProcedureSourceInfo(UnifiedSchema unifiedSchema);

    /**
     * Suppress logging, e.g. SQL log
     */
    void suppressLogging();
}
