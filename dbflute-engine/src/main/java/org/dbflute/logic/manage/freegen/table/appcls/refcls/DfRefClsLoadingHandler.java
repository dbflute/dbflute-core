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
package org.dbflute.logic.manage.freegen.table.appcls.refcls;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.dbflute.DfBuildProperties;
import org.dbflute.exception.DfIllegalPropertySettingException;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.logic.manage.freegen.DfFreeGenResource;
import org.dbflute.properties.DfBasicProperties;
import org.dbflute.properties.assistant.classification.DfClassificationElement;
import org.dbflute.properties.assistant.classification.DfClassificationGroup;
import org.dbflute.properties.assistant.classification.DfClassificationTop;
import org.dbflute.properties.assistant.classification.refcls.DfRefClsElement;
import org.dbflute.properties.assistant.classification.refcls.DfRefClsReferredCDef;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 1.2.5 split from DfAppClsTableLoader (2021/07/11 Sunday at roppongi japanese)
 */
public class DfRefClsLoadingHandler {

    // ===================================================================================
    //                                                                      Prepare refCls
    //                                                                      ==============
    public void assertRefClsOnlyOne(String classificationName, DfRefClsElement refClsElement, Map<String, Object> elementMap,
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

    public DfRefClsElement createRefClsElement(String classificationName, Map<String, Object> elementMap, DfFreeGenResource resource) {
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

    // ===================================================================================
    //                                                                      Find Reference
    //                                                                      ==============
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

    // ===================================================================================
    //                                                                      Include refCls
    //                                                                      ==============
    public void includeRefClsElement(DfClassificationTop classificationTop, DfRefClsElement refClsElement) {
        final DfClassificationTop refClsTop = refClsElement.getReferredClsTop();
        if (refClsElement.isRefTypeIncluded()) {
            final String groupName = refClsElement.getGroupName();
            final List<DfClassificationElement> refElementList;
            if (groupName != null) {
                // e.g. map:{ refCls=maihamadb@MemberStatus.serviceAvailable ; refType=included }
                refElementList = findRefGroup(refClsElement, refClsTop, groupName).getElementList();
            } else {
                // e.g. map:{ refCls=maihamadb@MemberStatus ; refType=included }
                refElementList = refClsTop.getClassificationElementList();
            }
            classificationTop.addClassificationElementAll(copyIncludedElementList(classificationTop, refElementList));
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

    protected List<DfClassificationElement> copyIncludedElementList(DfClassificationTop classificationTop,
            List<DfClassificationElement> refElementList) {
        return refElementList.stream().map(el -> el.copyElement(classificationTop).asRefClsIncluded()).collect(Collectors.toList());
    }

    // ===================================================================================
    //                                                         Resolve Included Overriding
    //                                                         ===========================
    public boolean resolveIncludedOverridingIfExists(DfClassificationTop classificationTop, DfRefClsElement refClsElement,
            Map<String, Object> elementMap, DfFreeGenResource resource) {
        if (refClsElement == null) {
            return false;
        }
        final String literalCode = (String) elementMap.get(DfClassificationElement.KEY_CODE); // not null (already checked)
        final Optional<DfClassificationElement> optSameCodeElement = classificationTop.getClassificationElementList()
                .stream()
                .filter(el -> el.isRefClsIncluded())
                .filter(el -> el.getCode().equals(literalCode))
                .findFirst();
        optSameCodeElement.ifPresent(sameCodeElement -> {
            final Object overrideProp = elementMap.get("override");
            if (overrideProp != null && "true".equalsIgnoreCase(overrideProp.toString())) {
                sameCodeElement.overrideBasicItemMap(elementMap);
            } else {
                throwAppClsRefClsIncludedCodeConflictException(classificationTop, refClsElement, literalCode, resource);
            }
        });
        return optSameCodeElement.isPresent();
    }

    protected void throwAppClsRefClsIncludedCodeConflictException(DfClassificationTop classificationTop, DfRefClsElement refClsElement,
            String conflictedCode, DfFreeGenResource resource) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The literal code is conflicted with included code by refCls.");
        br.addItem("Advice");
        br.addElement("Make sure your classification code of literal and included.");
        br.addElement("Basically you cannot define literal code same as included code.");
        br.addElement("");
        br.addElement("While, if you need to override included element by literal element,");
        br.addElement("add 'override' attribute to the literal element definition.");
        br.addElement("For example:");
        br.addElement("  (o): name attribute is overridden");
        br.addElement("    map:{ code=FML ; override=true ; name=OneMan }");
        br.addElement("");
        br.addElement("  (o): both name and alias attributes are overridden");
        br.addElement("    map:{ code=FML ; override=true ; name=OneMan ; alias=Showbase }");
        br.addItem("AppCls");
        br.addElement(classificationTop.getClassificationName());
        br.addItem("RefCls");
        br.addElement(refClsElement);
        br.addItem("Conflicted Code");
        br.addElement(conflictedCode);
        br.addItem("dfprop File");
        br.addElement(resource.getResourceFile());
        final String msg = br.buildExceptionMessage();
        throw new DfIllegalPropertySettingException(msg);
    }

    // ===================================================================================
    //                                                                Inherit refCls Group
    //                                                                ====================
    public void inheritRefClsGroup(DfClassificationTop classificationTop, DfRefClsElement refClsElement) {
        // all types are inheritable, considering short grouping elements @since 1.2.5
        final DfClassificationTop refClsTop = refClsElement.getReferredClsTop();
        classificationTop.inheritRefClsGroup(refClsTop);
    }

    // ===================================================================================
    //                                                                       Referred CDef
    //                                                                       =============
    public List<String> prepareImportedReferredCDefFqcnList(List<DfClassificationTop> topList, Map<String, Object> optionMap) {
        final String currentCDefPackage = extractCDefPackage(optionMap); // null allowed, as best effort way
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
                if (currentCDefPackage != null && currentCDefPackage.equals(referredCDefPackage)) { // same package CDef
                    continue; // same-package import statement is unneeded (to avoid Eclipse warning) e.g. namedcls to namedcls
                }
                final String referredCDefClassName = refClsElement.getReferredCDefClassName();
                final String refThemeCDefFqcn = referredCDefPackage + "." + referredCDefClassName;
                refThemeCDefFqcnMap.put(refClsTheme, refThemeCDefFqcn);
            }
        }
        return new ArrayList<>(refThemeCDefFqcnMap.values());
    }

    // ===================================================================================
    //                                                                  Register Reference
    //                                                                  ==================
    public void registerRefClsReference(String requestName, String clsTheme, List<DfClassificationTop> topList,
            Map<String, Object> optionMap) {
        if (!isOptionLastaFlute(optionMap)) {
            return; // only LastaFlute classifications can be referred (to be simple logic)
        }
        if (isOptionLastaDoc(optionMap)) {
            return; // LastaDoc uses real loader's registration (to avoid unexpected data use)
        }
        final Map<String, DfClassificationTop> clsTopMap =
                topList.stream().collect(Collectors.toMap(top -> top.getClassificationName(), top -> top));

        final String cdefPackage = extractCDefPackage(optionMap); // not null here, already checked here
        final String cdefClassName = extractCDefClassName(optionMap); // me too
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
    //                                                                        Assist Logic
    //                                                                        ============
    protected String extractCDefClassName(Map<String, Object> optionMap) {
        return (String) optionMap.get("cdefClassName"); // not null if LastaFlute embedded FreeGen
    }

    protected String extractCDefPackage(Map<String, Object> optionMap) {
        return (String) optionMap.get("cdefPackage"); // not null if LastaFlute embedded FreeGen
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfBasicProperties getBasicProperties() {
        return DfBuildProperties.getInstance().getBasicProperties();
    }
}
