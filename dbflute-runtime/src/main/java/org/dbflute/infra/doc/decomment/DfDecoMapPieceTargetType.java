/*
 * Copyright 2014-2017 the original author or authors.
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
package org.dbflute.infra.doc.decomment;

import static org.dbflute.util.DfTypeUtil.emptyStrings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.dbflute.exception.ClassificationNotFoundException;
import org.dbflute.jdbc.Classification;
import org.dbflute.jdbc.ClassificationCodeType;
import org.dbflute.jdbc.ClassificationMeta;
import org.dbflute.jdbc.ClassificationUndefinedHandlingType;
import org.dbflute.optional.OptionalThing;

/**
 * target type of decomment piece
 * @author cabos
 * @author jflute
 */
// done cabos delete empty lines by jflute (2017/11/11)
public enum DfDecoMapPieceTargetType implements Classification {

    // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
    // copied from intro's appcls by cabos (commented by jflute) (2017/11/11)
    // _/_/_/_/_/_/_/_/_/_/
    /** Column: piece for column */
    Column("COLUMN", "Column", emptyStrings()), // comment to avoid intellij formatter headache
    /** Table: piece for table */
    Table("TABLE", "Table", emptyStrings());

    private static final Map<String, DfDecoMapPieceTargetType> _codeClsMap = new HashMap<String, DfDecoMapPieceTargetType>();
    private static final Map<String, DfDecoMapPieceTargetType> _nameClsMap = new HashMap<String, DfDecoMapPieceTargetType>();

    static {
        for (DfDecoMapPieceTargetType value : values()) {
            _codeClsMap.put(value.code().toLowerCase(), value);
            for (String sister : value.sisterSet()) {
                _codeClsMap.put(sister.toLowerCase(), value);
            }
        }
    }

    private String _code;
    private String _alias;
    private Set<String> _sisterSet;

    private DfDecoMapPieceTargetType(String code, String alias, String[] sisters) {
        _code = code;
        _alias = alias;
        _sisterSet = Collections.unmodifiableSet(new LinkedHashSet<String>(Arrays.asList(sisters)));
    }

    public String code() {
        return _code;
    }

    public String alias() {
        return _alias;
    }

    public Set<String> sisterSet() {
        return _sisterSet;
    }

    public Map<String, Object> subItemMap() {
        return Collections.emptyMap();
    }

    public ClassificationMeta meta() {
        // TODO cabos confirm it by jflute (2017/11/11)
        return new ClassificationMeta() { // may be used by LastaFlute (in DBFlute Intro)

            @Override
            public String classificationName() {
                return DfDecoMapPieceTargetType.class.getSimpleName();
            }

            @Override
            public Classification codeOf(Object code) { // null if not found
                return DfDecoMapPieceTargetType.of(code).orElse(null);
            }

            @Override
            public Classification nameOf(String name) { // null if not found
                return DfDecoMapPieceTargetType.byName(name).orElse(null);
            }

            @Override
            public List<Classification> listAll() {
                return DfDecoMapPieceTargetType.listAll().stream().map(el -> {
                    return (Classification) el; // cannot cast directly so convert explicitly for now
                }).collect(Collectors.toList());
            }

            @Override
            public List<Classification> groupOf(String groupName) {
                return Collections.emptyList(); // empty if not found (see javadoc)
            }

            @Override
            public ClassificationCodeType codeType() {
                return ClassificationCodeType.String;
            }

            @Override
            public ClassificationUndefinedHandlingType undefinedHandlingType() {
                return ClassificationUndefinedHandlingType.EXCEPTION;
            }
        };
    }

    public boolean inGroup(String groupName) {
        return false;
    }

    /**
     * Get the classification of the code. (CaseInsensitive)
     * @param code The value of code, which is case-insensitive. (NullAllowed: if null, returns empty)
     * @return The optional classification corresponding to the code. (NotNull, EmptyAllowed: if not found, returns empty)
     */
    public static OptionalThing<DfDecoMapPieceTargetType> of(Object code) {
        if (code == null) {
            return OptionalThing.ofNullable(null, () -> {
                throw new ClassificationNotFoundException("null code specified");
            });
        }
        if (code instanceof DfDecoMapPieceTargetType) {
            return OptionalThing.of((DfDecoMapPieceTargetType) code);
        }
        if (code instanceof OptionalThing<?>) {
            return of(((OptionalThing<?>) code).orElse(null));
        }
        return OptionalThing.ofNullable(_codeClsMap.get(code.toString().toLowerCase()), () -> {
            throw new ClassificationNotFoundException("Unknown classification code: " + code);
        });
    }

