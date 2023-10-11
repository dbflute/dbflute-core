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
package org.dbflute.logic.manage.freegen.table.xls;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.dbflute.exception.DfIllegalPropertySettingException;
import org.dbflute.exception.DfRequiredPropertyNotFoundException;
import org.dbflute.helper.io.xls.DfXlsFactory;
import org.dbflute.logic.manage.freegen.DfFreeGenMapProp;
import org.dbflute.logic.manage.freegen.DfFreeGenMetaData;
import org.dbflute.logic.manage.freegen.DfFreeGenResource;
import org.dbflute.logic.manage.freegen.DfFreeGenTableLoader;
import org.dbflute.logic.manage.freegen.reflector.DfFreeGenLazyReflector;
import org.dbflute.logic.manage.freegen.reflector.DfFreeGenMethodConverter;
import org.dbflute.util.DfCollectionUtil;

/**
 * @author jflute
 * @author p1us2er0
 */
public class DfXlsTableLoader implements DfFreeGenTableLoader {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfFreeGenMethodConverter _methodConverter = new DfFreeGenMethodConverter();

    // ===================================================================================
    //                                                                          Load Table
    //                                                                          ==========
    // ; resourceMap = map:{
    //     ; resourceType = XLS
    //     ; resourceFile = ../../...
    // }
    // ; outputMap = map:{
    //     ; templateFile = CsvDto.vm
    //     ; outputDirectory = ../src/main/java
    //     ; package = org.dbflute...
    //     ; className = FooDto
    // }
    // ; optionMap = map:{
    //     ; sheetName = [sheet-name]
    //     ; rowBeginNumber = 3
    //     ; columnMap = map:{
    //         ; name = 3
    //         ; type = 4
    //     }
    //     ; mappingMap = map:{
    //         ; type = map:{
    //             ; INTEGER = Integer
    //             ; VARCHAR = String
    //         }
    //     }
    // }
    @Override
    public DfFreeGenMetaData loadTable(String requestName, DfFreeGenResource resource, DfFreeGenMapProp mapProp) {
        final Map<String, Object> tableMap = mapProp.getOptionMap();
        final Map<String, Map<String, String>> mappingMap = mapProp.getMappingMap();
        if (tableMap == null || tableMap.isEmpty()) {
            String msg = "The tableMap was not found in the FreeGen property: " + requestName;
            throw new DfRequiredPropertyNotFoundException(msg);
        }
        final String sheetName = (String) tableMap.get("sheetName");
        if (sheetName == null) {
            String msg = "The sheetName was not found in the FreeGen property: " + requestName;
            throw new DfRequiredPropertyNotFoundException(msg);
        }
        final Integer rowBeginNumber;
        {
            final String numStr = (String) tableMap.get("rowBeginNumber");
            if (numStr == null) {
                String msg = "The rowBeginNumber was not found in the FreeGen property: " + requestName;
                throw new DfRequiredPropertyNotFoundException(msg);
            }
            rowBeginNumber = Integer.valueOf(numStr);
        }
        final String resourceFile = resource.getResourceFile();
        @SuppressWarnings("unchecked")
        final Map<String, String> columnMap = (Map<String, String>) tableMap.get("columnMap");
        final Workbook workbook = DfXlsFactory.instance().createWorkbook(new File(resourceFile));
        final Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            String msg = "Not found the sheet name in the file: name=" + sheetName + " xls=" + resourceFile;
            throw new IllegalStateException(msg);
        }
        final List<Map<String, Object>> columnList = new ArrayList<Map<String, Object>>(); // rows
        for (int i = (rowBeginNumber - 1); i < Integer.MAX_VALUE; i++) {
            final Row row = sheet.getRow(i);
            if (row == null) {
                break;
            }
            final Map<String, Object> beanMap = DfCollectionUtil.newLinkedHashMap();
            final List<DfFreeGenLazyReflector> reflectorList = DfCollectionUtil.newArrayList();
            boolean exists = false;
            for (Entry<String, String> entry : columnMap.entrySet()) {
                final String key = entry.getKey();
                final String value = entry.getValue();
                if (value == null) {
                    String msg = "Not found the value of the key in FreeGen " + requestName + ": " + key;
                    throw new DfIllegalPropertySettingException(msg);
                }
                if (processColumnValue(requestName, columnMap, row, beanMap, key, value, reflectorList, mappingMap)) {
                    exists = true;
                }
            }
            prepareColumnNameConversion(requestName, beanMap, reflectorList);
            if (exists) {
                columnList.add(beanMap);
            } else { // means empty row
                break;
            }
            for (DfFreeGenLazyReflector reflector : reflectorList) {
                reflector.reflect();
            }
        }
        final String tableName = sheetName; // basically unused, also for compatible
        return DfFreeGenMetaData.asOnlyOne(tableMap, tableName, columnList);
    }

    protected boolean processColumnValue(final String requestName, final Map<String, String> columnMap, final Row row,
            final Map<String, Object> beanMap, final String key, final String value, List<DfFreeGenLazyReflector> reflectorList,
            Map<String, Map<String, String>> mappingMap) {
        if (convertByMethod(requestName, beanMap, key, value, reflectorList)) {
            return false;
        }
        // normal setting (cell number)
        boolean exists = false;
        final Integer cellNumber;
        try {
            cellNumber = Integer.valueOf(value) - 1;
        } catch (NumberFormatException e) {
            String msg = "The property value should be Integer in FreeGen " + requestName + ":";
            msg = msg + " key=" + key + " value=" + value;
            throw new DfIllegalPropertySettingException(msg);
        }
        final Cell cell = row.getCell(cellNumber);
        if (cell == null) {
            return false;
        }
        final RichTextString cellValue = cell.getRichStringCellValue();
        if (cellValue == null) {
            return false;
        }
        exists = true;
        String resultValue = cellValue.getString();
        final Map<String, String> mapping = mappingMap.get(key);
        if (mapping != null) {
            final String mappingValue = mapping.get(resultValue);
            if (mappingValue != null) {
                resultValue = mappingValue;
            }
        }
        beanMap.put(key, resultValue);
        return exists;
    }

    protected void prepareColumnNameConversion(String requestName, final Map<String, Object> beanMap,
            final List<DfFreeGenLazyReflector> reflectorList) {
        convertByMethod(requestName, beanMap, "camelizedName", "df:camelize(name)", reflectorList);
        convertByMethod(requestName, beanMap, "capCamelName", "df:capCamel(name)", reflectorList);
        convertByMethod(requestName, beanMap, "uncapCamelName", "df:uncapCamel(name)", reflectorList);
    }

    protected boolean convertByMethod(final String requestName, final Map<String, Object> beanMap, final String key, final String value,
            List<DfFreeGenLazyReflector> reflectorList) {
        return _methodConverter.processConvertMethod(requestName, beanMap, key, value, reflectorList);
    }
}
