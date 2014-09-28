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
package org.seasar.dbflute.outsidesql.irregular;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.seasar.dbflute.jdbc.CursorHandler;
import org.seasar.dbflute.s2dao.rshandler.TnMapListResultSetHandler;

/**
 * The cursor handler returning list for map. <br />
 * Normally it should not be used. <br />
 * Basically only for direct SQL when it cannot be helped.
 * @author jflute
 * @since 1.0.5F (2014/05/12 Monday)
 */
public class IrgMapListCursorHandler implements CursorHandler {

    public Object handle(ResultSet rs) throws SQLException {
        final TnMapListResultSetHandler rsHandler = createMapListResultSetHandler();
        @SuppressWarnings("unchecked")
        final List<Map<String, Object>> resultList = (List<Map<String, Object>>) rsHandler.handle(rs);
        return resultList;
    }

    protected TnMapListResultSetHandler createMapListResultSetHandler() {
        return new TnMapListResultSetHandler();
    }
}
