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
import org.dbflute.logic.manage.freegen.table.appcls.refcls.DfRefClsLoadingHandler;
import org.dbflute.properties.DfBasicProperties;
import org.dbflute.properties.DfClassificationProperties;
import org.dbflute.properties.assistant.classification.DfClassificationElement;
import org.dbflute.properties.assistant.classification.DfClassificationTop;
import org.dbflute.properties.assistant.classification.element.proploading.DfClsElementLiteralArranger;
import org.dbflute.properties.assistant.classification.refcls.DfRefClsElement;
import org.dbflute.util.Srl;

/**
 * basically use AppCls, this is the sub function.
 * @author jflute
 */
public class DfAppClsTableLoader implements DfFreeGenTableLoader {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfRefClsLoadingHandler _refClsLoadingHandler = new DfRefClsLoadingHandler();

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
        final Map<String, Object> appClsMap = readAppClsMap(resourceFile);
        boolean hasRefCls = false;
        final DfClassificationProperties clsProp = getClassificationProperties();
        final DfClsElementLiteralArranger literalArranger = new DfClsElementLiteralArranger();
        final List<DfClassificationTop> topList = new ArrayList<DfClassificationTop>();
        for (Entry<String, Object> entry : appClsMap.entrySet()) {
            final String classificationName = entry.getKey();
            final DfClassificationTop classificationTop = new DfClassificationTop(classificationName);
            topList.add(classificationTop);
            DfRefClsElement refClsElement = null;
            @SuppressWarnings("unchecked")
            final List<Map<String, Object>> elementMapList = (List<Map<String, Object>>) entry.getValue();
            for (Map<String, Object> elementMap : elementMapList) {
                if (isElementMapTop(elementMap)) {
                    // e.g. map:{ topComment=... ; codeType=String }
                    classificationTop.acceptBasicItem(elementMap);
                    classificationTop.putGroupingAll(clsProp.getElementMapGroupingMap(elementMap));
                    classificationTop.putDeprecatedAll(clsProp.getElementMapDeprecatedMap(elementMap));
                } else {
                    if (isElementMapRefCls(elementMap)) {
                        // e.g. map:{ refCls=maihamadb@MemberStatus ; refType=included }
                        assertRefClsOnlyOne(classificationName, refClsElement, elementMap, resource);
                        refClsElement = createRefClsElement(classificationName, elementMap, resource);
                        refClsElement.verifyFormalRefType(classificationTop);
                        classificationTop.addRefClsElement(refClsElement);
                        includeRefClsElement(classificationTop, refClsElement);
                    } else {
                        // e.g. map:{ code=FML ; name=OneMan ; alias=ShowBase ; comment=Formalized }
                        literalArranger.arrangeWithoutDefault(classificationName, elementMap);
                        final boolean resolved = resolveIncludedOverridingIfExists(classificationTop, refClsElement, elementMap, resource);
                        if (!resolved) { // can be treated as normal element
                            final DfClassificationElement element = new DfClassificationElement();
                            element.setClassificationName(classificationName);
                            element.acceptBasicItemMap(elementMap);
                            classificationTop.addClassificationElement(element);
                        }
                    }
                }
            }
            if (refClsElement != null) {
                inheritRefClsGroup(classificationTop, refClsElement); // should be after all literal evaluation
                refClsElement.verifyRelationshipByRefTypeIfNeeds(classificationTop);
                hasRefCls = true;
            }
        }
        final Map<String, Object> optionMap = mapProp.getOptionMap();
        final String clsTheme = (String) optionMap.getOrDefault("clsTheme", getDefaultClsTheme()); // basically exists
        setupOptionMap(optionMap, topList, hasRefCls, clsTheme);

