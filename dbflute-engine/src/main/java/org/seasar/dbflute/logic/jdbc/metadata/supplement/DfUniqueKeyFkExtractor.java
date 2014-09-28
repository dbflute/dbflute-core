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
package org.seasar.dbflute.logic.jdbc.metadata.supplement;

import java.util.List;
import java.util.Map;

/**
 * @author jflute
 * @since 1.0.5A (2013/11/04 Monday)
 */
public interface DfUniqueKeyFkExtractor {

    /**
     * Extract unique-key FK info. (same schema only) <br />
     * @return The map of unique-key FK. map:{ tableName = map:{ fkName = list:{ columns } } }
     */
    Map<String, Map<String, List<UserUniqueFkColumn>>> extractUniqueKeyFkMap();

    public static class UserUniqueFkColumn {
        protected String _foreignKeyName;
        protected String _localTableName;
        protected String _localColumnName;
        protected String _foreignTableName;
        protected String _foreignColumnName;

        public String getForeignKeyName() {
            return _foreignKeyName;
        }

        public void setForeignKeyName(String foreignKeyName) {
            this._foreignKeyName = foreignKeyName;
        }

        public String getLocalTableName() {
            return _localTableName;
        }

        public void setLocalTableName(String localTableName) {
            this._localTableName = localTableName;
        }

        public String getLocalColumnName() {
            return _localColumnName;
        }

        public void setLocalColumnName(String localColumnName) {
            this._localColumnName = localColumnName;
        }

        public String getForeignTableName() {
            return _foreignTableName;
        }

        public void setForeignTableName(String foreignTableName) {
            this._foreignTableName = foreignTableName;
        }

        public String getForeignColumnName() {
            return _foreignColumnName;
        }

        public void setForeignColumnName(String foreignColumnName) {
            this._foreignColumnName = foreignColumnName;
        }
    }
}
