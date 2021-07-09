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
import java.util.LinkedHashMap;
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
import org.dbflute.logic.manage.freegen.table.appcls.refcls.DfRefClsReferenceRegistry;
import org.dbflute.properties.DfBasicProperties;
import org.dbflute.properties.DfClassificationProperties;
import org.dbflute.properties.assistant.classification.DfClassificationElement;
import org.dbflute.properties.assistant.classification.DfClassificationGroup;
import org.dbflute.properties.assistant.classification.DfClassificationTop;
import org.dbflute.properties.assistant.classification.element.proploading.DfClsElementLiteralArranger;
import org.dbflute.properties.assistant.classification.refcls.DfRefClsElement;
import org.dbflute.properties.assistant.classification.refcls.DfRefClsReference;
import org.dbflute.properties.assistant.classification.refcls.DfRefClsReferredCDef;
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
                        literalArranger.arrange(classificationName, elementMap);
                        final DfClassificationElement element = new DfClassificationElement();
                        element.setClassificationName(classificationName);
                        element.acceptBasicItemMap(elementMap);
                        classificationTop.addClassificationElement(element);
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
        final String clsTheme = (String) optionMap.getOrDefault("clsTheme", "appcls"); // basically exists
        setupOptionMap(optionMap, topList, hasRefCls, clsTheme);

        stopRedundantCommentIfNeeds(requestName, resourceFile, topList, optionMap); // @since 1.2.5
        registerRefClsReference(requestName, clsTheme, topList, optionMap); // for next appcls's refCls @since 1.2.5
        return DfFreeGenMetaData.asOnlyOne(optionMap, clsTheme, Collections.emptyList()); // #for_now can be flexible? (table name is unused?)
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

    protected DfRefClsElement createRefClsElement(String classificationName, Map<String, Object> elementMap, DfFreeGenResource resource) {
        // e.g. map:{ refCls=maihamadb@MemberStatus ; refType=included }
        final String refAttr = (String) elementMap.get(DfRefClsElement.KEY_REFCLS);
        final String refClsTheme; // of e.g. DB or "appcls" or namedcls's name
        final String refClsName; // classification name
        final String groupName;
        if (refAttr.contains("@")) { // #hope other schema's reference
            refClsTheme = Srl.substringFirstFront(refAttr, "@");
            final String rearName = Srl.substringFirstRear(refAttr, "@");
            if (rearName.contains(".")) {
                refClsName = Srl.substringFirstFront(rearName, ".");
                groupName = Srl.substringFirstRear(rearName, ".");
            } else {
                refClsName = rearName;
                groupName = null;
            }
        } else {
            refClsTheme = getBasicProperties().getProjectName(); // current DB as default
            if (refAttr.contains(".")) {
                refClsName = Srl.substringFirstFront(refAttr, ".");
                groupName = Srl.substringFirstRear(refAttr, ".");
            } else {
                refClsName = refAttr;
                groupName = null;
            }
        }
        final String refType = extractRefType(classificationName, elementMap);
        final DfClassificationTop referredClsTop = findReferredClsTop(classificationName, refAttr, refClsTheme, refClsName, resource);
        final DfRefClsReferredCDef referredCDef = findReferredCDef(classificationName, refAttr, refClsTheme, refClsName, resource);
        final String resourceFile = resource.getResourceFile();
        return new DfRefClsElement(refClsTheme, refClsName, groupName, refType, referredClsTop, referredCDef, resourceFile);
    }

    protected String extractRefType(String classificationName, Map<String, Object> elementMap) {
        final String refType = (String) elementMap.get(DfRefClsElement.KEY_REFTYPE);
        if (refType == null) {
            String msg = "Not found the refType in refCls elementMap: " + classificationName + " " + elementMap;
            throw new DfIllegalPropertySettingException(msg);
        }
        return refType;
    }

    // -----------------------------------------------------
    //                                        Find Reference
    //                                        --------------
    protected DfClassificationTop findReferredClsTop(String classificationName, String refAttr, String refClsTheme, String refClsName,
            DfFreeGenResource resource) {
        final DfRefClsReference reference = findReference(classificationName, refAttr, refClsTheme, refClsName, resource);
        final Map<String, DfClassificationTop> clsTopMap = reference.getReferredClsTopMap();
        final DfClassificationTop referredClsTop = clsTopMap.get(refClsName);
        if (referredClsTop == null) {
            throwAppClsReferredClsNotFoundException(classificationName, refAttr, refClsTheme, refClsName, clsTopMap, resource);
        }
        return referredClsTop;
    }

    protected DfRefClsReferredCDef findReferredCDef(String classificationName, String refAttr, String refClsTheme, String refClsName,
            DfFreeGenResource resource) {
        final DfRefClsReference reference = findReference(classificationName, refAttr, refClsTheme, refClsName, resource);
        return reference.getReferredCDef();
    }

    protected DfRefClsReference findReference(String classificationName, String refAttr, String refClsTheme, String refClsName,
            DfFreeGenResource resource) {
        final DfRefClsReference reference = DfRefClsReferenceRegistry.getInstance().findReference(refClsTheme);
        if (reference == null) {
            throwAppClsReferredClsThemeNotFoundException(classificationName, refAttr, refClsTheme, refClsName, resource);
        }
        return reference;
    }

    protected void throwAppClsReferredClsThemeNotFoundException(String classificationName, String refAttr, String refClsTheme,
            String refClsName, DfFreeGenResource resource) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the referred cls-theme from the app classification.");
        br.addItem("Advice");
        br.addElement("Make sure your cls-theme value of refCls attribute.");
        br.addElement("And refCls attribute depends on loading order.");
        br.addElement("For example, appcls can refers namedcls, but reverse cannot.");
        br.addElement("  (o):");
        br.addElement("    appcls to namedcls");
        br.addElement("    appcls to DB cls");
        br.addElement("    namedcls to DB cls");
        br.addElement("  (?):");
        br.addElement("    appcls to appcls (depending on FreeGen definition order)");
        br.addElement("");
        br.addElement("In case of reference to DB, if dbflute_maihamadb:");
        br.addElement("  refType=maihamadb@MemberStatus");
        br.addElement("");
        br.addElement("In case of reference to namedcls, if hangar_leonardo_cls.dfprop:");
        br.addElement("  refType=leonardo_cls@MemberStatus");
        br.addItem("AppCls");
        br.addElement(classificationName);
        br.addItem("RefCls Attribute");
        br.addElement(refAttr);
        br.addItem("NotFound Theme");
        br.addElement(refClsTheme);
        br.addItem("Existing Theme");
        br.addElement(DfRefClsReferenceRegistry.getInstance().getThemeClsTopMap().keySet());
        br.addItem("dfprop File");
        br.addElement(resource.getResourceFile());
        final String msg = br.buildExceptionMessage();
        throw new DfIllegalPropertySettingException(msg);
    }

    protected void throwAppClsReferredClsNotFoundException(String classificationName, String refAttr, String refClsTheme, String refClsName,
            Map<String, DfClassificationTop> clsTopMap, DfFreeGenResource resource) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the referred classification from the app classification.");
        br.addItem("Advice");
        br.addElement("Make sure your classification name of refCls attribute.");
        br.addItem("AppCls");
        br.addElement(classificationName);
        br.addItem("RefCls Attribute");
        br.addElement(refAttr);
        br.addItem("RefCls Theme");
        br.addElement(refClsTheme);
        br.addItem("NotFound Cls");
        br.addElement(refClsName);
        br.addItem("Existing Cls");
        br.addElement(clsTopMap.keySet());
        br.addItem("dfprop File");
        br.addElement(resource.getResourceFile());
        final String msg = br.buildExceptionMessage();
        throw new DfIllegalPropertySettingException(msg);
    }

    // -----------------------------------------------------
    //                                        Include refCls
    //                                        --------------
    protected void includeRefClsElement(DfClassificationTop classificationTop, DfRefClsElement refClsElement) {
        final DfClassificationTop refClsTop = refClsElement.getReferredClsTop();
        if (refClsElement.isRefTypeIncluded()) {
            final String groupName = refClsElement.getGroupName();
            final List<DfClassificationElement> dbElementList;
            if (groupName != null) {
                // e.g. map:{ refCls=maihamadb@MemberStatus.serviceAvailable ; refType=included }
                dbElementList = findRefGroup(refClsElement, refClsTop, groupName).getElementList();
            } else {
                // e.g. map:{ refCls=maihamadb@MemberStatus ; refType=included }
                dbElementList = refClsTop.getClassificationElementList();
            }
            classificationTop.addClassificationElementAll(copyElementList(classificationTop, dbElementList));
        }
        // later, literal elements are not evaluated yet here
        //classificationTop.inheritRefClsGroup(dbClsTop);
    }

    protected DfClassificationGroup findRefGroup(DfRefClsElement refClsElement, DfClassificationTop refClsTop, String groupName) {
        return refClsTop.getGroupList().stream().filter(gr -> {
            return gr.getGroupName().equals(groupName);
        }).findFirst().orElseThrow(() -> {
            String msg = "Not found the group name: " + refClsElement.getClassificationName() + "." + groupName;
            return new DfIllegalPropertySettingException(msg);
        });
    }

    protected List<DfClassificationElement> copyElementList(DfClassificationTop classificationTop,
            List<DfClassificationElement> dbElementList) {
        return dbElementList.stream().map(el -> el.copyElement(classificationTop)).collect(Collectors.toList());
    }

    // -----------------------------------------------------
    //                                  Inherit refCls Group
    //                                  --------------------
    protected void inheritRefClsGroup(DfClassificationTop classificationTop, DfRefClsElement refClsElement) {
        // all types are inheritable, considering short grouping elements @since 1.2.5
        final DfClassificationTop refClsTop = refClsElement.getReferredClsTop();
        classificationTop.inheritRefClsGroup(refClsTop);
    }

    // -----------------------------------------------------
    //                                         Referred CDef
    //                                         -------------
    protected List<String> prepareReferredCDefFqcnList(List<DfClassificationTop> topList) {
        final Map<String, String> refThemeCDefFqcnMap = new LinkedHashMap<String, String>();
        for (DfClassificationTop top : topList) {
            final List<DfRefClsElement> refClsElementList = top.getRefClsElementList();
            for (DfRefClsElement refClsElement : refClsElementList) {
                final String refClsTheme = refClsElement.getRefClsTheme();
                final String existingImportExp = refThemeCDefFqcnMap.get(refClsTheme);
                if (existingImportExp != null) {
                    break;
                }
                final String referredCDefPackage = refClsElement.getReferredCDefPackage();
                final String referredCDefClassName = refClsElement.getReferredCDefClassName();
                final String refThemeCDefFqcn = referredCDefPackage + "." + referredCDefClassName;
                refThemeCDefFqcnMap.put(refClsTheme, refThemeCDefFqcn);
            }
        }
        return new ArrayList<>(refThemeCDefFqcnMap.values());
    }

    // -----------------------------------------------------
    //                                    Register Reference
    //                                    ------------------
    protected void registerRefClsReference(String requestName, String clsTheme, List<DfClassificationTop> topList,
            Map<String, Object> optionMap) {
        if (!isOptionLastaFlute(optionMap)) {
            return; // only LastaFlute classifications can be referred (to be simple logic)
        }
        if (isOptionLastaDoc(optionMap)) {
            return; // LastaDoc uses real loader's registration (to avoid unexpected data use)
        }
        final Map<String, DfClassificationTop> clsTopMap =
                topList.stream().collect(Collectors.toMap(top -> top.getClassificationName(), top -> top));

        final String cdefPackage = (String) optionMap.get("cdefPackage"); // not null
        final String cdefClassName = (String) optionMap.get("cdefClassName"); // not null
        if (cdefPackage == null || cdefClassName == null) { // just in case
            throwRefClsReferenceCDefInfoNotFoundException(requestName, clsTheme, optionMap, cdefPackage, cdefClassName);
        }
        final DfRefClsReferredCDef referredCDef = new DfRefClsReferredCDef(cdefPackage, cdefClassName);
        DfRefClsReferenceRegistry.getInstance().registerReference(clsTheme, clsTopMap, referredCDef);
    }

    protected boolean isOptionLastaFlute(Map<String, Object> optionMap) {
        final Boolean isLastaFlute = (Boolean) optionMap.get("isLastaFlute");
        return isLastaFlute != null && isLastaFlute;
    }

    protected boolean isOptionLastaDoc(Map<String, Object> optionMap) {
        final Boolean isLastaFlute = (Boolean) optionMap.get("isLastaDoc");
        return isLastaFlute != null && isLastaFlute;
    }

    protected void throwRefClsReferenceCDefInfoNotFoundException(String requestName, String clsTheme, Map<String, Object> optionMap,
            String cdefPackage, String cdefClassName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Both cdefPackage and cdefClassName are required here.");
        br.addItem("FreeGen Request");
        br.addElement(requestName);
        br.addItem("clsTheme");
        br.addElement(clsTheme);
        br.addItem("cdefPackage");
        br.addElement(cdefPackage);
        br.addItem("cdefClassName");
        br.addElement(cdefClassName);
        br.addItem("optionMap");
        optionMap.forEach((key, value) -> {
            br.addElement(key + " = " + value);
        });
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg);
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