        stopRedundantCommentIfNeeds(requestName, resourceFile, topList, optionMap); // @since 1.2.5
        registerRefClsReference(requestName, clsTheme, topList, optionMap); // for next appcls's refCls @since 1.2.5
        return DfFreeGenMetaData.asOnlyOne(optionMap, clsTheme, Collections.emptyList()); // #for_now can be flexible? (table name is unused?)
    }

    protected String getDefaultClsTheme() { // you can override for e.g. webcls in LastaDoc (2021/07/10)
        return "appcls"; // used in LastaDoc (which does not have clsTheme in optionMap)
    }

    // -----------------------------------------------------
    //                                              Read Map
    //                                              --------
    protected Map<String, Object> readAppClsMap(String resourceFile) {
        final Map<String, Object> appClsMap;
        try {
            appClsMap = new DfMapFile().readMap(new FileInputStream(resourceFile));
        } catch (FileNotFoundException e) {
            throw new DfIllegalPropertySettingException("Not found the dfprop file: " + resourceFile, e);
        } catch (IOException e) {
            throw new DfIllegalPropertySettingException("Cannot read the the dfprop file: " + resourceFile, e);
        }
        return appClsMap;
    }

    // -----------------------------------------------------
    //                                      Element Map Type
    //                                      ----------------
    protected boolean isElementMapTop(Map<String, Object> elementMap) {
        return elementMap.get(DfClassificationTop.KEY_TOP_COMMENT) != null;
    }

    protected boolean isElementMapRefCls(Map<String, Object> elementMap) {
        return elementMap.get(DfRefClsElement.KEY_REFCLS) != null;
    }

    // -----------------------------------------------------
    //                                            Option Map
    //                                            ----------
    protected void setupOptionMap(Map<String, Object> optionMap, List<DfClassificationTop> topList, boolean hasRefCls, String clsTheme) {
        optionMap.put("clsTheme", clsTheme); // unused for now (2021/07/07)
        optionMap.put("classificationTopList", topList);
        optionMap.put("classificationNameList", topList.stream().map(top -> top.getClassificationName()).collect(Collectors.toList()));
        optionMap.put("hasRefCls", hasRefCls);
        optionMap.put("referredCDefFqcnList", prepareReferredCDefFqcnList(topList)); // @since 1.2.5

        // already unused @since 1.2.5 but compatible for plain freegen just in case
        optionMap.put("allcommonPackage", getBasicProperties().getBaseCommonPackage());
    }

    // ===================================================================================
    //                                                                              refCls
    //                                                                              ======
    // -----------------------------------------------------
    //                                        Prepare refCls
    //                                        --------------
    protected void assertRefClsOnlyOne(String classificationName, DfRefClsElement refClsElement, Map<String, Object> elementMap,
            DfFreeGenResource resource) {
        _refClsLoadingHandler.assertRefClsOnlyOne(classificationName, refClsElement, elementMap, resource);
    }

    protected DfRefClsElement createRefClsElement(String classificationName, Map<String, Object> elementMap, DfFreeGenResource resource) {
        return _refClsLoadingHandler.createRefClsElement(classificationName, elementMap, resource);
    }

    // -----------------------------------------------------
    //                                        Include refCls
    //                                        --------------
    protected void includeRefClsElement(DfClassificationTop classificationTop, DfRefClsElement refClsElement) {
        _refClsLoadingHandler.includeRefClsElement(classificationTop, refClsElement);
    }

    // -----------------------------------------------------
    //                           Resolve Included Overriding
    //                           ---------------------------
    protected boolean resolveIncludedOverridingIfExists(DfClassificationTop classificationTop, DfRefClsElement refClsElement,
            Map<String, Object> elementMap, DfFreeGenResource resource) {
        return _refClsLoadingHandler.resolveIncludedOverridingIfExists(classificationTop, refClsElement, elementMap, resource);
    }

    // -----------------------------------------------------
    //                                  Inherit refCls Group
    //                                  --------------------
    protected void inheritRefClsGroup(DfClassificationTop classificationTop, DfRefClsElement refClsElement) {
        _refClsLoadingHandler.inheritRefClsGroup(classificationTop, refClsElement);
    }

    // -----------------------------------------------------
    //                                         Referred CDef
    //                                         -------------
    protected List<String> prepareReferredCDefFqcnList(List<DfClassificationTop> topList) {
        return _refClsLoadingHandler.prepareReferredCDefFqcnList(topList);
    }

    // -----------------------------------------------------
    //                                    Register Reference
    //                                    ------------------
    protected void registerRefClsReference(String requestName, String clsTheme, List<DfClassificationTop> topList,
            Map<String, Object> optionMap) {
        _refClsLoadingHandler.registerRefClsReference(requestName, clsTheme, topList, optionMap);
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
