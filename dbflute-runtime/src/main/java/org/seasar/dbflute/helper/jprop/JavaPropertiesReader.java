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
package org.seasar.dbflute.helper.jprop;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.helper.jprop.exception.JavaPropertiesImplicitOverrideException;
import org.seasar.dbflute.helper.jprop.exception.JavaPropertiesLonelyOverrideException;
import org.seasar.dbflute.helper.jprop.exception.JavaPropertiesReadFailureException;
import org.seasar.dbflute.helper.jprop.exception.JavaPropertiesStreamNotFoundException;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.DfCollectionUtil.AccordingToOrderIdExtractor;
import org.seasar.dbflute.util.DfCollectionUtil.AccordingToOrderResource;
import org.seasar.dbflute.util.DfReflectionUtil;
import org.seasar.dbflute.util.Srl;
import org.seasar.dbflute.util.Srl.ScopeInfo;

/**
 * @author jflute
 * @since 1.0.1 (2012/12/15 Saturday)
 */
public class JavaPropertiesReader {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String OVERRIDE_ANNOTATION = "@Override";
    public static final String SECURE_ANNOTATION = "@Secure";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                                 Basic
    //                                                 -----
    protected final String _title;
    protected final JavaPropertiesStreamProvider _streamProvider;

    // -----------------------------------------------------
    //                                                Option
    //                                                ------
    protected final Map<String, JavaPropertiesStreamProvider> _extendsProviderMap = newLinkedHashMapSized(4);
    protected boolean _checkImplicitOverride;
    protected String _streamEncoding; // used if set

    // -----------------------------------------------------
    //                                            Reflection
    //                                            ----------
    protected Method _convertMethod; // cached
    protected boolean _convertMethodNotFound;
    protected final Properties _reflectionProperties = new Properties();

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public JavaPropertiesReader(String title, JavaPropertiesStreamProvider streamProvider) {
        _title = title;
        _streamProvider = streamProvider;
    }

    // -----------------------------------------------------
    //                                                Option
    //                                                ------
    public JavaPropertiesReader extendsProperties(String title, JavaPropertiesStreamProvider extendsStreamProvider) {
        if (_extendsProviderMap.containsKey(title)) {
            String msg = "The argument 'title' has already been registered:";
            msg = msg + " title=" + title + " registered=" + _extendsProviderMap.keySet();
            throw new IllegalArgumentException(msg);
        }
        _extendsProviderMap.put(title, extendsStreamProvider);
        return this;
    }

    public JavaPropertiesReader checkImplicitOverride() {
        _checkImplicitOverride = true;
        return this;
    }

    public JavaPropertiesReader encodeAsUTF8() {
        _streamEncoding = "UTF-8";
        return this;
    }

    public JavaPropertiesReader encodeAs(String encoding) {
        _streamEncoding = encoding;
        return this;
    }

