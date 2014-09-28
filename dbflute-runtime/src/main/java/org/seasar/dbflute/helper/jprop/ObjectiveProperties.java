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

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.DfTypeUtil;
import org.seasar.dbflute.util.DfTypeUtil.ParseDateException;
import org.seasar.dbflute.util.DfTypeUtil.ParseDateNumberFormatException;
import org.seasar.dbflute.util.DfTypeUtil.ParseDateOutOfCalendarException;

/**
 * The properties object that can be objective. <br />
 * You can make properties that extends other (super) properties.
 * @author jflute
 * @since 1.0.1 (2012/12/30 Sunday)
 */
public class ObjectiveProperties {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The resource path of (base-point) properties loaded by class loader. (NotNull) */
    protected final String _resourcePath;

    // -----------------------------------------------------
    //                                                Option
    //                                                ------
    /** The list of resource path of extends properties loaded by class loader. (NotNull, EmptyAllowed) */
    protected List<String> _extendsResourcePathList = DfCollectionUtil.newArrayListSized(4);

    /** Does it check the implicit override property? */
    protected boolean _checkImplicitOverride;

    /** The encoding for stream to the properties file. (NullAllowed: if nul, use default encoding) */
    protected String _streamEncoding; // used if set

    // -----------------------------------------------------
    //                                              Contents
    //                                              --------
    /** The result of java properties reading. (NotNull: after loading) */
    protected JavaPropertiesResult _javaPropertiesResult;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * @param resourcePath The path of the property as resource for this class loader. (NotNull)
     */
    public ObjectiveProperties(String resourcePath) {
        _resourcePath = resourcePath;
    }

    // -----------------------------------------------------
    //                                                Option
    //                                                ------
    /**
     * Set the properties paths to extends them.
     * @param extendsResourcePaths The varying array of path for resources to extends. (NotNull)
     * @return this. (NotNull)
     */
    public ObjectiveProperties extendsProperties(String... extendsResourcePaths) {
        if (extendsResourcePaths != null && extendsResourcePaths.length > 0) {
            for (String extendsResourcePath : extendsResourcePaths) {
                _extendsResourcePathList.add(extendsResourcePath);
            }
        }
        return this;
    }

    /**
     * Enable implicit override check. <br />
     * If the property existing in super-properties is set in sub-properties implicitly,
     * an exception is thrown (you can detect the wrong situation).
     * You can override by override annotation in the line comment like this:
     * <pre>
     * # @Override here is comment area, you can write about the property
     * foo.bar = qux
     * </pre>
     * And if the property with override annotation but no existence in super-properties,
     * an exception is thrown (you can detect the wrong situation).
     * @return this. (NotNull)
     */
    public ObjectiveProperties checkImplicitOverride() {
        _checkImplicitOverride = true;
        return this;
    }

    /**
     * Encode the stream to the properties file as UTF-8. <br />
     * If not use this, encoded as default encoding.
     * @return this. (NotNull)
     */
    public ObjectiveProperties encodeAsUTF8() {
        _streamEncoding = "UTF-8";
        return this;
    }

    // ===================================================================================
    //                                                                     Load Properties
    //                                                                     ===============
    /**
     * Load properties. <br />
     * You can get properties after loading.
     * @return this. (NotNull)
     */
    public ObjectiveProperties load() {
        final String title = toTitle(_resourcePath);
        final JavaPropertiesReader reader = createJavaPropertiesReader(title);
        prepareExtendsProperties(reader);
        if (_checkImplicitOverride) {
            reader.checkImplicitOverride();
        }
        if (_streamEncoding != null) {
            reader.encodeAs(_streamEncoding);
        }
        _javaPropertiesResult = reader.read();
        return this;
    }

    protected JavaPropertiesReader createJavaPropertiesReader(final String title) {
        return new JavaPropertiesReader(title, createJavaPropertiesStreamProvider());
    }

    protected JavaPropertiesStreamProvider createJavaPropertiesStreamProvider() {
        return new JavaPropertiesStreamProvider() {
            public InputStream provideStream() throws IOException {
                return toStream(_resourcePath);
            }
        };
    }

    protected void prepareExtendsProperties(final JavaPropertiesReader reader) {
        for (final String extendsResourcePath : _extendsResourcePathList) {
            final String title = toTitle(extendsResourcePath);
            reader.extendsProperties(title, new JavaPropertiesStreamProvider() {
                public InputStream provideStream() throws IOException {
                    return toStream(extendsResourcePath);
                }
            });
        }
    }

