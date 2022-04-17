/*
 * Copyright 2014-2022 the original author or authors.
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
package org.dbflute.logic.replaceschema.loaddata.delimiter.secretary;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.dbflute.DfBuildProperties;
import org.dbflute.bhv.exception.SQLExceptionAdviser;
import org.dbflute.exception.DfDelimiterDataRegistrationFailureException;
import org.dbflute.exception.DfDelimiterDataTableNotFoundException;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.logic.replaceschema.loaddata.base.DfAbsractDataWriter.StringProcessor;
import org.dbflute.properties.DfBasicProperties;

/**
 * @author jflute
 * @since 1.2.5 extracted from DfDelimiterDataWriterImpl (2021/01/20 Wednesday at roppongi japanese)
 */
public class DfDelimiterDataWritingExceptionThrower {

    protected final SQLExceptionAdviser _adviser = new SQLExceptionAdviser();

    public void throwTableNotFoundException(String fileName, String tableDbName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The table specified on the delimiter file was not found in the schema.");
        br.addItem("Advice");
        br.addElement("Please confirm the name about its spelling.");
        br.addElement("And confirm that whether the DLL executions have errors.");
        br.addItem("Delimiter File");
        br.addElement(fileName);
        br.addItem("Table");
        br.addElement(tableDbName);
        final String msg = br.buildExceptionMessage();
        throw new DfDelimiterDataTableNotFoundException(msg);
    }

    public String buildFailureMessage(String fileName, String tableDbName, String executedSql, List<String> valueList,
            Map<String, Map<String, Class<?>>> bindTypeCacheMap, Map<String, Map<String, StringProcessor>> stringProcessorCacheMap,
            SQLException sqlEx) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to register the table data.");
        br.addItem("Advice");
        if (sqlEx != null) {
            br.addElement("Please confirm the SQLException message.");
            final String advice = _adviser.askAdvice(sqlEx, getBasicProperties().getCurrentDBDef());
            if (advice != null && advice.trim().length() > 0) {
                br.addElement("*" + advice);
            }
        } else {
            br.addElement("Please confirm the nested exception message.");
            br.addElement("and your delimiter data file.");
        }
        br.addItem("Delimiter File");
        br.addElement(fileName);
        br.addItem("Table");
        br.addElement(tableDbName);
        if (sqlEx != null) {
            br.addItem("SQLException");
            br.addElement(sqlEx.getClass().getName());
            br.addElement(sqlEx.getMessage());
        }
        br.addItem("Target Row");
        br.addElement("(derived from non-batch retry)");
        br.addElement(DfDelimiterDataRegistrationFailureException.RETRY_STORY_VARIABLE); // replaced at getMessage()
        br.addItem("Executed SQL");
        br.addElement(executedSql);
        if (!valueList.isEmpty()) { // basically when batch update is suppressed
            br.addItem("Bound Values");
            br.addElement(valueList);
        }
        final Map<String, Class<?>> bindTypeMap = bindTypeCacheMap.get(tableDbName);
        if (bindTypeMap != null) {
            br.addItem("Bind Type");
            final Set<Entry<String, Class<?>>> entrySet = bindTypeMap.entrySet();
            for (Entry<String, Class<?>> entry : entrySet) {
                br.addElement(entry.getKey() + " = " + entry.getValue());
            }
        }
        final Map<String, StringProcessor> stringProcessorMap = stringProcessorCacheMap.get(tableDbName);
        if (bindTypeMap != null) {
            br.addItem("String Processor");
            final Set<Entry<String, StringProcessor>> entrySet = stringProcessorMap.entrySet();
            for (Entry<String, StringProcessor> entry : entrySet) {
                br.addElement(entry.getKey() + " = " + entry.getValue());
            }
        }
        return br.buildExceptionMessage();
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfBuildProperties getProperties() {
        return DfBuildProperties.getInstance();
    }

    protected DfBasicProperties getBasicProperties() {
        return getProperties().getBasicProperties();
    }
}
