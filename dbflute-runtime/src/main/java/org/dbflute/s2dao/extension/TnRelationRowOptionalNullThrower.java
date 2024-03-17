/*
 * Copyright 2014-2024 the original author or authors.
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
package org.dbflute.s2dao.extension;

import java.util.Arrays;
import java.util.TimeZone;

import org.dbflute.Entity;
import org.dbflute.dbmeta.DBMeta;
import org.dbflute.exception.RelationEntityNotFoundException;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.optional.SerializableOptionalThingExceptionThrower;
import org.dbflute.system.DBFluteSystem;
import org.dbflute.twowaysql.DisplaySqlBuilder;
import org.dbflute.twowaysql.style.BoundDateDisplayStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 1.1.0-sp3 (2015/05/16 Saturday)
 */
public class TnRelationRowOptionalNullThrower extends SerializableOptionalThingExceptionThrower {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final long serialVersionUID = 1L;
    private static final Logger _log = LoggerFactory.getLogger(TnRelationRowOptionalNullThrower.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // several attributes are not null, but treated as null allowed just in case
    protected final Object _row; // not null
    protected final String _propertyName; // not null
    protected final String _invokePath; // null allowed (exists if saved)
    protected final String _sql; // not null (if CB)
    protected final Object[] _args; // not null (if CB)

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public TnRelationRowOptionalNullThrower(Object row, String propertyName, String invokePath, String sql, Object[] args) {
        _row = row;
        _propertyName = propertyName;
        _invokePath = invokePath;
        _sql = sql;
        _args = args;
    }

    // ===================================================================================
    //                                                                            Throwing
    //                                                                            ========
    @Override
    public void throwNotFoundException() {
        throwRelationEntityNotFoundException();
    }

    protected void throwRelationEntityNotFoundException() {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the relation entity.");
        br.addItem("Advice");
        br.addElement("Confirm the existence in your business rule.");
        br.addElement("If the relation entity might not exist, check it.");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    List<Member> memberList = memberBhv.selectList(cb -> {");
        br.addElement("        cb.setupSelect_MemberServiceAsOne();");
        br.addElement("    });");
        br.addElement("    for (Member member : memberList) {");
        br.addElement("        ... = member.getMemberServiceAsOne().alwaysPresent(...); // *No");
        br.addElement("    }");
        br.addElement("  (o):");
        br.addElement("    List<Member> memberList = memberBhv.selectList(cb -> {");
        br.addElement("        cb.setupSelect_MemberServiceAsOne();");
        br.addElement("    });");
        br.addElement("    for (Member member : memberList) {");
        br.addElement("        member.getMemberServiceAsOne().ifPresent(service -> {  // OK");
        br.addElement("            ... = service.getServicePointCount();");
        br.addElement("        });");
        br.addElement("    }");
        br.addItem("Your Operation");
        final String localTable;
        final String localSuffix;
        if (_row instanceof Entity) { // basically here
            final Entity entity = ((Entity) _row);
            final DBMeta dbmeta = entity.asDBMeta();
            localTable = dbmeta.getTableDispName();
            localSuffix = dbmeta.extractPrimaryKeyMap(entity).toString();
        } else {
            localTable = _row != null ? _row.getClass().getSimpleName() : null; // just in case
            localSuffix = "{" + _row + "}";
        }
        br.addElement(localTable + ":" + localSuffix + " => " + _propertyName);
        if (_row instanceof Entity) { // basically here
            final Entity entity = ((Entity) _row);
            br.addItem("Local Entity");
            try {
                br.addElement(entity.toStringWithRelation());
            } catch (RuntimeException continued) {
                final String tableDbName = entity.asTableDbName();
                final String msg = "*Failed to build string from the entity for debug: " + tableDbName;
                if (_log.isDebugEnabled()) {
                    _log.debug(msg);
                }
                br.addElement(msg);
            }
        } else {
            br.addItem("Local Entity");
            br.addElement(_row);
        }
        if (_invokePath != null) { // if saved
            br.addItem("Behavior");
            br.addElement(_invokePath);
        }
        if (_sql != null) { // basically true, just in case
            br.addItem("ConditionBean");
            try {
                final TimeZone finalTimeZone = getFinalTimeZone();
                final DisplaySqlBuilder displaySqlBuilder = createDisplaySqlBuilder(finalTimeZone);
                br.addElement(displaySqlBuilder.buildDisplaySql(_sql, _args));
                br.addElement("");
                br.addElement("(using DBFlute system time-zone: " + finalTimeZone.getID() + ")");
            } catch (RuntimeException continued) {
                final String msg = "*Failed to get display SQL from the condition-bean for debug.";
                if (_log.isDebugEnabled()) {
                    _log.debug(msg);
                }
                br.addElement(_sql);
                if (_args != null) {
                    br.addElement(Arrays.asList(_args));
                }
            }
        }
        // no way outsideSql
        //if (outsideSqlPath != null) {
        //    br.addItem("OutsideSql");
        //    br.addElement("path : " + outsideSqlPath);
        //    br.addElement("pmb  : " + parameterBean);
        //}
        final String msg = br.buildExceptionMessage();
        throw new RelationEntityNotFoundException(msg);
    }

    protected TimeZone getFinalTimeZone() {
        return DBFluteSystem.getFinalTimeZone();
    }

    protected DisplaySqlBuilder createDisplaySqlBuilder(TimeZone finalTimeZone) {
        return new DisplaySqlBuilder(createBoundDateDisplayStyle(finalTimeZone));
    }

    protected BoundDateDisplayStyle createBoundDateDisplayStyle(TimeZone finalTimeZone) {
        // BoundDateDisplayStyle (from CB) cannot be serializable so use default
        final String datePattern = "yyyy/MM/dd";
        final String timestampPattern = "yyyy/MM/dd HH:mm:ss.SSS";
        final String timePattern = "HH:mm:ss";
        return new BoundDateDisplayStyle(datePattern, timestampPattern, timePattern, () -> {
            return finalTimeZone;
        });
    }
}
