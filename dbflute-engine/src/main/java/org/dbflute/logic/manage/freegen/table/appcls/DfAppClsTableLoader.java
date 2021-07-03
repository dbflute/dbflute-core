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
package org.dbflute.logic.manage.freegen.table.appcls;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.dbflute.DfBuildProperties;
import org.dbflute.exception.DfIllegalPropertySettingException;
import org.dbflute.helper.dfmap.DfMapFile;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.logic.manage.freegen.DfFreeGenMapProp;
import org.dbflute.logic.manage.freegen.DfFreeGenMetaData;
import org.dbflute.logic.manage.freegen.DfFreeGenResource;
import org.dbflute.logic.manage.freegen.DfFreeGenTableLoader;
import org.dbflute.properties.DfBasicProperties;
import org.dbflute.properties.DfClassificationProperties;
import org.dbflute.properties.assistant.classification.DfClassificationElement;
import org.dbflute.properties.assistant.classification.DfClassificationGroup;
import org.dbflute.properties.assistant.classification.DfClassificationTop;
import org.dbflute.properties.assistant.classification.element.literal.DfClassificationLiteralArranger;
import org.dbflute.properties.assistant.classification.top.refcls.DfRefClsElement;
import org.dbflute.util.Srl;

/**
 * basically use AppCls, this is the sub function.
 * @author jflute
 */
public class DfAppClsTableLoader implements DfFreeGenTableLoader {

    // ===================================================================================
    //                                                                          Load Table
    //                                                                          ==========
    // ; resourceMap = map:{
    //     ; baseDir = ../src/main
    //     ; resourceType = APP_CLS
    //     ; resourceFile = ../../../dockside_appcls.properties
    // }
    // ; outputMap = map:{
    //     ; templateFile = LaAppCDef.vm
    //     ; outputDirectory = $$baseDir$$/java
    //     ; package = org.dbflute...
    //     ; className = unused
    // }
    // ; optionMap = map:{
    // }
    @Override
    public DfFreeGenMetaData loadTable(String requestName, DfFreeGenResource resource, DfFreeGenMapProp mapProp) {
        final String resourceFile = resource.getResourceFile();
        final Map<String, Object> appClsMap;
        try {
            appClsMap = new DfMapFile().readMap(new FileInputStream(resourceFile));
        } catch (FileNotFoundException e) {
            throw new DfIllegalPropertySettingException("Not found the dfprop file: " + resourceFile, e);
        } catch (IOException e) {
            throw new DfIllegalPropertySettingException("Cannot read the the dfprop file: " + resourceFile, e);
        }
        Map<String, DfClassificationTop> dbClsMap = null; // lazy load because it might be unused
        boolean hasRefCls = false;
        final DfClassificationProperties clsProp = getClassificationProperties();
        final DfClassificationLiteralArranger literalArranger = new DfClassificationLiteralArranger();
        final List<DfClassificationTop> topList = new ArrayList<DfClassificationTop>();
        for (Entry<String, Object> entry : appClsMap.entrySet()) {
            final String classificationName = entry.getKey();
            final DfClassificationTop classificationTop = new DfClassificationTop();
            topList.add(classificationTop);
            classificationTop.setClassificationName(classificationName);
            DfRefClsElement refClsElement = null;
            @SuppressWarnings("unchecked")
            final List<Map<String, Object>> elementMapList = (List<Map<String, Object>>) entry.getValue();
            for (Map<String, Object> elementMap : elementMapList) {
                if (isElementMapClassificationTop(elementMap)) {
                    classificationTop.acceptClassificationTopBasicItemMap(elementMap);

                    // pickup from DfClassificationProperties@processClassificationTopFromLiteralIfNeeds()
                    classificationTop.putGroupingAll(clsProp.getElementMapGroupingMap(elementMap));
                    classificationTop.putDeprecatedAll(clsProp.getElementMapDeprecatedMap(elementMap));
                } else {
                    if (isElementMapRefCls(elementMap)) {
                        assertRefClsOnlyOne(classificationName, refClsElement, elementMap, resource);
                        if (dbClsMap == null) {
                            dbClsMap = clsProp.getClassificationTopMap();
                        }
                        refClsElement = createRefClsElement(classificationName, elementMap, dbClsMap, resource);
                        handleRefCls(classificationTop, refClsElement);
                    } else {
                        literalArranger.arrange(classificationName, elementMap);
                        final DfClassificationElement element = new DfClassificationElement();
                        element.setClassificationName(classificationName);
                        element.acceptBasicItemMap(elementMap);
                        classificationTop.addClassificationElement(element);
                    }
                }
            }
            if (refClsElement != null) {
                refClsElement.verifyRelationshipByRefTypeIfNeeds(classificationTop);
                hasRefCls = true;
            }
        }
        final Map<String, Object> optionMap = mapProp.getOptionMap();
        final String clsTheme = (String) optionMap.getOrDefault("clsTheme", "appcls");
        optionMap.put("clsTheme", clsTheme);
        optionMap.put("classificationTopList", topList);
        optionMap.put("classificationNameList", topList.stream().map(top -> {
            return top.getClassificationName();
        }).collect(Collectors.toList()));
        optionMap.put("hasRefCls", hasRefCls);
        optionMap.put("allcommonPackage", getBasicProperties().getBaseCommonPackage());

        stopRedundantCommentIfNeeds(requestName, resourceFile, topList, optionMap);

        // #for_now can be flexible? (table name is unused?)
        return DfFreeGenMetaData.asOnlyOne(optionMap, clsTheme, Collections.emptyList());
    }

