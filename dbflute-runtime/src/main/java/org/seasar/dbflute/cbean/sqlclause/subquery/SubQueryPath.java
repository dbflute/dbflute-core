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
package org.seasar.dbflute.cbean.sqlclause.subquery;

import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 * @since 0.9.7.2 (2010/06/20 Sunday)
 */
public class SubQueryPath {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _subQueryPath;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * @param subQueryPath The property path of sub-query. (NotNull)
     */
    public SubQueryPath(String subQueryPath) {
        _subQueryPath = subQueryPath;
    }

    // ===================================================================================
    //                                                                   Location Resolver
    //                                                                   =================
    public String resolveParameterLocationPath(String clause) {
        return replaceString(clause, "/*pmb.conditionQuery.", "/*pmb." + _subQueryPath + ".");
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected final String replaceString(String text, String fromText, String toText) {
        return Srl.replace(text, fromText, toText);
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public int hashCode() {
        return _subQueryPath.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof SubQueryPath)) {
            return false;
        }
        final SubQueryPath target = (SubQueryPath) obj;
        return _subQueryPath.equals(target.toString());
    }

    @Override
    public String toString() {
        return _subQueryPath;
    }
}
