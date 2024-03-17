/*
 * Copyright 2014-2024 the original author or authors.
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
package org.dbflute.helper.jprop;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.dbflute.helper.jprop.exception.JavaPropertiesImplicitOverrideException;
import org.dbflute.helper.jprop.exception.JavaPropertiesLonelyOverrideException;
import org.dbflute.helper.jprop.exception.JavaPropertiesReadFailureException;
import org.dbflute.helper.jprop.exception.JavaPropertiesStreamNotFoundException;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.DfCollectionUtil.AccordingToOrderIdExtractor;
import org.dbflute.util.DfCollectionUtil.AccordingToOrderResource;
import org.dbflute.util.Srl;
import org.dbflute.util.Srl.ScopeInfo;

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
    protected boolean _useNonNumberVariable;
    protected Set<String> _variableExceptSet; // used if set
    protected boolean _suppressVariableOrder; // for compatible

    // to avoid Java11 warning
    //// -----------------------------------------------------
    ////                                            Reflection
    ////                                            ----------
    //protected Method _convertMethod; // cached
    //protected boolean _convertMethodNotFound;
    //protected final Properties _reflectionProperties = new Properties();

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
    public JavaPropertiesReader extendsProperties(String title, JavaPropertiesStreamProvider noArgLambda) {
        if (_extendsProviderMap.containsKey(title)) {
            String msg = "The argument 'title' has already been registered:";
            msg = msg + " title=" + title + " registered=" + _extendsProviderMap.keySet();
            throw new IllegalArgumentException(msg);
        }
        _extendsProviderMap.put(title, noArgLambda);
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

    public JavaPropertiesReader useNonNumberVariable() {
        _useNonNumberVariable = true;
        return this;
    }

    public JavaPropertiesReader useVariableExcept(Set<String> variableExceptSet) {
        if (variableExceptSet == null) {
            throw new IllegalArgumentException("The argument 'variableExceptSet' should not be null.");
        }
        _variableExceptSet = variableExceptSet;
        return this;
    }

    public JavaPropertiesReader suppressVariableOrder() { // for compatible
        _suppressVariableOrder = true;
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

            final List<ScopeInfo> variableScopeList = analyzeVariableScopeList(value);
            final List<Integer> variableNumberList = DfCollectionUtil.newArrayListSized(variableScopeList.size());
            final List<String> variableStringList = DfCollectionUtil.newArrayListSized(variableScopeList.size());
            reflectToVariableList(key, variableScopeList, variableNumberList, variableStringList);
            property.setVariableNumberList(variableNumberList);
            property.setVariableStringList(variableStringList);
            final List<String> variableArgNameList = prepareVariableArgNameList(variableStringList);
            property.setVariableArgNameList(variableArgNameList);
            property.setVariableArgDef(buildVariableArgDef(variableArgNameList));
            property.setVariableArgSet(buildVariableArgSet(variableArgNameList));
            property.setComment(comment);
            if (containsSecureAnnotation(property)) {
                property.toBeSecure();
            }

            propertyList.add(property);
        }
        return prepareResult(prop, propertyList, duplicateKeyList);
    }

    protected List<ScopeInfo> analyzeVariableScopeList(String value) {
        final List<ScopeInfo> variableScopeList = newArrayList();
        {
            final List<ScopeInfo> scopeList;
            if (Srl.is_NotNull_and_NotTrimmedEmpty(value)) {
                scopeList = extractVariableScopeList(value);
            } else {
                scopeList = DfCollectionUtil.emptyList();
            }
            for (ScopeInfo scopeInfo : scopeList) {
                final String content = scopeInfo.getContent();
                if (isUnsupportedVariableContent(content)) {
                    continue;
                }
                variableScopeList.add(scopeInfo);
            }
        }
        orderVariableScopeList(variableScopeList);
        return variableScopeList;
    }

    protected List<ScopeInfo> extractVariableScopeList(String value) {
        return Srl.extractScopeList(value, "{", "}"); // e.g. {0}, {1}
    }

    protected boolean isUnsupportedVariableContent(String content) {
        if (content.contains(" ")) { // e.g. {sea land}
            return true;
        }
        if (!_useNonNumberVariable && !Srl.isNumberHarfAll(content)) { // number only but non-number
            // e.g. {sea}, {land}
            return true;
        }
        if (_variableExceptSet != null && _variableExceptSet.contains(content)) { // e.g. {item} in LastaFlute
            return true;
        }
        return false;
    }

    protected void orderVariableScopeList(List<ScopeInfo> variableScopeList) {
        if (_suppressVariableOrder) { // for compatible
            return;
        }
        // should be ordered for MessageFormat by jflute (2017/08/19)
        final VariableOrderAgent orderAgent = createVariableOrderAgent();
        orderAgent.orderScopeList(variableScopeList);
    }

    protected VariableOrderAgent createVariableOrderAgent() {
        return new VariableOrderAgent();
    }

    public static class VariableOrderAgent { // can be used other libraries

        public void orderScopeList(List<ScopeInfo> variableScopeList) {
            Collections.sort(variableScopeList, (o1, o2) -> { // ...after all, split indexed and named
                return Srl.isNumberHarfAll(o1.getContent()) ? -1 : 0;
            });
            orderIndexedOnly(variableScopeList); // e.g. {2}-{sea}-{0}-{land}-{1} to {0}-{sea}-{1}-{land}-{2}
            orderNamedOnly(variableScopeList); // e.g. {2}-{sea}-{0}-{land}-{1} to {0}-{land}-{1}-{sea}-{2}
        }

        protected void orderIndexedOnly(List<ScopeInfo> variableScopeList) {
            final Map<Integer, ScopeInfo> namedMap = new LinkedHashMap<Integer, ScopeInfo>();
            for (int i = 0; i < variableScopeList.size(); i++) {
                final ScopeInfo element = variableScopeList.get(i);
                if (!Srl.isNumberHarfAll(element.getContent())) {
                    namedMap.put(i, element);
                }
            }
            final List<ScopeInfo> sortedList = variableScopeList.stream()
                    .filter(el -> Srl.isNumberHarfAll(el.getContent()))
                    .sorted(Comparator.comparing(el -> filterNumber(el.getContent()), Comparator.naturalOrder()))
                    .collect(Collectors.toList());
            namedMap.forEach((key, value) -> {
                sortedList.add(key, value);
            });
            variableScopeList.clear();
            variableScopeList.addAll(sortedList);
        }

        protected String filterNumber(String el) {
            final String ltrimmed = Srl.ltrim(el, "0"); // zero suppressed e.g. "007" to "7"
            if (ltrimmed.isEmpty() && el.contains("0")) { // e.g. "000"
                return "0";
            } else {
                return ltrimmed;
            }
        }

        protected void orderNamedOnly(List<ScopeInfo> variableScopeList) {
            final Map<Integer, ScopeInfo> indexedMap = new LinkedHashMap<Integer, ScopeInfo>();
            for (int i = 0; i < variableScopeList.size(); i++) {
                final ScopeInfo element = variableScopeList.get(i);
                if (Srl.isNumberHarfAll(element.getContent())) {
                    indexedMap.put(i, element);
                }
            }
            final List<ScopeInfo> sortedList = variableScopeList.stream().filter(el -> {
                return !Srl.isNumberHarfAll(el.getContent());
            }).sorted((o1, o2) -> {
                final String v1 = o1.getContent();
                final String v2 = o2.getContent();
                if (isSpecialNamedOrder(v1, v2)) {
                    return -1;
                } else if (isSpecialNamedOrder(v2, v1)) {
                    return 1;
                }
                return v1.compareTo(v2);
            }).collect(Collectors.toList());
            indexedMap.forEach((key, value) -> {
                sortedList.add(key, value);
            });
            variableScopeList.clear();
            variableScopeList.addAll(sortedList);
        }

        protected boolean isSpecialNamedOrder(String v1, String v2) {
            return v1.equals("min") && v2.equals("max") // used by e.g. Hibernate Validator
                    || v1.equals("minimum") && v2.equals("maximum") //
                    || v1.equals("start") && v2.equals("end") //
                    || v1.equals("before") && v2.equals("after") //
            ;
        }
    }

    protected void reflectToVariableList(String key, List<ScopeInfo> variableScopeList, List<Integer> variableNumberList,
            List<String> variableStringList) {
        for (ScopeInfo scopeInfo : variableScopeList) {
            final String content = scopeInfo.getContent();
            final Integer variableNumber = valueOfVariableNumber(key, content);
            if (variableNumber != null) { // for non-number option
                variableNumberList.add(variableNumber);
            }
            variableStringList.add(content); // contains all elements
        }
    }

    protected List<String> prepareVariableArgNameList(List<String> variableStringList) {
        final List<String> variableArgNameList = DfCollectionUtil.newArrayListSized(variableStringList.size());
        for (Object name : variableStringList) {
            variableArgNameList.add(doBuildVariableArgName(name));
        }
        return variableArgNameList;
    }

    protected String doBuildVariableArgName(Object name) {
        return startsWithNumberVariable(name) ? ("arg" + name) : name.toString();
    }

    protected boolean startsWithNumberVariable(Object variable) {
        return "1234567890".contains(Srl.cut(variable.toString(), 1)); // sorry, simple logic
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
            propResult = newJavaPropertiesResult(prop, duplicateKeyList, extendsPropResult, mergedList);
        } else {
            propResult = newJavaPropertiesResult(prop, propertyList, duplicateKeyList);
        }
        return propResult;
    }

    protected JavaPropertiesResult newJavaPropertiesResult(Properties prop, List<String> duplicateKeyList,
            JavaPropertiesResult extendsPropResult, List<JavaPropertiesProperty> mergedList) {
        return new JavaPropertiesResult(prop, mergedList, duplicateKeyList, extendsPropResult);
    }

    protected JavaPropertiesResult newJavaPropertiesResult(Properties prop, List<JavaPropertiesProperty> propertyList,
            List<String> duplicateKeyList) {
        return new JavaPropertiesResult(prop, propertyList, duplicateKeyList);
    }

    protected JavaPropertiesReader createExtendsReader() {
        final Map<String, JavaPropertiesStreamProvider> providerMap = newLinkedHashMap(_extendsProviderMap);
        final Entry<String, JavaPropertiesStreamProvider> firstEntry = providerMap.entrySet().iterator().next();
        final String firstKey = firstEntry.getKey();
        final JavaPropertiesStreamProvider firstProvider = firstEntry.getValue();
        final JavaPropertiesReader extendsReader = newExtendsReader(firstKey, firstProvider);
        providerMap.remove(firstKey);
        for (Entry<String, JavaPropertiesStreamProvider> entry : providerMap.entrySet()) { // next extends
            extendsReader.extendsProperties(entry.getKey(), entry.getValue());
        }
        if (_checkImplicitOverride) {
            extendsReader.checkImplicitOverride();
        }
        if (_streamEncoding != null) {
            extendsReader.encodeAs(_streamEncoding);
        }
        return extendsReader;
    }

    protected JavaPropertiesReader newExtendsReader(String title, JavaPropertiesStreamProvider streamProvider) {
        return newJavaPropertiesReader(title, streamProvider);
    }

    // general factory (LastaFlute may override this as patch, so keep it)
    protected JavaPropertiesReader newJavaPropertiesReader(String title, JavaPropertiesStreamProvider streamProvider) {
        return new JavaPropertiesReader(title, streamProvider); // for e.g. extends
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
    //                                                                    Property Comment
    //                                                                    ================
    protected Map<String, String> readKeyCommentMap(List<String> duplicateKeyList) {
        final Map<String, String> keyCommentMap = DfCollectionUtil.newLinkedHashMap();
        final String encoding = "UTF-8"; // because properties normally cannot have double bytes
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(preparePropFileStream(), encoding));
            final StringBuilder commentSb = new StringBuilder();
            while (true) {
                final String line = br.readLine();
                if (line == null) {
                    break;
                }
                final String ltrimmedLine = Srl.ltrim(line);
                if (ltrimmedLine.startsWith("#")) { // comment lines
                    final String commentCandidate = Srl.substringFirstRear(ltrimmedLine, "#").trim();
                    if (maybeDelimiterLineComment(ltrimmedLine)) { // e.g. #foo.bar.qux = value (comment out???)
                        commentSb.setLength(0); // clear comments for (maybe) previous property
                    } else {
                        if (!ltrimmedLine.trim().equals("#")) { // not sharp lonely
                            if (commentSb.length() > 0) { // second or more lines
                                // "\n" string is treated as line separator after loading convert
                                commentSb.append("\\n");
                            }
                            commentSb.append(commentCandidate); // 99% comment
                        }
                    }
                    continue;
                }
                // non-comment here
                if (!ltrimmedLine.contains("=")) { // line separated property value for previous
                    commentSb.setLength(0); // clear previous comment for line separated value
                    continue;
                }
                // key value here
                final String key = Srl.substringFirstFront(ltrimmedLine, "=").trim();
                if (keyCommentMap.containsKey(key)) {
                    duplicateKeyList.add(key);
                    keyCommentMap.remove(key); // remove existing key for order and override
                }
                keyCommentMap.put(key, loadConvert(commentSb.toString()));
                commentSb.setLength(0);
            }
        } catch (IOException e) {
            throwJavaPropertiesReadFailureException(e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ignored) {}
            }
        }
        return keyCommentMap;
    }

    protected boolean maybeDelimiterLineComment(String lineComment) { // best effort logic
        if (lineComment.contains("===") || lineComment.contains("---")) { // maybe tag comment, feeling value
            return true;
        }
        if (lineComment.contains("=")) {
            // e.g. #foo.bar.qux = value (comment out???)
            // not "sea land piari = bonvo dstore" (natural language)
            // and also tag comment using equal is here
            final String left = Srl.substringFirstFront(lineComment, "=");
            return !Srl.contains(left.trim(), " ");
        }
        return false; // normal line comment as natural language
    }

    // ===================================================================================
    //                                                                     Unicode Convert
    //                                                                     ===============
    protected String loadConvert(String expression) {
        if (expression == null || expression.isEmpty()) {
            return null;
        }
        final String encoding = "UTF-8";
        final String fixedKey = "sea";
        final String propStyle = fixedKey + " = " + Srl.quoteDouble(expression); // quote not to trim
        try {
            final Properties prop = new Properties(); // work instance
            prop.load(new InputStreamReader(new ByteArrayInputStream(propStyle.getBytes(encoding)), encoding));
            final String converted = prop.getProperty(fixedKey); // not null logically
            return Srl.unquoteDouble(converted);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Not found the encoding: " + encoding, e);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load the input stream: " + encoding, e);
        }
        // to avoid Java11 warning
        //final Method method = getConvertMethod();
        //if (method == null) {
        //    return expression;
        //}
        //final char[] in = expression.toCharArray();
        //final Object[] args = new Object[] { in, 0, expression.length(), new char[] {} };
        //return (String) DfReflectionUtil.invoke(method, _reflectionProperties, args);
    }

    // to avoid Java11 warning
    //protected Method getConvertMethod() {
    //    if (_convertMethod != null) {
    //        return _convertMethod;
    //    }
    //    if (_convertMethodNotFound) {
    //        return null;
    //    }
    //    final Class<?>[] argTypes = new Class<?>[] { char[].class, int.class, int.class, char[].class };
    //    _convertMethod = DfReflectionUtil.getWholeMethod(Properties.class, "loadConvert", argTypes);
    //    if (_convertMethod == null) {
    //        _convertMethodNotFound = true;
    //    } else {
    //        _convertMethod.setAccessible(true);
    //    }
    //    return _convertMethod;
    //}

    // ===================================================================================
    //                                                                    Plain Properties
    //                                                                    ================
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
                } catch (IOException ignored) {}
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
                    } catch (IOException ignored) {}
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
        if (Srl.isNumberHarfAll(content)) {
            try {
                return Integer.valueOf(content);
            } catch (NumberFormatException e) { // no way, but just in case
                if (_useNonNumberVariable) {
                    return null;
                }
                String msg = "The NON-number variable was found: provider=" + _streamProvider + " key=" + key;
                throw new IllegalStateException(msg, e);
            }
        } else { // non-number
            if (_useNonNumberVariable) {
                return null;
            } else {
                String msg = "The NON-number variable was found: provider=" + _streamProvider + " key=" + key;
                throw new IllegalStateException(msg);
            }
        }
    }

    protected String buildVariableArgDef(List<String> variableArgNameList) {
        final StringBuilder sb = new StringBuilder();
        for (String name : variableArgNameList) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            doBuildVariableArgStringDef(sb, name);
        }
        return sb.toString();
    }

    protected void doBuildVariableArgStringDef(StringBuilder sb, String variableName) {
        sb.append("String ").append(variableName); // java style
    }

    protected String buildVariableArgSet(List<String> variableArgNameList) {
        final StringBuilder sb = new StringBuilder();
        for (String name : variableArgNameList) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(name);
        }
        return sb.toString();
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
