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
package org.dbflute.bhv.core.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dbflute.Entity;
import org.dbflute.cbean.ConditionBean;
import org.dbflute.dbmeta.DBMeta;
import org.dbflute.dbmeta.info.ColumnInfo;
import org.dbflute.outsidesql.OutsideSqlOption;
import org.dbflute.s2dao.metadata.TnBeanMetaData;
import org.dbflute.s2dao.metadata.TnPropertyType;
import org.dbflute.util.DfTypeUtil;

/**
 * @author jflute
 */
public abstract class AbstractBatchUpdateCommand extends AbstractAllBehaviorCommand<int[]> {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The type of entity. (Required) */
    protected Class<? extends Entity> _entityType;

    /** The instance of entity list. (Required) */
    protected List<? extends Entity> _entityList;

    // ===================================================================================
    //                                                                   Basic Information
    //                                                                   =================
    public Class<?> getCommandReturnType() {
        return int[].class;
    }

    // ===================================================================================
    //                                                                  Detail Information
    //                                                                  ==================
    public boolean isConditionBean() {
        return false;
    }

    public boolean isOutsideSql() {
        return false;
    }

    public boolean isProcedure() {
        return false;
    }

    public boolean isSelect() {
        return false;
    }

    public boolean isSelectCount() {
        return false;
    }

    public boolean isSelectCursor() {
        return false;
    }

    public boolean isInsert() {
        return false; // as default
    }

    public boolean isUpdate() {
        return false; // as default
    }

    public boolean isDelete() {
        return false; // as default
    }

    public boolean isEntityUpdateFamily() {
        return false;
    }

    public boolean isBatchUpdateFamily() {
        return true; // yes!
    }

    public boolean isQueryUpdateFamily() {
        return false;
    }

    // ===================================================================================
    //                                                                             Factory
    //                                                                             =======
    // -----------------------------------------------------
    //                                          BeanMetaData
    //                                          ------------
    protected TnBeanMetaData createBeanMetaData() {
        return _beanMetaDataFactory.createBeanMetaData(_entityType);
    }

    // ===================================================================================
    //                                                                    Process Callback
    //                                                                    ================
    public void beforeGettingSqlExecution() {
    }

    public void afterExecuting() {
    }

    // ===================================================================================
    //                                                               SqlExecution Handling
    //                                                               =====================
    public String buildSqlExecutionKey() {
        assertStatus("buildSqlExecutionKey");
        final String entityName = DfTypeUtil.toClassTitle(_entityType);
        return _tableDbName + ":" + getCommandName() + "(List<" + entityName + ">)";
    }

    public Object[] getSqlExecutionArgument() {
        assertStatus("getSqlExecutionArgument");
        return doGetSqlExecutionArgument();
    }

    protected abstract Object[] doGetSqlExecutionArgument();

    // ===================================================================================
    //                                                                Argument Information
    //                                                                ====================
    public ConditionBean getConditionBean() {
        return null;
    }

    public Entity getEntity() {
        return null; // because of non one entity here
    }

    public List<Entity> getEntityList() {
        return Collections.unmodifiableList(_entityList);
    }

    public String getOutsideSqlPath() {
        return null;
    }

    public String getParameterBean() {
        return null;
    }

    public OutsideSqlOption getOutsideSqlOption() {
        return null;
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    /**
     * Find DB meta. <br>
     * Basically this method should be called when initializing only.
     * @return DB meta. (NullAllowed: If the entity does not its DB meta)
     */
    protected DBMeta findDBMeta() {
        // /- - - - - - - - - - - - - - - - - - - - - - - - - - - - 
        // Cannot use the handler of DBMeta instance
        // because the customize-entity is contained to find here.
        // - - - - - - - - - -/
        //DBMetaInstanceHandler.findDBMeta(_tableDbName);

        final Class<?> beanType = _entityType;
        if (beanType == null) {
            return null;
        }
        if (!Entity.class.isAssignableFrom(beanType)) {
            return null;
        }
        final Entity entity;
        try {
            entity = (Entity) beanType.newInstance();
        } catch (InstantiationException e) {
            throw new IllegalStateException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
        return entity.asDBMeta();
    }

    /**
     * Get persistent property names. <br>
     * Basically this method should be called when initializing only.
     * @param bmd The bean meta data. (NotNull)
     * @return Persistent property names. (NotNull)
     */
    protected String[] getPersistentPropertyNames(TnBeanMetaData bmd) {
        final DBMeta dbmeta = findDBMeta();
        if (dbmeta != null) {
            final List<ColumnInfo> columnInfoList = dbmeta.getColumnInfoList();
            final List<String> propertyNameList = new ArrayList<String>();
            for (ColumnInfo columnInfo : columnInfoList) {
                propertyNameList.add(columnInfo.getPropertyName());
            }
            return propertyNameList.toArray(new String[] {});
        } else {
            // when the entity does not have its DB meta.
            return createNonOrderedPropertyNames(bmd);
        }
    }

    private String[] createNonOrderedPropertyNames(TnBeanMetaData bmd) {
        final List<String> propertyNameList = new ArrayList<String>();
        final List<TnPropertyType> ptList = bmd.getPropertyTypeList();
        for (TnPropertyType pt : ptList) {
            if (pt.isPersistent()) {
                propertyNameList.add(pt.getPropertyName());
            }
        }
        return propertyNameList.toArray(new String[propertyNameList.size()]);
    }

    // ===================================================================================
    //                                                                       Assert Helper
    //                                                                       =============
    protected void assertStatus(String methodName) {
        assertBasicProperty(methodName);
        assertComponentProperty(methodName);
        if (_entityType == null) {
            throw new IllegalStateException(buildAssertMessage("_entityType", methodName));
        }
        if (_entityList == null) {
            throw new IllegalStateException(buildAssertMessage("_entityList", methodName));
        }
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void setEntityType(Class<? extends Entity> entityType) {
        _entityType = entityType;
    }

    public void setEntityList(List<? extends Entity> entityList) {
        _entityList = entityList;
    }
}