    protected boolean isElementMapClassificationTop(Map<String, Object> elementMap) {
        return elementMap.get(DfClassificationTop.KEY_TOP_COMMENT) != null;
    }

    // ===================================================================================
    //                                                                              refCls
    //                                                                              ======
    protected boolean isElementMapRefCls(Map<String, Object> elementMap) {
        return elementMap.get(DfRefClsElement.KEY_REFCLS) != null;
    }

    protected void assertRefClsOnlyOne(String classificationName, DfRefClsElement refClsElement, Map<String, Object> elementMap,
            DfFreeGenResource resource) {
        if (refClsElement != null) { // only-one refCls is supported #for_now
            final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
            br.addNotice("Duplicate refCls in the app classification.");
            br.addItem("Advice");
            br.addElement("Only-one refCls is supported in one app classification.");
            br.addItem("AppCls");
            br.addElement(classificationName);
            br.addItem("Existing refCls");
            br.addElement(refClsElement);
            br.addItem("Duplicate refCls");
            br.addElement(elementMap);
            br.addItem("dfprop File");
            br.addElement(resource.getResourceFile());
            final String msg = br.buildExceptionMessage();
            throw new DfIllegalPropertySettingException(msg);
        }
    }

    protected DfRefClsElement createRefClsElement(String classificationName, Map<String, Object> elementMap,
            Map<String, DfClassificationTop> dbClsMap, DfFreeGenResource resource) {
        final String refCls = (String) elementMap.get(DfRefClsElement.KEY_REFCLS);
        final String projectName;
        final String refClsName;
        final String groupName;
        if (refCls.contains("@")) { // #hope other schema's reference
            projectName = Srl.substringFirstFront(refCls, "@");
            final String rearName = Srl.substringFirstRear(refCls, "@");
            if (rearName.contains(".")) {
                refClsName = Srl.substringFirstFront(rearName, ".");
                groupName = Srl.substringFirstRear(rearName, ".");
            } else {
                refClsName = rearName;
                groupName = null;
            }
        } else {
            projectName = null;
            if (refCls.contains(".")) {
                refClsName = Srl.substringFirstFront(refCls, ".");
                groupName = Srl.substringFirstRear(refCls, ".");
            } else {
                refClsName = refCls;
                groupName = null;
            }
        }
        final String classificationType = buildClassificationType(refClsName);
        final String refType = (String) elementMap.get(DfRefClsElement.KEY_REFTYPE);
        if (refType == null) {
            String msg = "Not found the refType in refCls elementMap: " + classificationName + " " + elementMap;
            throw new DfIllegalPropertySettingException(msg);
        }
        final DfClassificationTop dbClsTop = findDBCls(classificationName, refClsName, dbClsMap, resource);
        return new DfRefClsElement(projectName, refClsName, classificationType, groupName, refType, dbClsTop);
    }

    protected String buildClassificationType(String refClsName) {
        return getBasicProperties().getCDefPureName() + "." + refClsName;
    }

    protected DfClassificationTop findDBCls(String classificationName, String refClsName, Map<String, DfClassificationTop> dbClsMap,
            DfFreeGenResource resource) {
        final DfClassificationTop refTop = dbClsMap.get(refClsName);
        if (refTop == null) {
            throwAppClsReferredDBClsNotFoundException(classificationName, refClsName, dbClsMap, resource);
        }
        return refTop;
    }

