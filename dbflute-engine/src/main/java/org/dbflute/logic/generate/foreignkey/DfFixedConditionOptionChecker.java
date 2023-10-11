/*
 * Copyright 2014-2023 the original author or authors.
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
package org.dbflute.logic.generate.foreignkey;

import java.util.Map;

import org.apache.torque.engine.database.model.ForeignKey;
import org.dbflute.cbean.chelper.HpFixedConditionQueryResolver;
import org.dbflute.exception.DfFixedConditionOptionConstraintFailureException;
import org.dbflute.helper.message.ExceptionMessageBuilder;

/**
 * @author jflute
 * @since 1.1.9 (2018/10/05 Friday)
 */
public class DfFixedConditionOptionChecker {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final ForeignKey _foreignKey;
    protected final String _fixedCondition;
    protected final Map<String, String> _dynamicFixedConditionMap;
    protected final boolean _fixedInline;
    protected final boolean _fixedReferrer;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfFixedConditionOptionChecker(ForeignKey foreignKey, String fixedCondition, Map<String, String> dynamicFixedConditionMap,
            boolean fixedInline, boolean fixedReferrer) {
        _foreignKey = foreignKey;
        _fixedCondition = fixedCondition;
        _dynamicFixedConditionMap = dynamicFixedConditionMap;
        _fixedInline = fixedInline;
        _fixedReferrer = fixedReferrer;
    }

    // ===================================================================================
    //                                                                         Constraints
    //                                                                         ===========
    public void checkOptionConstraints() {
        if (_fixedInline) {
            final String localAliasMark = HpFixedConditionQueryResolver.LOCAL_ALIAS_MARK;
            if (_fixedCondition != null && _fixedCondition.contains(localAliasMark)) {
                String msg = "fixedInline with " + localAliasMark + " is not available.";
                throwFixedConditionOptionConstraintFailureException(msg);
            }
        }
        if (_fixedReferrer) {
            if (_fixedInline) {
                String msg = "fixedReferrer with fixedInline is not available.";
                throwFixedConditionOptionConstraintFailureException(msg);
            }
            if (!_dynamicFixedConditionMap.isEmpty()) {
                String msg = "fixedReferrer with bind variables is not available.";
                throwFixedConditionOptionConstraintFailureException(msg);
            }
            final String foreignAliasMark = HpFixedConditionQueryResolver.FOREIGN_ALIAS_MARK;
            if (_fixedCondition != null && _fixedCondition.contains(foreignAliasMark)) {
                String msg = "fixedReferrer with " + foreignAliasMark + " is not available.";
                throwFixedConditionOptionConstraintFailureException(msg);
            }
        }
    }

    protected void throwFixedConditionOptionConstraintFailureException(String notice) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice(notice);
        br.addItem("DBFlute Property");
        br.addElement("additionalForeignKeyMap.dfprop");
        br.addItem("FK Name");
        br.addElement(_foreignKey.getName());
        br.addItem("fixedCondition");
        br.addElement(_fixedCondition);
        br.addItem("fixedSuffix");
        br.addElement(_foreignKey.getFixedSuffix());
        br.addItem("fixedInline");
        br.addElement(_fixedInline);
        br.addItem("fixedReferrer");
        br.addElement(_fixedReferrer);
        final String msg = br.buildExceptionMessage();
        throw new DfFixedConditionOptionConstraintFailureException(msg);
    }
}