    protected String toTitle(String path) {
        return DfTypeUtil.toClassTitle(this) + ":" + path;
    }

    protected InputStream toStream(String resourcePath) {
        return getClass().getClassLoader().getResourceAsStream(resourcePath);
    }

    // ===================================================================================
    //                                                                        Get Property
    //                                                                        ============
    /**
     * Get the value of property as {@link String}.
     * @param propertyKey The key of the property. (NotNull)
     * @return The value of found property. (NullAllowed: if null, not found)
     */
    public String get(String propertyKey) {
        final JavaPropertiesProperty property = _javaPropertiesResult.getProperty(propertyKey);
        return property != null ? property.getPropertyValue() : null;
    }

    /**
     * Get the value of property as {@link Integer}.
     * @param propertyKey The key of the property. (NotNull)
     * @return The value of found property. (NullAllowed: if null, not found)
     * @throws NumberFormatException When the property is not integer.
     */
    public Integer getAsInteger(String propertyKey) {
        final String value = get(propertyKey);
        return value != null ? DfTypeUtil.toInteger(value) : null;
    }

    /**
     * Get the value of property as {@link Long}.
     * @param propertyKey The key of the property. (NotNull)
     * @return The value of found property. (NullAllowed: if null, not found)
     * @throws NumberFormatException When the property is not long.
     */
    public Long getAsLong(String propertyKey) {
        final String value = get(propertyKey);
        return value != null ? DfTypeUtil.toLong(value) : null;
    }

    /**
     * Get the value of property as {@link BigDecimal}.
     * @param propertyKey The key of the property. (NotNull)
     * @return The value of found property. (NullAllowed: if null, not found)
     * @throws NumberFormatException When the property is not decimal.
     */
    public BigDecimal getAsDecimal(String propertyKey) {
        final String value = get(propertyKey);
        return value != null ? DfTypeUtil.toBigDecimal(value) : null;
    }

    /**
     * Get the value of property as {@link Date}.
     * @param propertyKey The key of the property. (NotNull)
     * @return The value of found property. (NullAllowed: if null, not found)
     * @throws ParseDateException When it failed to parse the string to date.
     * @throws ParseDateNumberFormatException When it failed to format the elements as number.
     * @throws ParseDateOutOfCalendarException When the date was out of calendar. (if BC, not thrown)
     */
    public Date getAsDate(String propertyKey) {
        final String value = get(propertyKey);
        return value != null ? DfTypeUtil.toDate(value) : null;
    }

    /**
     * Is the property true?
     * @param propertyKey The key of the property which is boolean type. (NotNull)
     * @return The determination, true or false. (if the property can be true, returns true)
     */
    public boolean is(String propertyKey) {
        final String value = get(propertyKey);
        return value != null && value.trim().equalsIgnoreCase("true");
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ObjectiveProperties)) {
            return false;
        }
        final ObjectiveProperties another = (ObjectiveProperties) obj;
        if (_javaPropertiesResult == null) {
            return another._javaPropertiesResult == null;
        }
        return _javaPropertiesResult.equals(another._javaPropertiesResult);
    }

    @Override
    public int hashCode() {
        return _javaPropertiesResult != null ? _javaPropertiesResult.hashCode() : 0;
    }

    @Override
    public String toString() {
        return DfTypeUtil.toClassTitle(this) + ":{" + _javaPropertiesResult + "}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    /**
     * Get the resource path of (base-point) properties loaded by class loader. (NotNull)
     * @return The path string for the resource. (NotNull)
     */
    public String getResourcePath() {
        return _resourcePath;
    }

    /**
     * Get the list of resource path of extends properties loaded by class loader.
     * @return The list of path string for the resource. (NotNull, EmptyAllowed)
     */
    public List<String> getExtendsResourcePathList() {
        return _extendsResourcePathList;
    }

    /**
     * Does it check the implicit override property?
     * @return The determination, true or false.
     */
    public boolean isCheckImplicitOverride() {
        return _checkImplicitOverride;
    }

    /**
     * Get the result of java properties reading.
     * @return The result of java properties reading. (NotNull: after loading)
     */
    public JavaPropertiesResult getJavaPropertiesResult() {
        return _javaPropertiesResult;
    }
}
