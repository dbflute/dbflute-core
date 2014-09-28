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
package org.seasar.dbflute.logic.jdbc.metadata.various.array;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.engine.database.model.UnifiedSchema;
import org.seasar.dbflute.helper.StringKeyMap;
import org.seasar.dbflute.helper.StringSet;
import org.seasar.dbflute.helper.jdbc.facade.DfJdbcFacade;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfProcedureArgumentInfo;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfTypeArrayInfo;
import org.seasar.dbflute.logic.jdbc.metadata.procedure.DfProcedureParameterNativeExtractorOracle;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 * @since 0.9.7.6 (2010/11/20 Saturday)
 */
public class DfArrayExtractorOracle {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Log _log = LogFactory.getLog(DfArrayExtractorOracle.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DataSource _dataSource;

    protected final boolean _suppressLogging;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfArrayExtractorOracle(DataSource dataSource, boolean suppressLogging) {
        _dataSource = dataSource;
        _suppressLogging = suppressLogging;
    }

    // ===================================================================================
    //                                                                             Extract
    //                                                                             =======
    public StringKeyMap<DfTypeArrayInfo> extractFlatArrayInfoMap(UnifiedSchema unifiedSchema) {
        final StringKeyMap<DfTypeArrayInfo> firstMap = doExtractFlatArrayInfoFirstMap(unifiedSchema);
        if (!firstMap.isEmpty()) {
            return firstMap;
        }
        // if ALL_COLL_TYPES is unsupported at the Oracle version
        return doExtractFlatArrayInfoSecondMap(unifiedSchema); // second
    }

    // ===================================================================================
    //                                                                    First Array Info
    //                                                                    ================
    protected StringKeyMap<DfTypeArrayInfo> doExtractFlatArrayInfoFirstMap(UnifiedSchema unifiedSchema) {
        final List<Map<String, String>> resultList = selectFirstArray(unifiedSchema);
        final StringKeyMap<DfTypeArrayInfo> arrayTypeMap = StringKeyMap.createAsFlexibleOrdered();
        for (Map<String, String> map : resultList) {
            final String typeName = buildArrayTypeName(map.get("TYPE_NAME"), unifiedSchema);
            final DfTypeArrayInfo arrayInfo = new DfTypeArrayInfo(unifiedSchema, typeName);
            final String elementTypeOwner = map.get("ELEM_TYPE_OWNER"); // ARRAY and STRUCT only
            final String elementTypeName = map.get("ELEM_TYPE_NAME");
            final String elementType = Srl.connectPrefix(elementTypeName, elementTypeOwner, ".");
            arrayInfo.setElementType(elementType);
            arrayTypeMap.put(typeName, arrayInfo);
        }
        return arrayTypeMap;
    }

    protected List<Map<String, String>> selectFirstArray(UnifiedSchema unifiedSchema) {
        final DfJdbcFacade facade = new DfJdbcFacade(_dataSource);
        final List<String> columnList = new ArrayList<String>();
        columnList.add("TYPE_NAME");
        columnList.add("COLL_TYPE");
        columnList.add("ELEM_TYPE_OWNER");
        columnList.add("ELEM_TYPE_NAME");
        columnList.add("LENGTH");
        columnList.add("PRECISION");
        columnList.add("SCALE");
        final String sql = buildFirstArraySql(unifiedSchema);
        final List<Map<String, String>> resultList;
        try {
            log(sql);
            resultList = facade.selectStringList(sql, columnList);
        } catch (Exception continued) {
            // because it's basically assist info
            log("Failed to select first array info: " + continued.getMessage());
            return DfCollectionUtil.emptyList();
        }
        return resultList;
    }

    protected String buildFirstArraySql(UnifiedSchema unifiedSchema) {
        final StringBuilder sb = new StringBuilder();
        sb.append("select *");
        sb.append(" from ALL_COLL_TYPES");
        sb.append(" where OWNER = '").append(unifiedSchema.getPureSchema()).append("'");
        sb.append(" order by TYPE_NAME");
        return sb.toString();
    }

    // ===================================================================================
    //                                                                   Second Array Info
    //                                                                   =================
    protected StringKeyMap<DfTypeArrayInfo> doExtractFlatArrayInfoSecondMap(UnifiedSchema unifiedSchema) {
        final StringKeyMap<DfTypeArrayInfo> flatArrayInfoMap = StringKeyMap.createAsFlexibleOrdered();
        final List<DfProcedureArgumentInfo> argInfoList = extractProcedureArgumentInfoList(unifiedSchema);
        for (int i = 0; i < argInfoList.size(); i++) {
            final DfProcedureArgumentInfo argInfo = argInfoList.get(i);
            final String argumentName = argInfo.getArgumentName();
            if (Srl.is_Null_or_TrimmedEmpty(argumentName)) {
                continue;
            }
            final String dataType = argInfo.getDataType();
            if (!isDataTypeArray(dataType)) {
                continue;
            }
            final String typeName = argInfo.getTypeName();
            if (Srl.is_Null_or_TrimmedEmpty(typeName)) {
                continue;
            }
            setupFlatArrayInfo(flatArrayInfoMap, argInfoList, argInfo, i);
        }
        final StringSet allArrayTypeSet = extractSimpleArrayNameSet(unifiedSchema);
        for (String allArrayTypeName : allArrayTypeSet) {
            if (!flatArrayInfoMap.containsKey(allArrayTypeName)) {
                final DfTypeArrayInfo arrayInfo = new DfTypeArrayInfo(unifiedSchema, allArrayTypeName);
                arrayInfo.setElementType("Unknown"); // the way to get the info is also unknown
                flatArrayInfoMap.put(allArrayTypeName, arrayInfo);
            }
        }
        return flatArrayInfoMap;
    }

    protected void setupFlatArrayInfo(StringKeyMap<DfTypeArrayInfo> flatArrayInfoMap,
            List<DfProcedureArgumentInfo> argInfoList, DfProcedureArgumentInfo argInfo, int index) {
        final UnifiedSchema owner = UnifiedSchema.createAsDynamicSchema(null, argInfo.getTypeOwner());
        final String realTypeName = buildArrayTypeName(argInfo);
        final DfTypeArrayInfo arrayInfo = new DfTypeArrayInfo(owner, realTypeName);
        final boolean nestedArray = reflectArrayElementType(argInfoList, index, arrayInfo);
        flatArrayInfoMap.put(realTypeName, arrayInfo);
        if (nestedArray) {
            final int nextIndex = (index + 1);
            final DfProcedureArgumentInfo nextArgInfo = argInfoList.get(nextIndex);
            setupFlatArrayInfo(flatArrayInfoMap, argInfoList, nextArgInfo, nextIndex); // recursive call
        }
    }

    protected boolean reflectArrayElementType(List<DfProcedureArgumentInfo> argInfoList, int i,
            DfTypeArrayInfo arrayInfo) {
        boolean nestedArray = false;
        final int nextIndex = (i + 1);
        if (argInfoList.size() > nextIndex) { // element type is in data type of next record
            final DfProcedureArgumentInfo nextInfo = argInfoList.get(nextIndex);
            if (Srl.is_Null_or_TrimmedEmpty(nextInfo.getArgumentName())) { // element record's argument is null
                final String typeName = nextInfo.getTypeName();
                final String dataType = nextInfo.getDataType();
                final String elementType;
                if (Srl.is_NotNull_and_NotTrimmedEmpty(typeName)) { // not scalar (array or struct)
                    if (isDataTypeArray(dataType)) { // can get one more record (Oracle's specification)
                        nestedArray = true;
                    }
                    elementType = buildArrayTypeName(nextInfo);
                } else { // scalar element
                    elementType = dataType;
                }
                arrayInfo.setElementType(elementType);
            }
        } else {
            log("*Unexpected, no next record for array meta: " + arrayInfo);
            arrayInfo.setElementType("Unknown"); // basically no way but just in case
        }
        return nestedArray;
    }

    protected boolean isDataTypeArray(String dataType) {
        return Srl.containsAnyIgnoreCase(dataType, "TABLE", "VARRAY");
    }

    protected boolean isDataTypeStruct(String dataType) {
        return Srl.equalsIgnoreCase(dataType, "OBJECT");
    }

    protected String buildArrayTypeName(DfProcedureArgumentInfo argInfo) {
        return argInfo.buildArrayTypeName();
    }

    // ===================================================================================
    //                                                                       Argument Info
    //                                                                       =============
    protected List<DfProcedureArgumentInfo> extractProcedureArgumentInfoList(UnifiedSchema unifiedSchema) {
        final DfProcedureParameterNativeExtractorOracle extractor = createProcedureParameterExtractorOracle();
        return extractor.extractProcedureArgumentInfoList(unifiedSchema);
    }

    protected DfProcedureParameterNativeExtractorOracle createProcedureParameterExtractorOracle() {
        return new DfProcedureParameterNativeExtractorOracle(_dataSource, _suppressLogging);
    }

    // ===================================================================================
    //                                                                    Simple Type Info
    //                                                                    ================
    protected StringSet extractSimpleArrayNameSet(UnifiedSchema unifiedSchema) {
        final List<Map<String, String>> resultList = selectSimpleArray(unifiedSchema);
        final StringSet arrayTypeSet = StringSet.createAsFlexibleOrdered();
        for (Map<String, String> map : resultList) {
            arrayTypeSet.add(buildArrayTypeName(map.get("TYPE_NAME"), unifiedSchema));
        }
        return arrayTypeSet;
    }

    protected List<Map<String, String>> selectSimpleArray(UnifiedSchema unifiedSchema) {
        final DfJdbcFacade facade = new DfJdbcFacade(_dataSource);
        final List<String> columnList = new ArrayList<String>();
        columnList.add("TYPE_NAME");
        final String sql = buildSimpleArraySql(unifiedSchema);
        final List<Map<String, String>> resultList;
        try {
            log(sql);
            resultList = facade.selectStringList(sql, columnList);
        } catch (Exception continued) {
            // because it's basically assist info
            log("Failed to select simple array info: " + continued.getMessage());
            return DfCollectionUtil.emptyList();
        }
        return resultList;
    }

    protected String buildSimpleArraySql(UnifiedSchema unifiedSchema) {
        final StringBuilder sb = new StringBuilder();
        sb.append("select *");
        sb.append(" from ALL_TYPES");
        sb.append(" where OWNER = '").append(unifiedSchema.getPureSchema()).append("'");
        sb.append(" and TYPECODE = 'COLLECTION'");
        sb.append(" order by TYPE_NAME");
        return sb.toString();
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    public String buildArrayTypeName(String typeName, UnifiedSchema unifiedSchema) {
        return Srl.connectPrefix(typeName, unifiedSchema.getPureSchema(), ".");
    }

    // ===================================================================================
    //                                                                             Logging
    //                                                                             =======
    protected void log(String msg) {
        if (_suppressLogging) {
            return;
        }
        _log.info(msg);
    }
}