    protected void throwAppClsReferredDBClsNotFoundException(String classificationName, String refClsName,
            Map<String, DfClassificationTop> dbClsMap, DfFreeGenResource resource) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the DB classification for app classification.");
        br.addItem("Advice");
        br.addElement("Make sure your DB classification name.");
        br.addItem("AppCls");
        br.addElement(classificationName);
        br.addItem("NotFound DBCls");
        br.addElement(refClsName);
        br.addItem("Existing DBCls");
        br.addElement(dbClsMap.keySet());
        br.addItem("dfprop File");
        br.addElement(resource.getResourceFile());
        final String msg = br.buildExceptionMessage();
        throw new DfIllegalPropertySettingException(msg);
    }

    protected void handleRefCls(DfClassificationTop classificationTop, DfRefClsElement refClsElement) {
        refClsElement.verifyFormalRefType(classificationTop);
        classificationTop.addRefClsElement(refClsElement);
        if (refClsElement.isRefTypeIncluded()) {
            final DfClassificationTop dbClsTop = refClsElement.getDBClsTop();
            final String groupName = refClsElement.getGroupName();
            if (groupName != null) {
                final DfClassificationGroup group = dbClsTop.getGroupList().stream().filter(gr -> {
                    return gr.getGroupName().equals(groupName);
                }).findFirst().orElseThrow(() -> {
                    String msg = "Not found the group name: " + refClsElement.getClassificationName() + "." + groupName;
                    return new DfIllegalPropertySettingException(msg);
                });
                classificationTop.addClassificationElementAll(group.getElementList());
            } else {
                final List<DfClassificationElement> dbElementList = dbClsTop.getClassificationElementList();
                classificationTop.addClassificationElementAll(dbElementList);
                classificationTop.acceptGroupList(dbClsTop.getGroupList());
            }
        }
    }

    // ===================================================================================
    //                                                                   Redundant Comment
    //                                                                   =================
    protected void stopRedundantCommentIfNeeds(String requestName, String resourceFile, List<DfClassificationTop> topList,
            Map<String, Object> optionMap) {
        if (isSuppressRedundantCommentStop(optionMap)) {
            return;
        }
        for (DfClassificationTop top : topList) {
            final List<DfClassificationElement> elementList = top.getClassificationElementList();
            for (DfClassificationElement element : elementList) {
                if (element.hasComment()) {
                    final String comment = element.getComment();
                    if (Srl.equalsPlain(comment, element.getCode(), element.getName(), element.getAlias())) {
                        throwAppClsRedundantCommentException(requestName, resourceFile, top, element, comment);
                    }
                }
            }
        }
    }

    protected void throwAppClsRedundantCommentException(String requestName, String resourceFile, DfClassificationTop top,
            DfClassificationElement element, String comment) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Found the redundant comment in the app classification.");
        br.addItem("Advice");
        br.addElement("Comment should not be same as code, name, alias.");
        br.addElement("Write description for the element or remove comment definition.");
        br.addElement("(Many redundant comments are noise to watch valueable comments)");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    code=ONE ; name=OneMan ; alias=Showbase ; comment=ONE // *Bad");
        br.addElement("    code=ONE ; name=OneMan ; alias=Showbase ; comment=OneMan // *Bad");
        br.addElement("    code=ONE ; name=OneMan ; alias=Showbase ; comment=Showbase // *Bad");
        br.addElement("  (o):");
        br.addElement("    code=ONE ; ... ; comment=beautiful show at the showbase // Good");
        br.addElement("    code=ONE ; name=OneMan ; alias=ShowBase // Good");
        br.addElement("");
        br.addElement("However if you need to suppress this stop, set the property as followings.");
        br.addElement("When lastaFluteMap.dfprop:");
        br.addElement(" e.g. appName is sea, freeGen title is appcls");
        br.addElement("  ; overrideMap = map:{");
        br.addElement("      ; sea.freeGen.appcls.isSuppressRedundantCommentStop = true");
        br.addElement("  }");
        br.addElement(" e.g. appName is hangar, freeGen title is land_cls (namedcls)");
        br.addElement("  ; overrideMap = map:{");
        br.addElement("      ; hangar.freeGen.land_cls.isSuppressRedundantCommentStop = true");
        br.addElement("  }");
        br.addElement("When freeGenMap.dfprop:");
        br.addElement("  ; optionMap = map:{");
        br.addElement("      ; isSuppressRedundantCommentStop = true");
        br.addElement("  }");
        br.addItem("FreeGen Request");
        br.addElement(requestName);
        br.addItem("Resource File");
        br.addElement(resourceFile);
        br.addItem("Classification");
        br.addElement(top.toString());
        br.addItem("Element");
        br.addElement(element.toString());
        br.addItem("Redundant Comment");
        br.addElement(comment);
        final String msg = br.buildExceptionMessage();
        throw new DfIllegalPropertySettingException(msg);
    }

    protected boolean isSuppressRedundantCommentStop(final Map<String, Object> optionMap) {
        final Object stop = optionMap.get("isSuppressRedundantCommentStop");
        return stop != null && "true".equalsIgnoreCase(stop.toString());
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfBasicProperties getBasicProperties() {
        return DfBuildProperties.getInstance().getBasicProperties();
    }

    protected DfClassificationProperties getClassificationProperties() {
        return DfBuildProperties.getInstance().getClassificationProperties();
    }
}
