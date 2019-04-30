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
package org.dbflute.s2dao.rowcreator;

import java.util.Set;

import org.dbflute.dbmeta.info.ColumnInfo;

/**
 * @author jflute
 */
public interface TnRelationSelector {

    /**
     * Does the mapping has non-limit of relation nest level?
     * @return The determination, true or false.
     */
    boolean isNonLimitMapping();

    /**
     * Is the relation (of current relation No suffix) non-selected?
     * @param relationNoSuffix The suffix of relation No, same as foreign relation path. (NotNull)  
     * @return The determination, true or false.
     */
    boolean isNonSelectedRelation(String relationNoSuffix);

    /**
     * Does the relation non-connect to selected next relation?
     * @param relationNoSuffix The suffix of relation No, same as foreign relation path. (NotNull)  
     * @return The determination, true or false.
     */
    boolean isNonSelectedNextConnectingRelation(String relationNoSuffix);

    /**
     * Can it use the relation cache for entity mapping?
     * @param relationNoSuffix The suffix of relation no. e.g. _1_3 (NotNull)
     * @return The determination, true or false.
     */
    boolean canUseRelationCache(String relationNoSuffix); // sorry, emergency implementation

    /**
     * Can the access to non-specified column be checked?
     * @param relationNoSuffix The suffix of relation no. e.g. _1_3 (NotNull)
     * @return The determination, true or false.
     */
    boolean isNonSpecifiedColumnAccessAllowed(String relationNoSuffix); // sorry, though see the emergency implementation...

    /**
     * Does the relation use SpecifyColumn?
     * @param relationNoSuffix The suffix of relation No, same as foreign relation path. (NotNull)  
     * @return The determination, true or false.
     */
    boolean isUsingSpecifyColumnInRelation(String relationNoSuffix);

    /**
     * Get the set of column specified as null object for the relation.
     * @param relationNoSuffix The suffix of relation No, same as foreign relation path. (NotNull)  
     * @return The set of column info. (NotNull, EmptyAllowed)
     */
    Set<ColumnInfo> getRelationSpecifiedNullObjectColumnSet(String relationNoSuffix);

    /**
     * Is the selecting undefined classification code allowed?
     * @param relationNoSuffix The suffix of relation No, same as foreign relation path. (NotNull)  
     * @return The determination, true or false.
     */
    boolean isUndefinedClassificationSelectAllowed(String relationNoSuffix);

    /**
     * Is the handling of column null object enabled?
     * @param relationNoSuffix The suffix of relation No, same as foreign relation path. (NotNull)  
     * @return The determination, true or false.
     */
    boolean isColumnNullObjectEnabled(String relationNoSuffix);
}
