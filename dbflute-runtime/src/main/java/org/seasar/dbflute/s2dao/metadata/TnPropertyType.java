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

import org.seasar.dbflute.helper.beans.DfPropertyDesc;

/**
 * @author modified by jflute (originated in S2Dao)
 */
public interface TnPropertyType extends TnPropertyMapping {

    /**
     * Get the description object of the property.
     * @return The instance of property description. (NotNull)
     */
    DfPropertyDesc getPropertyDesc();

    boolean isPrimaryKey();

    void setPrimaryKey(boolean primaryKey);

    boolean isPersistent();

    void setPersistent(boolean persistent);
}