/*
 * Copyright 2014-2015 the original author or authors.
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
package org.dbflute.bhv.core.command;

import org.dbflute.bhv.core.context.ResourceContext;
import org.dbflute.cbean.ConditionBean;
import org.dbflute.dbmeta.DBMetaProvider;
import org.dbflute.dbway.DBDef;
import org.dbflute.outsidesql.OutsideSqlContext;
import org.dbflute.outsidesql.OutsideSqlFilter;
import org.dbflute.outsidesql.OutsideSqlOption;
import org.dbflute.outsidesql.factory.OutsideSqlContextFactory;

/**
 * @author jflute
 * @param <RESULT> The type of result.
 */
public abstract class AbstractOutsideSqlCommand<RESULT> extends AbstractBehaviorCommand<RESULT> {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                     Basic Information
    //                                     -----------------
    /** The path of outside-SQL. (NotNull: after initialization) */
    protected String _outsideSqlPath;

    /** The parameter-bean. (NullAllowed) */
    protected Object _parameterBean;

    /** The option of outside-SQL. (NotNull: after initialization) */
    protected OutsideSqlOption _outsideSqlOption;

    /** The current database definition. (NotNull: after initialization) */
    protected DBDef _currentDBDef;

    /** The factory of outside-SQL context. (NotNull: after initialization) */
    protected OutsideSqlContextFactory _outsideSqlContextFactory;

    /** The filter of outside-SQL. (NullAllowed) */
    protected OutsideSqlFilter _outsideSqlFilter;

    // ===================================================================================
    //                                                                  Detail Information
    //                                                                  ==================
    public boolean isConditionBean() {
        return false;
    }

    public boolean isOutsideSql() {
        return true;
    }

    public boolean isSelectCount() {
        return false; // fixed false because of unknown
    }

    public boolean isSelectCursor() {
        return false; // as default (selectCursor() should override this)
    }

    public boolean isInsert() {
        return false; // fixed false because of unknown
    }

    public boolean isUpdate() {
        return false; // fixed false because of unknown
    }

    public boolean isDelete() {
        return false; // fixed false because of unknown
    }

    // ===================================================================================
    //                                                                Argument Information
    //                                                                ====================
    public ConditionBean getConditionBean() {
        return null;
    }

    public String getOutsideSqlPath() {
        return _outsideSqlPath;
    }

    public Object getParameterBean() {
        return _parameterBean;
    }

    public OutsideSqlOption getOutsideSqlOption() {
        return _outsideSqlOption;
    }

    // ===================================================================================
    //                                                                  OutsideSql Element
    //                                                                  ==================
    protected OutsideSqlContext createOutsideSqlContext() {
        final DBMetaProvider dbmetaProvider = ResourceContext.dbmetaProvider();
        final String outsideSqlPackage = ResourceContext.getOutsideSqlPackage();
        final OutsideSqlContext context = _outsideSqlContextFactory.createContext(dbmetaProvider, outsideSqlPackage);
        setupOutsideSqlContextProperty(context);
        context.setupBehaviorQueryPathIfNeeds();
        return context;
    }

    protected void setupOutsideSqlContextProperty(OutsideSqlContext context) {
        final String path = _outsideSqlPath;
        final Object pmb = _parameterBean;
        final OutsideSqlOption option = _outsideSqlOption;
        context.setOutsideSqlPath(path);
        context.setParameterBean(pmb);
        context.setResultType(getResultType());
        context.setMethodName(getCommandName());
        context.setStatementConfig(option.getStatementConfig());
        context.setTableDbName(option.getTableDbName());
        context.setOffsetByCursorForcedly(option.isAutoPaging());
        context.setLimitByCursorForcedly(option.isAutoPaging());
        context.setOutsideSqlFilter(_outsideSqlFilter);
        context.setRemoveBlockComment(option.isRemoveBlockComment());
        context.setRemoveLineComment(option.isRemoveLineComment());
        context.setFormatSql(option.isFormatSql());
        context.setNonSpecifiedColumnAccessAllowed(option.isNonSpecifiedColumnAccessAllowed());
        context.setInternalDebug(ResourceContext.isInternalDebug());
        context.setupBehaviorQueryPathIfNeeds();
    }

    protected abstract Class<?> getResultType();

    protected String buildDbmsSuffix() {
        assertOutsideSqlBasic("buildDbmsSuffix");
        final String productName = _currentDBDef.code();
        return (productName != null ? "_" + productName.toLowerCase() : "");
    }

    protected boolean isRemoveBlockComment(OutsideSqlContext context) {
        return context.isRemoveBlockComment() || needsToRemoveBlockComment();
    }

    protected boolean isRemoveLineComment(OutsideSqlContext context) {
        return context.isRemoveLineComment() || needsToRemoveLineComment();
    }

    protected boolean needsToRemoveBlockComment() {
        assertOutsideSqlBasic("needsToRemoveBlockComment");
        return !_currentDBDef.dbway().isBlockCommentSupported();
    }

    protected boolean needsToRemoveLineComment() {
        assertOutsideSqlBasic("needsToRemoveLineComment");
        return !_currentDBDef.dbway().isLineCommentSupported();
    }

    // ===================================================================================
    //                                                                       Assert Helper
    //                                                                       =============
    protected void assertOutsideSqlBasic(String methodName) {
        if (_outsideSqlPath == null) {
            throw new IllegalStateException(buildAssertMessage("_outsideSqlPath", methodName));
        }
        if (_outsideSqlOption == null) {
            throw new IllegalStateException(buildAssertMessage("_outsideSqlOption", methodName));
        }
        if (_currentDBDef == null) {
            throw new IllegalStateException(buildAssertMessage("_currentDBDef", methodName));
        }
        if (_outsideSqlContextFactory == null) {
            throw new IllegalStateException(buildAssertMessage("_outsideSqlContextFactory", methodName));
        }
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void setOutsideSqlPath(String outsideSqlPath) {
        _outsideSqlPath = outsideSqlPath;
    }

    public void setParameterBean(Object parameterBean) {
        _parameterBean = parameterBean;
    }

    public void setOutsideSqlOption(OutsideSqlOption outsideSqlOption) {
        _outsideSqlOption = outsideSqlOption;
    }

    public void setCurrentDBDef(DBDef currentDBDef) {
        _currentDBDef = currentDBDef;
    }

    public void setOutsideSqlContextFactory(OutsideSqlContextFactory outsideSqlContextFactory) {
        _outsideSqlContextFactory = outsideSqlContextFactory;
    }

    public void setOutsideSqlFilter(OutsideSqlFilter outsideSqlFilter) {
        _outsideSqlFilter = outsideSqlFilter;
    }
}