    // ===================================================================================
    //                                                                                Read
    //                                                                                ====
    public JavaPropertiesResult read() {
        final List<JavaPropertiesProperty> propertyList = newArrayList();
        final List<String> duplicateKeyList = newArrayList();
        final Map<String, String> keyCommentMap = readKeyCommentMap(duplicateKeyList);
        final Properties prop = readPlainProperties();
        final List<String> keyList = orderKeyList(prop, keyCommentMap);
        for (String key : keyList) {
            final String value = prop.getProperty(key);
            final String comment = keyCommentMap.get(key);

            final JavaPropertiesProperty property = new JavaPropertiesProperty(key, value);

            final String defName = Srl.replace(key, ".", "_").toUpperCase();
            property.setDefName(defName);

            final String camelizedName = Srl.camelize(defName);
            property.setCamelizedName(camelizedName);
            property.setCapCamelName(Srl.initCap(camelizedName));
            property.setUncapCamelName(Srl.initUncap(camelizedName));

            final List<ScopeInfo> variableScopeList = newArrayList();
            {
                final List<ScopeInfo> scopeList;
                if (Srl.is_NotNull_and_NotTrimmedEmpty(value)) {
                    scopeList = Srl.extractScopeList(value, "{", "}"); // e.g. {0} is for {1}.
                } else {
                    scopeList = DfCollectionUtil.emptyList();
                }
                for (ScopeInfo scopeInfo : scopeList) {
                    final String content = scopeInfo.getContent();
                    try {
                        Integer.valueOf(content);
                        variableScopeList.add(scopeInfo);
                    } catch (NumberFormatException ignored) { // e.g. {A} is for {B}
                    }
                }
            }
            final List<Integer> variableNumberList = DfCollectionUtil.newArrayList();
            for (ScopeInfo scopeInfo : variableScopeList) {
                variableNumberList.add(valueOfVariableNumber(key, scopeInfo.getContent()));
            }
            property.setVariableArgDef(buildVariableArgDef(variableNumberList));
            property.setVariableArgSet(buildVariableArgSet(variableNumberList));
            property.setVariableNumberList(variableNumberList);
            property.setComment(comment);
            if (containsSecureAnnotation(property)) {
                property.toBeSecure();
            }

            propertyList.add(property);
        }
        return prepareResult(prop, propertyList, duplicateKeyList);
    }

    protected boolean containsSecureAnnotation(JavaPropertiesProperty property) {
        final String comment = property.getComment();
        return comment != null && Srl.containsIgnoreCase(comment, SECURE_ANNOTATION);
    }

    // -----------------------------------------------------
    //                                             Order Key
    //                                             ---------
    protected List<String> orderKeyList(Properties prop, final Map<String, String> keyCommentMap) {
        final List<Object> orderedList = newArrayList(prop.keySet());
        final AccordingToOrderResource<Object, String> resource = new AccordingToOrderResource<Object, String>();
        resource.setIdExtractor(new AccordingToOrderIdExtractor<Object, String>() {
            public String extractId(Object element) {
                return (String) element;
            }
        });
        resource.setOrderedUniqueIdList(newArrayList(keyCommentMap.keySet()));
        DfCollectionUtil.orderAccordingTo(orderedList, resource);
        final List<String> keyList = newArrayList();
        for (Object keyObj : orderedList) {
            keyList.add((String) keyObj);
        }
        return keyList;
    }

    // -----------------------------------------------------
    //                                        Prepare Result
    //                                        --------------
    protected JavaPropertiesResult prepareResult(Properties prop, List<JavaPropertiesProperty> propertyList,
            List<String> duplicateKeyList) {
        final JavaPropertiesResult propResult;
        if (!_extendsProviderMap.isEmpty()) {
            final JavaPropertiesReader extendsReader = createExtendsReader();
            final JavaPropertiesResult extendsPropResult = extendsReader.read();
            final List<JavaPropertiesProperty> mergedList = mergeExtendsPropResult(propertyList, extendsPropResult);
            propResult = new JavaPropertiesResult(prop, mergedList, duplicateKeyList, extendsPropResult);
        } else {
            propResult = new JavaPropertiesResult(prop, propertyList, duplicateKeyList);
        }
        return propResult;
    }

    protected JavaPropertiesReader createExtendsReader() {
        final Map<String, JavaPropertiesStreamProvider> providerMap = newLinkedHashMap(_extendsProviderMap);
        final Entry<String, JavaPropertiesStreamProvider> firstEntry = providerMap.entrySet().iterator().next();
        final String firstKey = firstEntry.getKey();
        final JavaPropertiesStreamProvider firstProvider = firstEntry.getValue();
        final JavaPropertiesReader extendsReader = new JavaPropertiesReader(firstKey, firstProvider);
        providerMap.remove(firstKey);
        for (Entry<String, JavaPropertiesStreamProvider> entry : providerMap.entrySet()) { // next extends
            extendsReader.extendsProperties(entry.getKey(), entry.getValue());
        }
        if (_checkImplicitOverride) {
            extendsReader.checkImplicitOverride();
        }
        return extendsReader;
    }

