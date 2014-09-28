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
package org.seasar.dbflute.bhv.core.command;

import org.seasar.dbflute.DBDef;
import org.seasar.dbflute.cbean.ConditionBean;
import org.seasar.dbflute.dbmeta.DBMetaProvider;
import org.seasar.dbflute.outsidesql.OutsideSqlContext;
import org.seasar.dbflute.outsidesql.OutsideSqlFilter;
import org.seasar.dbflute.outsidesql.OutsideSqlOption;
import org.seasar.dbflute.outsidesql.factory.OutsideSqlContextFactory;
import org.seasar.dbflute.resource.ResourceContext;

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
    /** The path of outside-SQL. (Required) */
    protected String _outsideSqlPath;

    /** The parameter-bean. (Required to set, but Nullable) */
    protected Object _parameterBean;

    /** The option of outside-SQL. (Required) */
    protected OutsideSqlOption _outsideSqlOption;

    /** The current database definition. (Required) */
    protected DBDef _currentDBDef;

    /** The factory of outside-SQL context. (NotNull) */
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

    protected void setupOutsideSqlContextProperty(OutsideSqlContext outsideSqlContext) {
        final String path = _outsideSqlPath;
        final Object pmb = _parameterBean;
        final OutsideSqlOption option = _outsideSqlOption;
        outsideSqlContext.setOutsideSqlPath(path);
        outsideSqlContext.setParameterBean(pmb);
        outsideSqlContext.setResultType(getResultType());
        outsideSqlContext.setMethodName(getCommandName());
        outsideSqlContext.setStatementConfig(option.getStatementConfig());
        outsideSqlContext.setTableDbName(option.getTableDbName());
        outsideSqlContext.setOffsetByCursorForcedly(option.isAutoPaging());
        outsideSqlContext.setLimitByCursorForcedly(option.isAutoPaging());
        outsideSqlContext.setOutsideSqlFilter(_outsideSqlFilter);
        outsideSqlContext.setRemoveBlockComment(option.isRemoveBlockComment());
        outsideSqlContext.setRemoveLineComment(option.isRemoveLineComment());
        outsideSqlContext.setFormatSql(option.isFormatSql());
        outsideSqlContext.setInternalDebug(ResourceContext.isInternalDebug());
        outsideSqlContext.setupBehaviorQueryPathIfNeeds();
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
