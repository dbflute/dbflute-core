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
package org.seasar.dbflute.logic.doc.prophtml;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.exception.DfIllegalPropertySettingException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.helper.jprop.JavaPropertiesProperty;
import org.seasar.dbflute.helper.jprop.JavaPropertiesReader;
import org.seasar.dbflute.helper.jprop.JavaPropertiesResult;
import org.seasar.dbflute.helper.jprop.JavaPropertiesStreamProvider;
import org.seasar.dbflute.properties.DfBasicProperties;
import org.seasar.dbflute.properties.DfDocumentProperties;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 * @since 1.0.1 (2012/12/21 Friday)
 */
public class DfPropHtmlManager {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Log _log = LogFactory.getLog(DfPropHtmlManager.class);

    /** The standard environment type. */
    private static final String ENV_TYPE_DEFAULT = "-";

    /** The root for language type. */
    private static final String LANG_TYPE_DEFAULT = "-";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The map of request. map:{requestName = request} (NotNull) */
    protected final Map<String, DfPropHtmlRequest> _requestMap = DfCollectionUtil.newLinkedHashMap();

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfPropHtmlManager() {
    }

    // ===================================================================================
    //                                                                        Load Request
    //                                                                        ============
    public void loadRequest() {
        final DfDocumentProperties prop = getDocumentProperties();
        final Map<String, Map<String, Object>> propertiesHtmlMap = prop.getPropertiesHtmlMap();
        if (propertiesHtmlMap.isEmpty()) {
            return;
        }
        for (Entry<String, Map<String, Object>> entry : propertiesHtmlMap.entrySet()) {
            final String requestName = entry.getKey();
            _log.info("[" + requestName + "]");
            final Map<String, Object> requestMap = entry.getValue();
            final DfPropHtmlRequest request = prepareRequest(requestMap, requestName);
            _requestMap.put(requestName, request);
        }
        analyzePropertiesDiff();
    }

    protected DfPropHtmlRequest prepareRequest(Map<String, Object> requestMap, String requestName) {
        final DfPropHtmlRequest request = createPropHtmlRequest(requestMap, requestName);
        final DfDocumentProperties prop = getDocumentProperties();
        final String rootFile = prop.getPropertiesHtmlRootFile(requestMap);
        final Map<String, DfPropHtmlFileAttribute> defaultEnvMap = setupDefaultEnvProperty(request, rootFile);
        assertPropHtmlRootFileExists(defaultEnvMap, requestName, rootFile);
        final Map<String, String> environmentMap = prop.getPropertiesHtmlEnvironmentMap(requestMap);
        final String standardPureFileName = Srl.substringLastRear(rootFile, "/");
        for (Entry<String, String> envEntry : environmentMap.entrySet()) {
            final String envType = envEntry.getKey();
            final String envDir = envEntry.getValue();
            final String envFile = envDir + "/" + standardPureFileName;
            setupEnvironmentProperty(request, envFile, envType, defaultEnvMap);
        }
        return request;
    }

    protected DfPropHtmlRequest createPropHtmlRequest(Map<String, Object> requestMap, String requestName) {
        final DfDocumentProperties prop = getDocumentProperties();
        final List<String> diffIgnored = prop.getPropertiesHtmlDiffIgnoredKeyList(requestMap);
        final List<String> masked = prop.getPropertiesHtmlMaskedKeyList(requestMap);
        final boolean envOnly = prop.isPropertiesHtmlEnvOnlyFloatLeft(requestMap);
        final String extendsProp = prop.getPropertiesHtmlExtendsPropRequest(requestMap);
        final boolean checkImpOver = prop.isPropertiesHtmlCheckImplicitOverride(requestMap);
        return new DfPropHtmlRequest(requestName, diffIgnored, masked, envOnly, extendsProp, checkImpOver);
    }

    protected void assertPropHtmlRootFileExists(Map<String, DfPropHtmlFileAttribute> defaultEnvMap, String requestName,
            String rootFile) {
        if (!defaultEnvMap.isEmpty()) {
            return;
        }
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the root file for PropertiesHtml.");
        br.addItem("Request Name");
        br.addElement(requestName);
        br.addItem("Root File");
        br.addElement(rootFile);
        final String msg = br.buildExceptionMessage();
        throw new DfIllegalPropertySettingException(msg);
    }

    // ===================================================================================
    //                                                                     Set up Property
    //                                                                     ===============
    protected Map<String, DfPropHtmlFileAttribute> setupDefaultEnvProperty(DfPropHtmlRequest request,
            String propertiesFile) {
        return doSetupEnvironmentProperty(request, propertiesFile, ENV_TYPE_DEFAULT, null);
    }