    /**
     * Find the classification by the name. (CaseInsensitive)
     * @param name The string of name, which is case-insensitive. (NotNull)
     * @return The optional classification corresponding to the name. (NotNull, EmptyAllowed: if not found, returns empty)
     */
    public static OptionalThing<DfDecoMapPieceTargetType> byName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("The argument 'name' should not be null.");
        }
        return OptionalThing.ofNullable(_nameClsMap.get(name.toLowerCase()), () -> {
            throw new ClassificationNotFoundException("Unknown classification name: " + name);
        });
    }

    /**
     * <span style="color: #AD4747; font-size: 120%">Old style so use of(code).</span> <br>
     * Get the classification by the code. (CaseInsensitive)
     * @param code The value of code, which is case-insensitive. (NullAllowed: if null, returns null)
     * @return The instance of the corresponding classification to the code. (NullAllowed: if not found, returns null)
     * @deprecated use of()
     */
    public static DfDecoMapPieceTargetType codeOf(Object code) {
        if (code == null) {
            return null;
        }
        if (code instanceof DfDecoMapPieceTargetType) {
            return (DfDecoMapPieceTargetType) code;
        }
        return _codeClsMap.get(code.toString().toLowerCase());
    }

    /**
     * <span style="color: #AD4747; font-size: 120%">Old style so use byName(name).</span> <br>
     * Get the classification by the name (also called 'value' in ENUM world).
     * @param name The string of name, which is case-sensitive. (NullAllowed: if null, returns null)
     * @return The instance of the corresponding classification to the name. (NullAllowed: if not found, returns null)
     */
    public static DfDecoMapPieceTargetType nameOf(String name) {
        if (name == null) {
            return null;
        }
        try {
            return valueOf(name);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    /**
     * Get the list of all classification elements. (returns new copied list)
     * @return The snapshot list of all classification elements. (NotNull)
     */
    public static List<DfDecoMapPieceTargetType> listAll() {
        return new ArrayList<DfDecoMapPieceTargetType>(Arrays.asList(values()));
    }

    /**
     * Get the list of classification elements in the specified group. (returns new copied list) <br>
     * @param groupName The string of group name, which is case-insensitive. (NotNull)
     * @return The snapshot list of classification elements in the group. (NotNull, EmptyAllowed: if not found, throws exception)
     */
    public static List<DfDecoMapPieceTargetType> listByGroup(String groupName) {
        if (groupName == null) {
            throw new IllegalArgumentException("The argument 'groupName' should not be null.");
        }
        throw new ClassificationNotFoundException("Unknown classification group: DfDecoMapPieceTargetType." + groupName);
    }

    /**
     * Get the list of classification elements corresponding to the specified codes. (returns new copied list) <br>
     * @param codeList The list of plain code, which is case-insensitive. (NotNull)
     * @return The snapshot list of classification elements in the code list. (NotNull, EmptyAllowed: when empty specified)
     */
    public static List<DfDecoMapPieceTargetType> listOf(Collection<String> codeList) {
        if (codeList == null) {
            throw new IllegalArgumentException("The argument 'codeList' should not be null.");
        }
        List<DfDecoMapPieceTargetType> clsList = new ArrayList<DfDecoMapPieceTargetType>(codeList.size());
        for (String code : codeList) {
            clsList.add(of(code).get());
        }
        return clsList;
    }

    /**
     * Get the list of classification elements in the specified group. (returns new copied list) <br>
     * @param groupName The string of group name, which is case-sensitive. (NullAllowed: if null, returns empty list)
     * @return The snapshot list of classification elements in the group. (NotNull, EmptyAllowed: if the group is not found)
     */
    public static List<DfDecoMapPieceTargetType> groupOf(String groupName) {
        return new ArrayList<DfDecoMapPieceTargetType>(4);
    }

    @Override
    public String toString() {
        return code();
    }
}
