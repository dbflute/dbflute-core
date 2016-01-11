/*
 * Copyright 2014-2016 the original author or authors.
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
package org.dbflute.logic.manage.freegen.table.webcls;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.dbflute.DfBuildProperties;
import org.dbflute.exception.DfIllegalPropertySettingException;
import org.dbflute.helper.mapstring.MapListFile;
import org.dbflute.logic.manage.freegen.DfFreeGenMapProp;
import org.dbflute.logic.manage.freegen.DfFreeGenResource;
import org.dbflute.logic.manage.freegen.DfFreeGenTable;
import org.dbflute.logic.manage.freegen.DfFreeGenTableLoader;
import org.dbflute.properties.DfBasicProperties;
import org.dbflute.properties.assistant.classification.DfClassificationElement;
import org.dbflute.properties.assistant.classification.DfClassificationLiteralArranger;
import org.dbflute.properties.assistant.classification.DfClassificationTop;
import org.dbflute.properties.assistant.classification.DfRefClsElement;
import org.dbflute.util.Srl;

/**
 * @author jflute
 */
public class DfWebClsTableLoader implements DfFreeGenTableLoader {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    // *very similar logic also exists on MailFlute
    public static final String META_DELIMITER = ">>>";
    public static final String COMMENT_BEGIN = "/*";
    public static final String COMMENT_END = "*/";
    public static final String TITLE_BEGIN = "[";
    public static final String TITLE_END = "]";
    public static final String SUBJECT_LABEL = "subject:";
    public static final String OPTION_LABEL = "option:";
    public static final String PLUS_HTML_OPTION = "+html";
    public static final String PROPDEF_PREFIX = "-- !!";
    // option check is not here because it can be added in MailFlute
    //public static final Set<String> optionSet;
    //static {
    //    optionSet = Collections.unmodifiableSet(DfCollectionUtil.newLinkedHashSet(PLUS_HTML_OPTION));
    //}
    public static final List<String> allowedPrefixList; // except first line (comment)
    static {
        allowedPrefixList = Arrays.asList(OPTION_LABEL, PROPDEF_PREFIX);
    }
    protected static final String LF = "\n";
    protected static final String CR = "\r";
    protected static final String CRLF = "\r\n";

    // ===================================================================================
    //                                                                          Load Table
    //                                                                          ==========
    // ; resourceMap = map:{
    //     ; baseDir = ../src/main
    //     ; resourceType = WEB_CLS
    //     ; resourceFile = ../../../dockside_webcls.properties
    // }
    // ; outputMap = map:{
    //     ; templateFile = LaWebCDef.vm
    //     ; outputDirectory = $$baseDir$$/java
    //     ; package = org.dbflute...
    //     ; className = unused
    // }
    // ; tableMap = map:{
    // }
    public DfFreeGenTable loadTable(String requestName, DfFreeGenResource resource, DfFreeGenMapProp mapProp) {
        final Map<String, Object> tableMap = mapProp.getTableMap();
        final String resourceFile = resource.getResourceFile();
        final Map<String, Object> webclsMap;
        try {
            webclsMap = new MapListFile().readMap(new FileInputStream(resourceFile));
        } catch (FileNotFoundException e) {
            throw new DfIllegalPropertySettingException("Not found the dfprop file: " + resourceFile, e);
        } catch (IOException e) {
            throw new DfIllegalPropertySettingException("Cannot read the the dfprop file: " + resourceFile, e);
        }
        boolean hasRefCls = false;
        final DfClassificationLiteralArranger literalArranger = new DfClassificationLiteralArranger();
        final List<DfClassificationTop> topList = new ArrayList<DfClassificationTop>();
        for (Entry<String, Object> entry : webclsMap.entrySet()) {
            final String classificationName = entry.getKey();
            final DfClassificationTop classificationTop = new DfClassificationTop();
            topList.add(classificationTop);
            classificationTop.setClassificationName(classificationName);
            @SuppressWarnings("unchecked")
            final List<Map<String, Object>> elementMapList = (List<Map<String, Object>>) entry.getValue();
            for (Map<String, Object> elementMap : elementMapList) {
                if (isElementMapClassificationTop(elementMap)) {
                    classificationTop.acceptClassificationTopBasicItemMap(elementMap);
                } else {
                    final DfClassificationElement element = new DfClassificationElement();
                    if (isElementMapRefCls(elementMap)) {
                        classificationTop.addRefClsElement(createRefClsElement(elementMap));
                        hasRefCls = true;
                    } else {
                        literalArranger.arrange(classificationName, elementMap);
                        element.acceptBasicItemMap(elementMap);
                        classificationTop.addClassificationElement(element);
                    }
                }
            }
        }
        tableMap.put("classificationTopList", topList);
        tableMap.put("classificationNameList", topList.stream().map(top -> {
            return top.getClassificationName();
        }).collect(Collectors.toList()));
        tableMap.put("hasRefCls", hasRefCls);
        tableMap.put("allcommonPackage", getBasicProperties().getBaseCommonPackage());
        return new DfFreeGenTable(tableMap, "webcls", Collections.emptyList());
    }

    protected boolean isElementMapClassificationTop(Map<String, Object> elementMap) {
        return elementMap.get(DfClassificationTop.KEY_TOP_COMMENT) != null;
    }

    protected boolean isElementMapRefCls(Map<String, Object> elementMap) {
        return elementMap.get(DfRefClsElement.KEY_REFCLS) != null;
    }

    protected DfRefClsElement createRefClsElement(Map<String, Object> elementMap) {
        // TODO jflute webcls (2016/01/11)
        final String refCls = (String) elementMap.get(DfRefClsElement.KEY_REFCLS);
        final String projectName;
        final String refClsName;
        if (refCls.contains("@")) {
            projectName = Srl.substringFirstFront(refCls, "@");
            refClsName = Srl.substringFirstRear(refCls, "@");
        } else {
            projectName = null;
            refClsName = refCls;
        }
        final String classificationType = buildClassificationType(refClsName);
        return new DfRefClsElement(projectName, refClsName, classificationType);
    }

    protected String buildClassificationType(String refClsName) {
        return getBasicProperties().getProjectPrefix() + "CDef." + refClsName;
    }

    protected DfBasicProperties getBasicProperties() {
        return DfBuildProperties.getInstance().getBasicProperties();
    }
}
