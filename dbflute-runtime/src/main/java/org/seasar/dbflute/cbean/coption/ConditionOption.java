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
package org.seasar.dbflute.cbean.coption;

import java.util.List;

import org.seasar.dbflute.cbean.chelper.HpSpecifiedColumn;
import org.seasar.dbflute.cbean.cipher.GearedCipherManager;
import org.seasar.dbflute.cbean.sqlclause.query.QueryClauseArranger;
import org.seasar.dbflute.dbway.ExtensionOperand;
import org.seasar.dbflute.dbway.StringConnector;

/**
 * The interface of condition-option.
 * @author jflute
 */
public interface ConditionOption {

    /**
     * Get the string expression of rear option.
     * @return The string for rear option. (NotNull, EmptyAllowed)
     */
    String getRearOption();

    /**
     * Does the option have compound columns?
     * @return The determination, true or false.
     */
    boolean hasCompoundColumn();

    /**
     * Get the list of compound columns.
     * @return The list of specified column. (NotNull, EmptyAllowed)
     */
    List<HpSpecifiedColumn> getCompoundColumnList();

    /**
     * Does the option have string connector basically for compound columns?
     * @return The determination, true or false.
     */
    boolean hasStringConnector();

    /**
     * Get the string connector basically for compound columns?
     * @return The object of string connector. (NullAllowed)
     */
    StringConnector getStringConnector();

    /**
     * Get the extension operand.
     * @return The object of the extension operand. (NullAllowed)
     */
    ExtensionOperand getExtensionOperand();

    /**
     * Get the arranger of query clause.
     * @return The object of the arranger. (NullAllowed)
     */
    QueryClauseArranger getWhereClauseArranger();

    /**
     * Get the manager of geared cipher. (basically for compound columns)
     * @return The manager of geared cipher. (NullAllowed)
     */
    GearedCipherManager getGearedCipherManager();
}
