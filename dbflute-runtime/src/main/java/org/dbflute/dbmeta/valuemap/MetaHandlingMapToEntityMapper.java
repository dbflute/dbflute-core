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
package org.dbflute.dbmeta.valuemap;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.dbflute.Entity;
import org.dbflute.dbmeta.info.ColumnInfo;
import org.dbflute.jdbc.Classification;
import org.dbflute.util.DfReflectionUtil;
import org.dbflute.util.DfTypeUtil;
import org.dbflute.util.Srl;

/**
 * The mapping object of column value map to entity by DB meta.
 * @author jflute
 * @since 1.1.0-sp1 (2015/01/19 Monday)
 */
public class MetaHandlingMapToEntityMapper {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Map<String, ? extends Object> _valueMap;
    protected String _columnName;
    protected String _uncapPropName;
    protected String _propertyName;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public MetaHandlingMapToEntityMapper(Map<String, ? extends Object> valueMap) {
        _valueMap = valueMap;
    }

    // ===================================================================================
    //                                                                             Mapping
    //                                                                             =======
    public <ENTITY extends Entity> void mappingToEntity(ENTITY entity, Map<String, ? extends Object> columnMap,
            List<ColumnInfo> columnInfoList) {
        entity.clearModifiedInfo();
        for (ColumnInfo columnInfo : columnInfoList) {
            final String columnName = columnInfo.getColumnDbName();
            final String propertyName = columnInfo.getPropertyName();
            final String uncapPropName = initUncap(propertyName);
            final Class<?> nativeType = columnInfo.getObjectNativeType();
            if (init(columnName, uncapPropName, propertyName)) {
                final Object value;
                if (String.class.isAssignableFrom(nativeType)) {
                    value = analyzeString(nativeType);
                } else if (Number.class.isAssignableFrom(nativeType)) {
                    value = analyzeNumber(nativeType);
                } else if (LocalDate.class.isAssignableFrom(nativeType)) {
                    value = analyzeLocalDate(nativeType);
                } else if (LocalDateTime.class.isAssignableFrom(nativeType)) {
                    value = analyzeLocalDateTime(nativeType);
                } else if (LocalTime.class.isAssignableFrom(nativeType)) {
                    value = analyzeLocalTime(nativeType);
                } else if (Date.class.isAssignableFrom(nativeType)) {
                    value = analyzeDate(nativeType);
                } else if (Boolean.class.isAssignableFrom(nativeType)) {
                    value = analyzeBoolean(nativeType);
                } else if (byte[].class.isAssignableFrom(nativeType)) {
                    value = analyzeBinary(nativeType);
                } else if (UUID.class.isAssignableFrom(nativeType)) {
                    value = analyzeUUID(nativeType);
                } else {
                    value = analyzeOther(nativeType);
                }
                columnInfo.write(entity, value);
            }
        }
    }

    protected final String initUncap(String str) {
        return Srl.initUncap(str);
    }

    protected boolean init(String columnName, String uncapPropName, String propertyName) {
        _columnName = columnName;
        _uncapPropName = uncapPropName;
        _propertyName = propertyName;
        return _valueMap.containsKey(_columnName);
    }

    // ===================================================================================
    //                                                                       Analyze Value
    //                                                                       =============
    @SuppressWarnings("unchecked")
    public <PROPERTY> PROPERTY analyzeString(Class<PROPERTY> javaType) {
        return (PROPERTY) DfTypeUtil.toString(getColumnValue());
    }

    @SuppressWarnings("unchecked")
    public <PROPERTY> PROPERTY analyzeNumber(Class<PROPERTY> javaType) {
        return (PROPERTY) DfTypeUtil.toNumber(getColumnValue(), javaType);
    }

    @SuppressWarnings("unchecked")
    public <PROPERTY> PROPERTY analyzeLocalDate(Class<PROPERTY> javaType) {
        return (PROPERTY) DfTypeUtil.toLocalDate(getColumnValue());
    }

    @SuppressWarnings("unchecked")
    public <PROPERTY> PROPERTY analyzeLocalDateTime(Class<PROPERTY> javaType) {
        return (PROPERTY) DfTypeUtil.toLocalDateTime(getColumnValue());
    }

    @SuppressWarnings("unchecked")
    public <PROPERTY> PROPERTY analyzeLocalTime(Class<PROPERTY> javaType) {
        return (PROPERTY) DfTypeUtil.toLocalTime(getColumnValue());
    }

    @SuppressWarnings("unchecked")
    public <PROPERTY> PROPERTY analyzeDate(Class<PROPERTY> javaType) {
        final Object obj = getColumnValue();
        if (Time.class.isAssignableFrom(javaType)) {
            return (PROPERTY) DfTypeUtil.toTime(obj);
        } else if (Timestamp.class.isAssignableFrom(javaType)) {
            return (PROPERTY) DfTypeUtil.toTimestamp(obj);
        } else {
            return (PROPERTY) DfTypeUtil.toDate(obj);
        }
    }

    @SuppressWarnings("unchecked")
    public <PROPERTY> PROPERTY analyzeBoolean(Class<PROPERTY> javaType) {
        return (PROPERTY) DfTypeUtil.toBoolean(getColumnValue());
    }

    @SuppressWarnings("unchecked")
    public <PROPERTY> PROPERTY analyzeBinary(Class<PROPERTY> javaType) {
        final Object obj = getColumnValue();
        if (obj == null) {
            return null;
        }
        if (obj instanceof Serializable) {
            return (PROPERTY) DfTypeUtil.toBinary((Serializable) obj);
        }
        throw new UnsupportedOperationException("unsupported binary type: " + obj.getClass());
    }

    @SuppressWarnings("unchecked")
    public <PROPERTY> PROPERTY analyzeUUID(Class<PROPERTY> javaType) {
        return (PROPERTY) DfTypeUtil.toUUID(getColumnValue());
    }

    @SuppressWarnings("unchecked")
    public <PROPERTY> PROPERTY analyzeOther(Class<PROPERTY> javaType) {
        final Object obj = getColumnValue();
        if (obj == null) {
            return null;
        }
        if (Classification.class.isAssignableFrom(javaType)) {
            final Class<?>[] argTypes = new Class[] { Object.class };
            final Method method = DfReflectionUtil.getPublicMethod(javaType, "codeOf", argTypes);
            return (PROPERTY) DfReflectionUtil.invokeStatic(method, new Object[] { obj });
        }
        return (PROPERTY) obj;
    }

    protected Object getColumnValue() {
        final Object value = _valueMap.get(_columnName);
        return filterClassificationValue(value);
    }

    protected Object filterClassificationValue(Object value) {
        if (value != null && value instanceof Classification) {
            value = ((Classification) value).code();
        }
        return value;
    }
}
