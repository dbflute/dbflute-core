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
package org.seasar.dbflute.s2dao.metadata;

import org.seasar.dbflute.dbmeta.info.ColumnInfo;
import org.seasar.dbflute.dbmeta.name.ColumnSqlName;
import org.seasar.dbflute.helper.beans.DfPropertyAccessor;
import org.seasar.dbflute.jdbc.ValueType;

/**
 * @author modified by jflute (originated in S2Dao)
 */
public interface TnPropertyMapping {

    /**
     * Get the accessor of the property. (basically by reflection)
     * @return The accessor instance. (NotNull)
     */
    DfPropertyAccessor getPropertyAccessor();

    /**
     * Get the value type.
     * @return The value type instance. (NotNull)
     */
    ValueType getValueType();

    /**
     * @return The property name of column. (NotNull)
     */
    String getPropertyName();

    /**
     * @return The DB name of column. (NotNull)
     */
    String getColumnDbName();

    /**
     * @return The SQL name of column. (NotNull)
     */
    ColumnSqlName getColumnSqlName();

    /**
     * @return The column info of DBMeta for the entity. (NullAllowed)
     */
    ColumnInfo getEntityColumnInfo(); // for DBFlute original mapping
}