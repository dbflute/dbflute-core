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
package org.seasar.dbflute.properties.assistant.freegen.xls;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.seasar.dbflute.exception.DfIllegalPropertySettingException;
import org.seasar.dbflute.exception.DfRequiredPropertyNotFoundException;
import org.seasar.dbflute.properties.assistant.freegen.DfFreeGenResource;
import org.seasar.dbflute.properties.assistant.freegen.DfFreeGenTable;
import org.seasar.dbflute.properties.assistant.freegen.reflector.DfFreeGenLazyReflector;
import org.seasar.dbflute.properties.assistant.freegen.reflector.DfFreeGenMethodConverter;
import org.seasar.dbflute.util.DfCollectionUtil;

/**
 * @author jflute
 */
public class DfXlsTableLoader {

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
    //     ; package = org.seasar.dbflute...
    //     ; className = FooDto
    // }
    // ; tableMap = map:{
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
    public DfFreeGenTable loadTable(String requestName, DfFreeGenResource resource, Map<String, Object> tableMap,
            Map<String, Map<String, String>> mappingMap) {
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
        HSSFWorkbook workbook;
        try {
            workbook = new HSSFWorkbook(new FileInputStream(new File(resourceFile)));
        } catch (IOException e) {
            String msg = "Failed to read the excel(xls): requestName=" + requestName + " resourceFile=" + resourceFile;
            throw new IllegalStateException(msg, e);
        }
        final HSSFSheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            String msg = "Not found the sheet name in the file: name=" + sheetName + " xls=" + resourceFile;
            throw new IllegalStateException(msg);
        }
        final List<Map<String, Object>> rowList = new ArrayList<Map<String, Object>>();
        for (int i = (rowBeginNumber - 1); i < Integer.MAX_VALUE; i++) {
            final HSSFRow row = sheet.getRow(i);
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
                rowList.add(beanMap);
            } else { // means empty row
                break;
            }
            for (DfFreeGenLazyReflector reflector : reflectorList) {
                reflector.reflect();
            }
        }
        return new DfFreeGenTable(tableMap, sheetName, rowList);
    }

    protected boolean processColumnValue(final String requestName, final Map<String, String> columnMap,
            final HSSFRow row, final Map<String, Object> beanMap, final String key, final String value,
            List<DfFreeGenLazyReflector> reflectorList, Map<String, Map<String, String>> mappingMap) {
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
        final HSSFCell cell = row.getCell(cellNumber);
        if (cell == null) {
            return false;
        }
        final HSSFRichTextString cellValue = cell.getRichStringCellValue();
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

    protected boolean convertByMethod(final String requestName, final Map<String, Object> beanMap, final String key,
            final String value, List<DfFreeGenLazyReflector> reflectorList) {
        return _methodConverter.processConvertMethod(requestName, beanMap, key, value, reflectorList);
    }
}
