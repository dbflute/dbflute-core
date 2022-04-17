/*
 * Copyright 2014-2022 the original author or authors.
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
package org.dbflute.s2dao.rshandler;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.dbflute.Entity;
import org.dbflute.bhv.core.context.ConditionBeanContext;
import org.dbflute.bhv.readable.EntityRowHandler;
import org.dbflute.cbean.ConditionBean;
import org.dbflute.s2dao.metadata.TnBeanMetaData;
import org.dbflute.s2dao.rowcreator.TnRelationRowCache;
import org.dbflute.s2dao.rowcreator.TnRelationRowCreator;
import org.dbflute.s2dao.rowcreator.TnRowCreator;

/**
 * @author modified by jflute (originated in S2Dao)
 */
public class TnBeanCursorResultSetHandler extends TnBeanListResultSetHandler {

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * @param beanMetaData Bean meta data. (NotNull)
     * @param rowCreator Row creator. (NotNull)
     * @param relationRowCreator Relation row creator. (NotNul)
     */
    public TnBeanCursorResultSetHandler(TnBeanMetaData beanMetaData, TnRowCreator rowCreator, TnRelationRowCreator relationRowCreator) {
        super(beanMetaData, rowCreator, relationRowCreator);
    }

    // ===================================================================================
    //                                                                              Handle
    //                                                                              ======
    @Override
    public Object handle(ResultSet rs) throws SQLException {
        if (!hasEntityRowHandler()) {
            String msg = "Bean cursor handling should have condition-bean!";
            throw new IllegalStateException(msg);
        }
        final EntityRowHandler<Entity> entityRowHandler = getEntityRowHandler();
        mappingBean(rs, row -> {
            if (!(row instanceof Entity)) { /* just in case */
                throwCursorRowNotEntityException(row);
            }
            entityRowHandler.handle((Entity) row);
            return !entityRowHandler.isBreakCursor(); /* continue to next records? */
        });
        return null;
    }

    protected void throwCursorRowNotEntityException(Object row) {
        String msg = "The row object should be an entity at bean cursor handling:";
        msg = msg + " row=" + (row != null ? row.getClass().getName() + ":" + row : null);
        throw new IllegalStateException(msg);
    }

    @Override
    protected TnRelationRowCache createRelationRowCache(boolean hasCB, ConditionBean cb) {
        // cursor select is for save memory so it should not cache instances
        return new TnRelationRowCache(-1, false);
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    protected boolean hasEntityRowHandler() {
        return ConditionBeanContext.isExistEntityRowHandlerOnThread();
    }

    protected EntityRowHandler<Entity> getEntityRowHandler() {
        final EntityRowHandler<? extends Entity> handlerOnThread = ConditionBeanContext.getEntityRowHandlerOnThread();
        @SuppressWarnings("unchecked")
        final EntityRowHandler<Entity> entityRowHandler = (EntityRowHandler<Entity>) handlerOnThread;
        return entityRowHandler;
    }
}
