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
package org.seasar.dbflute.logic.sql2entity.pmbean;

import java.util.List;

import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.properties.DfClassificationProperties;
import org.seasar.dbflute.properties.assistant.classification.DfClassificationTop;
import org.seasar.dbflute.resource.DBFluteSystem;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 * @since 0.6.3 (2008/02/05 Tuesday)
 */
public class DfPmbPropertyOptionClassification {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final String OPTION_PREFIX = "cls(";
    protected static final String OPTION_SUFFIX = ")";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfPmbMetaData _pmbMetaData;
    protected final String _propertyName;
    protected final DfClassificationProperties _classificationProperties;
    protected final DfPmbPropertyOptionFinder _pmbMetaDataPropertyOptionFinder;
    protected String _specifiedValue;
    protected boolean _extracted;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfPmbPropertyOptionClassification(DfPmbMetaData pmbMetaData, String propertyName,
            DfClassificationProperties classificationProperties,
            DfPmbPropertyOptionFinder pmbMetaDataPropertyOptionFinder) {
        _pmbMetaData = pmbMetaData;
        _propertyName = propertyName;
        _classificationProperties = classificationProperties;
        _pmbMetaDataPropertyOptionFinder = pmbMetaDataPropertyOptionFinder;
    }

    // ===================================================================================
    //                                                                      Classification
    //                                                                      ==============
    public boolean isPropertyOptionSpecifiedClassification() {
        return extractClassificationNameFromOption(false) != null;
    }

    public boolean isPropertyOptionClassificationFixedElement() {
        final String classificationName = extractClassificationNameFromOption(false);
        return classificationName != null && classificationName.contains(".");
    }

    public boolean isPropertyOptionClassificationFixedElementList() {
        final String classificationName = extractClassificationNameFromOption(false);
        return classificationName != null && classificationName.contains(".") && classificationName.contains(",");
    }

    public String getPropertyOptionClassificationName() {
        final String classificationName = extractClassificationNameFromOption(true);
        return Srl.substringFirstFront(classificationName, ".");
    }

    public String getPropertyOptionClassificationFixedElement() { // returns Bar if "Foo.Bar" 
        final String classificationName = extractClassificationNameFromOption(true);
        return Srl.substringFirstRear(classificationName, ".");
    }

    public List<String> getPropertyOptionClassificationFixedElementList() { // returns [Bar. Baz] if "Foo.Bar, Baz"
        final String fixedElement = getPropertyOptionClassificationFixedElement();
        return Srl.splitListTrimmed(fixedElement, ",");
    }

    public DfClassificationTop getPropertyOptionClassificationTop() {
        final String classificationName = extractClassificationNameFromOption(true);
        final DfClassificationTop classificationTop = _classificationProperties
                .getClassificationTop(classificationName);
        if (classificationTop == null) {
            throwClassificationNotFoundException(classificationName);
        }
        return classificationTop;
    }

    protected void throwClassificationNotFoundException(String classificationName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The classification in the parameter comment option was not found.");
        br.addItem("ParameterBean");
        br.addElement(_pmbMetaData.getClassName());
        br.addItem("Property");
        br.addElement(_propertyName);
        br.addItem("Option");
        br.addElement(OPTION_PREFIX + classificationName + OPTION_SUFFIX);
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg);
    }

    protected String extractClassificationNameFromOption(boolean check) {
        if (_extracted) {
            return _specifiedValue;
        }
        _extracted = true;
        final String pmbMetaDataPropertyOption = getPmbMetaDataPropertyOption();
        if (pmbMetaDataPropertyOption == null) {
            if (check) {
                String msg = "The property name didn't have its option:";
                msg = msg + " " + _pmbMetaData.getClassName() + "." + _propertyName;
                throw new IllegalStateException(msg);
            } else {
                return null;
            }
        }
        String option = pmbMetaDataPropertyOption.trim();
        {
            if (option.trim().length() == 0) {
                if (check) {
                    String msg = "The option of the property name should not be empty:";
                    msg = msg + " property=" + _pmbMetaData.getClassName() + "." + _propertyName;
                    throw new IllegalStateException(msg);
                } else {
                    return null;
                }
            }
            final List<String> splitOption = splitOption(option);
            String firstOption = null;
            for (String element : splitOption) {
                if (element.startsWith(OPTION_PREFIX) && element.endsWith(OPTION_SUFFIX)) {
                    firstOption = element;
                    break;
                }
            }
            if (firstOption == null) {
                if (check) {
                    String msg = "The option of class name and the property name should be 'cls(xxx)':";
                    msg = msg + " property=" + _pmbMetaData.getClassName() + "." + _propertyName + ":" + option;
                    throw new IllegalStateException(msg);
                } else {
                    return null;
                }
            }
            option = firstOption;
        }
        final int clsIdx = OPTION_PREFIX.length();
        final int clsEndIdx = option.length() - OPTION_SUFFIX.length();
        try {
            _specifiedValue = option.substring(clsIdx, clsEndIdx);
        } catch (StringIndexOutOfBoundsException e) {
            final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
            br.addNotice("The classification option for the parameter comment was invalid.");
            br.addItem("ParameterBean");
            br.addElement(_pmbMetaData.getClassName());
            br.addItem("Property");
            br.addElement(_propertyName);
            br.addItem("Option");
            br.addElement(option);
            br.addItem("Exception");
            br.addElement(e.getClass());
            br.addElement(e.getMessage());
            br.addElement("{" + option + "}.substring(" + clsIdx + ", " + clsEndIdx + ")");
            final String msg = br.buildExceptionMessage();
            throw new IllegalStateException(msg, e);
        }
        return _specifiedValue;
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    protected String getPmbMetaDataPropertyOption() {
        return _pmbMetaDataPropertyOptionFinder.findPmbMetaDataPropertyOption(_propertyName);
    }

    protected List<String> splitOption(String option) {
        return DfPmbPropertyOptionFinder.splitOption(option);
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String ln() {
        return DBFluteSystem.getBasicLn();
    }
}