    protected Map<String, DfPropHtmlFileAttribute> setupEnvironmentProperty(DfPropHtmlRequest request,
            String propertiesFile, String envType, Map<String, DfPropHtmlFileAttribute> defaultEnvMap) {
        return doSetupEnvironmentProperty(request, propertiesFile, envType, defaultEnvMap);
    }

    protected Map<String, DfPropHtmlFileAttribute> doSetupEnvironmentProperty(DfPropHtmlRequest request,
            String propertiesFile, String envType, Map<String, DfPropHtmlFileAttribute> defaultEnvMap) {
        final List<File> familyFileList = extractFamilyFileList(request.getRequestName(), propertiesFile);
        if (familyFileList.isEmpty()) {
            return DfCollectionUtil.emptyMap();
        }
        final String specifiedPureFileName = Srl.substringLastRear(propertiesFile, "/");
        final Map<String, DfPropHtmlFileAttribute> attributeMap = DfCollectionUtil.newLinkedHashMap();
        DfPropHtmlFileAttribute rootAttribute = null;
        if (defaultEnvMap != null) { // when specified environment
            for (DfPropHtmlFileAttribute attribute : defaultEnvMap.values()) {
                if (attribute.isRootFile()) { // always exists here
                    rootAttribute = attribute;
                }
            }
        }
        final DfPropHtmlRequest extendsRequest = getExtendsRequest(request);
        for (final File familyFile : familyFileList) {
            final String langType = extractLangType(familyFile.getName());
            final String fileKey = buildFileKey(familyFile, envType);
            _log.info("...Reading properties file: " + fileKey);
            final String title = fileKey + ":" + familyFile.getPath();
            final JavaPropertiesReader reader = createReader(request, title, familyFile);
            final DfPropHtmlFileAttribute extendsAttribute = findExtendsAttribute(extendsRequest, envType, langType);
            if (extendsAttribute != null) {
                prepareExtendsProperties(request, reader, extendsAttribute);
            }
            final JavaPropertiesResult jpropResult = reader.read();

            // only base-point properties are target here
            // extends properties are used only for override
            final List<JavaPropertiesProperty> jpropList = jpropResult.getPropertyBasePointOnlyList();
            final Set<String> propertyKeySet = DfCollectionUtil.newLinkedHashSet();
            for (JavaPropertiesProperty jprop : jpropList) {
                request.addProperty(envType, langType, jprop);
                propertyKeySet.add(jprop.getPropertyKey());
            }

            final DfPropHtmlFileAttribute attribute = new DfPropHtmlFileAttribute(familyFile, envType, langType);
            attribute.addPropertyKeyAll(propertyKeySet);
            attribute.setKeyCount(jpropList.size());
            attribute.setExtendsAttribute(extendsAttribute);
            attribute.addDuplicateKeyAll(jpropResult.getDuplicateKeyList());
            if (defaultEnvMap != null) { // when specified environment
                if (rootAttribute != null) { // always true
                    // every files compare with root file here
                    attribute.setStandardAttribute(rootAttribute);
                } else { // no way but just in case
                    final DfPropHtmlFileAttribute standardAttribute = defaultEnvMap.get(langType);
                    if (standardAttribute != null) {
                        // same language on standard environment is my standard here
                        attribute.setStandardAttribute(standardAttribute);
                    }
                    // if standard not found, the file exists only in the environment
                }
            } else { // when default environment
                attribute.toBeDefaultEnv();
                if (familyFile.getName().equals(specifiedPureFileName)) {
                    attribute.toBeRootFile();
                    rootAttribute = attribute; // save for relation to root
                }
            }
            request.addFileAttribute(attribute);
            attributeMap.put(langType, attribute);
        }
        if (rootAttribute != null) {
            for (DfPropHtmlFileAttribute attribute : attributeMap.values()) {
                if (attribute.isDefaultEnv() && !attribute.isRootFile()) {
                    attribute.setStandardAttribute(rootAttribute);
                }
            }
        }
        return attributeMap;
    }

    protected String buildFileKey(File familyFile, String envType) {
        return (ENV_TYPE_DEFAULT.equals(envType) ? "default" : envType) + ":" + familyFile.getName();
    }

