/*
 * Copyright 2014-2021 the original author or authors.
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
package org.dbflute.properties.assistant.classification.element.proploading;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import org.dbflute.DfBuildProperties;
import org.dbflute.properties.DfLittleAdjustmentProperties;
import org.dbflute.properties.assistant.classification.DfClassificationElement;
import org.dbflute.properties.assistant.classification.DfClassificationTop;
import org.dbflute.properties.assistant.classification.allinone.DfClassificationAllInOneSqlExecutor;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 1.2.5 split from DfClassificationProperties (2021/07/10 Saturday at ikspiari)
 */
public class DfClsTableAllInOneArranger {

    public void arrangeAllInOneTableClassification(Map<String, DfClassificationTop> classificationTopMap, Connection conn, String sql) {
        final DfClassificationAllInOneSqlExecutor executor = new DfClassificationAllInOneSqlExecutor();
        final List<Map<String, String>> resultList = executor.executeAllInOneSql(conn, sql);
        for (Map<String, String> map : resultList) {
            final String classificationName = map.get("classificationName");
            final DfClassificationTop classificationTop;
            {
                DfClassificationTop tmpTop = classificationTopMap.get(classificationName);
                if (tmpTop == null) {
                    tmpTop = createAllInOneTableClassificationTop(classificationName);
                    classificationTopMap.put(classificationName, tmpTop);
                }
                classificationTop = tmpTop;
            }
            // *no check and merge it
            //if (alreadySet.contains(classificationName)) {
            //    throwClassificationAlreadyExistsInDfPropException(classificationName, "All-in-One");
            //}
            if (!classificationTop.hasTopComment()) {
                final String topComment = map.get(DfClassificationTop.KEY_TOP_COMMENT);
                classificationTop.setTopComment(topComment);
            }
            if (!classificationTop.hasCodeType()) {
                final String codeType;
                {
                    String tmpType = map.get(DfClassificationTop.KEY_CODE_TYPE);
                    if (Srl.is_Null_or_TrimmedEmpty(tmpType)) {
                        // for compatibility
                        tmpType = map.get(DfClassificationTop.KEY_DATA_TYPE);
                    }
                    codeType = tmpType;
                }
                classificationTop.setCodeType(codeType);
            }
            final DfClassificationElement element = new DfClassificationElement();
            element.setClassificationName(classificationName);
            element.acceptBasicItemMap(map);
            classificationTop.addClassificationElement(element);
        }
    }

    protected DfClassificationTop createAllInOneTableClassificationTop(final String classificationName) {
        final DfClassificationTop tmpTop = new DfClassificationTop(classificationName);
        final DfLittleAdjustmentProperties prop = getLittleAdjustmentProperties();
        tmpTop.setCheckClassificationCode(prop.isPlainCheckClassificationCode());
        tmpTop.setUndefinedHandlingType(prop.getClassificationUndefinedHandlingType());
        tmpTop.setCheckImplicitSet(false); // unsupported
        tmpTop.setCheckSelectedClassification(prop.isCheckSelectedClassification());
        tmpTop.setForceClassificationSetting(prop.isForceClassificationSetting());
        return tmpTop;
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfLittleAdjustmentProperties getLittleAdjustmentProperties() {
        return DfBuildProperties.getInstance().getLittleAdjustmentProperties();
    }
}