    // ===================================================================================
    //                                                                               Merge
    //                                                                               =====
    protected List<JavaPropertiesProperty> mergeExtendsPropResult(List<JavaPropertiesProperty> propertyList,
            JavaPropertiesResult extendsPropResult) {
        final List<JavaPropertiesProperty> extendsPropertyList = extendsPropResult.getPropertyList();
        for (JavaPropertiesProperty property : extendsPropertyList) {
            property.toBeExtends();
        }
        final Map<String, JavaPropertiesProperty> extendsPropertyMap = toPropertyMap(extendsPropertyList);
        for (JavaPropertiesProperty property : propertyList) {
            final String propertyKey = property.getPropertyKey();
            if (extendsPropertyMap.containsKey(propertyKey)) {
                property.toBeOverride();
                checkImplicitOverride(property);
                final JavaPropertiesProperty extendsProperty = extendsPropertyMap.get(propertyKey);
                inheritSecure(property, extendsProperty);
                inheritComment(property, extendsProperty);
            } else {
                checkLonelyOverride(property);
            }
        }
        final Set<JavaPropertiesProperty> mergedPropertySet = DfCollectionUtil.newLinkedHashSet(propertyList);
        mergedPropertySet.addAll(extendsPropertyMap.values()); // merge (add if not exists)
        return DfCollectionUtil.newArrayList(mergedPropertySet);
    }

    protected Map<String, JavaPropertiesProperty> toPropertyMap(List<JavaPropertiesProperty> propertyList) {
        final Map<String, JavaPropertiesProperty> propertyMap = DfCollectionUtil.newLinkedHashMap();
        for (JavaPropertiesProperty property : propertyList) {
            propertyMap.put(property.getPropertyKey(), property);
        }
        return propertyMap;
    }

    protected void checkImplicitOverride(JavaPropertiesProperty property) {
        if (_checkImplicitOverride && !containsOverrideAnnotation(property)) {
            throwJavaPropertiesImplicitOverrideException(property);
        }
    }

    protected void checkLonelyOverride(JavaPropertiesProperty property) {
        if (_checkImplicitOverride && containsOverrideAnnotation(property)) {
            throwJavaPropertiesLonelyOverrideException(property);
        }
    }

    protected boolean containsOverrideAnnotation(JavaPropertiesProperty property) {
        final String comment = property.getComment();
        return comment != null && Srl.containsIgnoreCase(comment, OVERRIDE_ANNOTATION);
    }

    protected void inheritSecure(JavaPropertiesProperty property, JavaPropertiesProperty extendsProperty) {
        if (extendsProperty.isSecure()) {
            property.toBeSecure(); // inherit
        }
    }

    protected void inheritComment(JavaPropertiesProperty property, JavaPropertiesProperty extendsProperty) {
        final String comment = property.getComment();
        if (hasCommentIgnoreAnnotation(comment)) {
            return;
        }
        // only annotations or empty
        final String extendsPureComment = extractPureComment(extendsProperty.getComment());
        if (Srl.is_Null_or_TrimmedEmpty(extendsPureComment)) {
            return;
        }
        final String baseComment = Srl.is_NotNull_and_NotTrimmedEmpty(comment) ? comment + " " : "";
        property.setComment(baseComment + extendsPureComment); // inherit
    }

    protected boolean hasCommentIgnoreAnnotation(String comment) {
        if (Srl.is_Null_or_TrimmedEmpty(comment)) {
            return false;
        }
        final String replaced = extractPureComment(comment);
        return Srl.is_NotNull_and_NotTrimmedEmpty(replaced);
    }