    // ===================================================================================
    //                                                                         Family File
    //                                                                         ===========
    protected List<File> extractFamilyFileList(String requestName, String propertiesFile) {
        final List<File> familyFileList = DfCollectionUtil.newArrayList();
        final File targetDir = new File(Srl.substringLastFront(propertiesFile, "/"));
        final String pureFileName = Srl.substringLastRear(propertiesFile, "/");
        final String pureFileNameNoExt = Srl.substringLastFront(pureFileName, ".");
        final String ext = Srl.substringLastRear(pureFileName, ".");
        final String pureFileNameNoExtNoLang;
        final String langType = extractLangType(propertiesFile);
        if (!LANG_TYPE_DEFAULT.equals(langType)) { // the properties file has language type
            pureFileNameNoExtNoLang = Srl.substringLastFront(pureFileNameNoExt, "_");
        } else {
            pureFileNameNoExtNoLang = pureFileNameNoExt;
        }
        assertPropHtmlEnvironmentDirectoryExists(targetDir, requestName, propertiesFile);
        final File[] listFiles = targetDir.listFiles(new FileFilter() {
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return false;
                }
                final String pureName = file.getName();
                return pureName.startsWith(pureFileNameNoExtNoLang) && pureName.endsWith("." + ext);
            }
        });
        if (listFiles != null && listFiles.length > 0) {
            for (File file : listFiles) {
                familyFileList.add(file);
            }
        }
        return familyFileList;
    }

    protected String extractLangType(String propertiesFile) {
        final String pureFileName = Srl.substringLastRear(propertiesFile, "/");
        final String pureFileNameNoExt = Srl.substringLastFront(pureFileName, ".");
        if (pureFileNameNoExt.contains("_")) {
            final String langType = Srl.substringLastRear(pureFileNameNoExt, "_");
            if (langType.length() == 2) {
                return langType; // e.g. ja, en
            }
        }
        return LANG_TYPE_DEFAULT; // as default
    }

    protected void assertPropHtmlEnvironmentDirectoryExists(File targetDir, String requestName, String propertiesFile) {
        if (targetDir.exists()) {
            return;
        }
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the directory for the file for PropertiesHtml.");
        br.addItem("Request Name");
        br.addElement(requestName);
        br.addItem("Properties File");
        br.addElement(propertiesFile);
        final String msg = br.buildExceptionMessage();
        throw new DfIllegalPropertySettingException(msg);
    }

    // ===================================================================================
    //                                                                   Properties Reader
    //                                                                   =================
    protected JavaPropertiesReader createReader(DfPropHtmlRequest request, String title, final File familyFile) {
        return new JavaPropertiesReader(title, new JavaPropertiesStreamProvider() {
            public InputStream provideStream() throws IOException {
                return new FileInputStream(familyFile);
            }
        });
    }

    protected void prepareExtendsProperties(DfPropHtmlRequest request, JavaPropertiesReader reader,
            DfPropHtmlFileAttribute extendsAttribute) {
        DfPropHtmlFileAttribute current = extendsAttribute;
        while (true) {
            if (current == null) {
                break;
            }
            final File extendsFile = current.getPropertiesFile();
            final String extendsFileKey = buildFileKey(extendsFile, current.getEnvType());
            final String extendsTitle = extendsFileKey + ":" + extendsFile.getPath();
            reader.extendsProperties(extendsTitle, new JavaPropertiesStreamProvider() {
                public InputStream provideStream() throws IOException {
                    return new FileInputStream(extendsFile);
                }
            });
            current = current.getExtendsAttribute();
        }
        if (request.isCheckImplicitOverride()) {
            reader.checkImplicitOverride();
        }
    }

    // ===================================================================================
    //                                                                    Extends Property
    //                                                                    ================
    protected DfPropHtmlRequest getExtendsRequest(DfPropHtmlRequest request) {
        final String requestName = request.getExtendsPropRequest();
        if (Srl.is_Null_or_TrimmedEmpty(requestName)) {
            return null;
        }
        final DfPropHtmlRequest extendsPropRequest = _requestMap.get(requestName);
        if (extendsPropRequest == null) {
            String msg = "Not found the extends-property request: " + requestName;
            throw new DfIllegalPropertySettingException(msg);
        }
        return extendsPropRequest;
    }

    protected DfPropHtmlFileAttribute findExtendsAttribute(DfPropHtmlRequest extendsRequest, String envType,
            String langType) {
        if (extendsRequest == null) {
            return null;
        }
        DfPropHtmlFileAttribute attribute = extendsRequest.getFileAttribute(envType, langType);
        if (attribute == null) { // retry by default environment
            attribute = extendsRequest.getFileAttribute(ENV_TYPE_DEFAULT, langType);
            // but no retry by language because different language is not extends-target
        }
        return attribute;
    }

    // ===================================================================================
    //                                                                  Analyze Difference
    //                                                                  ==================
    protected void analyzePropertiesDiff() {
        for (Entry<String, DfPropHtmlRequest> entry : _requestMap.entrySet()) {
            final DfPropHtmlRequest request = entry.getValue();
            final Set<String> ignoredSet = request.getDiffIgnoredKeySet();
            final List<DfPropHtmlFileAttribute> attributeList = request.getFileAttributeList();
            for (DfPropHtmlFileAttribute attribute : attributeList) {
                doAnalyzePropertiesDiff(request, ignoredSet, attribute);
            }
        }
    }

    protected void doAnalyzePropertiesDiff(DfPropHtmlRequest request, Set<String> ignoredSet,
            DfPropHtmlFileAttribute attribute) {
        if (attribute.isRootFile()) {
            return;
        }
        final DfPropHtmlFileAttribute standardAttribute = attribute.getStandardAttribute();
        if (standardAttribute == null) {
            attribute.toBeLonely();
            return;
        }
        final Set<String> standardPropertyKeySet = standardAttribute.getPropertyKeySet();
        final Set<String> propertyKeySet = attribute.getPropertyKeySet();
        for (String propertyKey : propertyKeySet) {
            if (ignoredSet.contains(propertyKey)) {
                continue;
            }
            if (!standardPropertyKeySet.contains(propertyKey)) {
                final DfPropHtmlProperty property = request.getProperty(propertyKey);
                final String envType = attribute.getEnvType();
                final String langType = attribute.getLangType();
                final boolean override = property.isOverride(envType, langType);
                attribute.addOverKey(new DfPropHtmlDiffKey(propertyKey, override));
            }
        }
        for (String standardPropertyKey : standardPropertyKeySet) {
            if (ignoredSet.contains(standardPropertyKey)) {
                continue;
            }
            if (!propertyKeySet.contains(standardPropertyKey)) {
                final DfPropHtmlProperty standardProperty = request.getProperty(standardPropertyKey);
                final String envType = standardAttribute.getEnvType();
                final String langType = standardAttribute.getLangType();
                final boolean override = standardProperty.isOverride(envType, langType);
                attribute.addShortKey(new DfPropHtmlDiffKey(standardPropertyKey, override));
            }
        }
    }

    // ===================================================================================
    //                                                                         Header Info
    //                                                                         ===========
    public boolean hasTitle() {
        final String title = getTitle();
        return Srl.is_NotNull_and_NotTrimmedEmpty(title);
    }

    public String getTitle() {
        final DfDocumentProperties prop = getDocumentProperties();
        return prop.getPropertiesHtmlHeaderTitle();
    }

    // -----------------------------------------------------
    //                                           Style Sheet
    //                                           -----------
    public boolean isStyleSheetEmbedded() {
        return getDocumentProperties().isPropertiesHtmlStyleSheetEmbedded();
    }

    public boolean isStyleSheetLink() {
        return getDocumentProperties().isPropertiesHtmlStyleSheetLink();
    }

    public String getStyleSheetEmbedded() {
        return getDocumentProperties().getPropertiesHtmlStyleSheetEmbedded();
    }

    public String getStyleSheetLink() {
        return getDocumentProperties().getPropertiesHtmlStyleSheetLink();
    }

    // -----------------------------------------------------
    //                                            JavaScript
    //                                            ----------
    public boolean isJavaScriptEmbedded() {
        return getDocumentProperties().isPropertiesHtmlJavaScriptEmbedded();
    }

    public boolean isJavaScriptLink() {
        return getDocumentProperties().isPropertiesHtmlJavaScriptLink();
    }

    public String getJavaScriptEmbedded() {
        return getDocumentProperties().getPropertiesHtmlJavaScriptEmbedded();
    }

    public String getJavaScriptLink() {
        return getDocumentProperties().getPropertiesHtmlJavaScriptLink();
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

    protected DfDocumentProperties getDocumentProperties() {
        return getProperties().getDocumentProperties();
    }

    public String getProjectName() {
        return getBasicProperties().getProjectName();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public boolean existsRequest() {
        return !_requestMap.isEmpty();
    }

    public List<DfPropHtmlRequest> getRequestList() {
        return DfCollectionUtil.newArrayList(_requestMap.values());
    }

    public Map<String, DfPropHtmlRequest> getRequestMap() {
        return _requestMap;
    }
}
