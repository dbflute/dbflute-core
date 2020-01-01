/*
 * Copyright 2014-2020 the original author or authors.
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
import java.util.List;

import org.dbflute.bhv.exception.BehaviorExceptionThrower;
import org.dbflute.s2dao.metadata.TnBeanMetaData;
import org.dbflute.s2dao.rowcreator.TnRelationRowCreator;
import org.dbflute.s2dao.rowcreator.TnRowCreator;

/**
 * @author jflute
 * @since 1.1.1 (2015/12/30 Wednesday)
 */
public class TnBeanOneResultSetHandler extends TnBeanListResultSetHandler {

    protected final Object searchKey; // for exception message

    public TnBeanOneResultSetHandler(TnBeanMetaData beanMetaData, TnRowCreator rowCreator, TnRelationRowCreator relationRowCreator,
            Object searchKey) {
        super(beanMetaData, rowCreator, relationRowCreator);
        this.searchKey = searchKey;
    }

    @Override
    public Object handle(ResultSet rs) throws SQLException {
        @SuppressWarnings("unchecked")
        final List<Object> selectedList = (List<Object>) super.handle(rs);
        if (selectedList.isEmpty()) {
            return null; // wrapped as optional or checked in behavior
        }
        if (selectedList.size() >= 2) { // basically no coming here because of checking safety size in fetch process
            throwSelectEntityDuplicatedException(String.valueOf(selectedList.size()));
        }
        return selectedList.get(0); // one size here
    }

    protected void throwSelectEntityDuplicatedException(String resultCountExp) {
        createBhvExThrower().throwSelectEntityDuplicatedException(resultCountExp, searchKey, null);
    }

    protected BehaviorExceptionThrower createBhvExThrower() {
        return new BehaviorExceptionThrower();
    }
}