    protected String extractPureComment(String comment) {
        if (Srl.is_Null_or_TrimmedEmpty(comment)) {
            return comment;
        }
        final Map<String, String> fromToMap = new HashMap<String, String>();
        fromToMap.put(OVERRIDE_ANNOTATION, "");
        fromToMap.put(SECURE_ANNOTATION, "");
        return Srl.replaceBy(comment, fromToMap);
    }

    protected void throwJavaPropertiesImplicitOverrideException(JavaPropertiesProperty property) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Found the implicit override property.");
        br.addItem("Advice");
        br.addElement("The property overrides the inherited property.");
        br.addElement("Do you want to override it? Or is it your mistake?");
        br.addElement("If you override it, set the annotation " + OVERRIDE_ANNOTATION + " in the property.");
        br.addElement("For example:");
        br.addElement("  # " + OVERRIDE_ANNOTATION);
        br.addElement("  foo.bar.prop = abc");
        br.addItem("Properties");
        br.addElement(_title);
        br.addItem("Implicit Override Property");
        br.addElement(property.getPropertyKey());
        br.addElement(property.getPropertyValue());
        final String msg = br.buildExceptionMessage();
        throw new JavaPropertiesImplicitOverrideException(msg);
    }

    protected void throwJavaPropertiesLonelyOverrideException(JavaPropertiesProperty property) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Found the lonely override property.");
        br.addItem("Advice");
        br.addElement("The property does not override any inherited property");
        br.addElement("but the property have the annotation " + OVERRIDE_ANNOTATION + ".");
        br.addElement("Remove the annotation or fix the mistake of the property key.");
        br.addItem("Properties");
        br.addElement(_title);
        br.addItem("Lonely Override Property");
        br.addElement(property.getPropertyKey());
        br.addElement(property.getPropertyValue());
        final String msg = br.buildExceptionMessage();
        throw new JavaPropertiesLonelyOverrideException(msg);
    }

    // ===================================================================================
    //                                                                         Read Helper
    //                                                                         ===========
    protected Map<String, String> readKeyCommentMap(List<String> duplicateKeyList) {
        final Map<String, String> keyCommentMap = DfCollectionUtil.newLinkedHashMap();
        final String encoding = "UTF-8"; // because properties normally cannot have double bytes
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(preparePropFileStream(), encoding));
            String previousComment = null;
            while (true) {
                final String line = br.readLine();
                if (line == null) {
                    break;
                }
                final String ltrimmedLine = Srl.ltrim(line);
                if (ltrimmedLine.startsWith("# ")) { // comment lines
                    final String commentCandidate = Srl.substringFirstRear(ltrimmedLine, "#").trim();
                    if (ltrimmedLine.contains("=")) { // you cannot contain equal mark in comment
                        previousComment = null; // e.g. #foo.bar.qux = value (comment out???)
                    } else {
                        if (!ltrimmedLine.trim().equals("#")) { // not sharp lonely
                            previousComment = commentCandidate; // 99% comment
                        }
                    }
                    continue;
                }
                // key value here
                if (!ltrimmedLine.contains("=")) { // what's this? (no way)
                    continue;
                }
                final String key = Srl.substringFirstFront(ltrimmedLine, "=").trim();
                if (keyCommentMap.containsKey(key)) {
                    duplicateKeyList.add(key);
                    keyCommentMap.remove(key); // remove existing key for order and override
                }
                keyCommentMap.put(key, loadConvert(previousComment));
                previousComment = null;
            }
        } catch (IOException e) {
            throwJavaPropertiesReadFailureException(e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ignored) {
                }
            }
        }
        return keyCommentMap;
    }

    protected Properties readPlainProperties() {
        final Properties prop = new Properties();
        InputStream ins = null;
        try {
            ins = preparePropFileStream();
            loadProperties(prop, ins);
        } catch (IOException e) {
            throwJavaPropertiesReadFailureException(e);
        } finally {
            if (ins != null) {
                try {
                    ins.close();
                } catch (IOException ignored) {
                }
            }
        }
        return prop;
    }

    protected InputStream preparePropFileStream() throws IOException {
        final InputStream stream = _streamProvider.provideStream();
        if (stream == null) {
            throwJavaPropertiesStreamNotFoundException();
        }
        return stream;
    }

    protected void loadProperties(Properties prop, InputStream ins) throws IOException {
        if (_streamEncoding != null) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(ins, _streamEncoding));
                prop.load(br);
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        } else {
            prop.load(ins);
        }
    }

    protected void throwJavaPropertiesStreamNotFoundException() {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the steram for the properties file.");
        br.addItem("Advice");
        br.addElement("The stream provider should not return null but null returned.");
        br.addElement("Make sure your resource path for the properties.");
        br.addItem("Properties");
        br.addElement(_title);
        br.addItem("Stream Provider");
        br.addElement(_streamProvider);
        final String msg = br.buildExceptionMessage();
        throw new JavaPropertiesStreamNotFoundException(msg);
    }

    protected void throwJavaPropertiesReadFailureException(IOException e) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to read the properties file.");
        br.addItem("Properties");
        br.addElement(_title);
        br.addItem("IOException");
        br.addElement(e.getClass().getName());
        br.addElement(e.getMessage());
        final String msg = br.buildExceptionMessage();
        throw new JavaPropertiesReadFailureException(msg, e);
    }

    // ===================================================================================
    //                                                                     Variable Helper
    //                                                                     ===============
    protected Integer valueOfVariableNumber(String key, String content) {
        try {
            return Integer.valueOf(content);
        } catch (NumberFormatException e) {
            String msg = "The NOT-number variable was found: provider=" + _streamProvider + " key=" + key;
            throw new IllegalStateException(msg, e);
        }
    }

    protected String buildVariableArgDef(List<Integer> variableNumberList) {
        final StringBuilder sb = new StringBuilder();
        for (Integer number : variableNumberList) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("String arg").append(number);
        }
        return sb.toString();
    }

    protected String buildVariableArgSet(List<Integer> variableNumberList) {
        final StringBuilder sb = new StringBuilder();
        for (Integer number : variableNumberList) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("arg").append(number);
        }
        return sb.toString();
    }

    // ===================================================================================
    //                                                                     Unicode Convert
    //                                                                     ===============
    protected String loadConvert(String expression) {
        if (expression == null) {
            return null;
        }
        final Method method = getConvertMethod();
        if (method == null) {
            return expression;
        }
        final char[] in = expression.toCharArray();
        final Object[] args = new Object[] { in, 0, expression.length(), new char[] {} };
        return (String) DfReflectionUtil.invoke(method, _reflectionProperties, args);
    }

    protected Method getConvertMethod() {
        if (_convertMethod != null) {
            return _convertMethod;
        }
        if (_convertMethodNotFound) {
            return null;
        }
        final Class<?>[] argTypes = new Class<?>[] { char[].class, int.class, int.class, char[].class };
        _convertMethod = DfReflectionUtil.getWholeMethod(Properties.class, "loadConvert", argTypes);
        if (_convertMethod == null) {
            _convertMethodNotFound = true;
        } else {
            _convertMethod.setAccessible(true);
        }
        return _convertMethod;
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected <ELEMENT> ArrayList<ELEMENT> newArrayList() {
        return DfCollectionUtil.newArrayList();
    }

    protected <ELEMENT> ArrayList<ELEMENT> newArrayList(Collection<ELEMENT> elements) {
        return DfCollectionUtil.newArrayList(elements);
    }

    protected <KEY, VALUE> LinkedHashMap<KEY, VALUE> newLinkedHashMap(Map<KEY, VALUE> map) {
        return DfCollectionUtil.newLinkedHashMap(map);
    }

    protected <KEY, VALUE> LinkedHashMap<KEY, VALUE> newLinkedHashMapSized(int size) {
        return DfCollectionUtil.newLinkedHashMapSized(size);
    }
}
