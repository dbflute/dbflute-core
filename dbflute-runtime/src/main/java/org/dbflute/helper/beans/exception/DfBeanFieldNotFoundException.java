/*
 * Copyright 2014-2019 the original author or authors.
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
package org.dbflute.helper.beans.exception;

/**
 * @author modified by jflute (originated in S2Dao)
 */
public class DfBeanFieldNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private Class<?> targetClass;

    private String fieldName;

    public DfBeanFieldNotFoundException(Class<?> targetClass, String fieldName) {
        super("The field was not found: class=" + targetClass.getName() + " field=" + fieldName);
        this.targetClass = targetClass;
        this.fieldName = fieldName;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    public String getFieldName() {
        return fieldName;
    }
}