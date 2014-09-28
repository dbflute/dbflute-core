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
package org.seasar.dbflute.logic.jdbc.mapping;

import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.torque.engine.database.model.TypeMap;
import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.logic.generate.language.DfLanguageDependency;
import org.seasar.dbflute.logic.generate.language.DfLanguageDependencyJava;
import org.seasar.dbflute.logic.jdbc.mapping.DfJdbcTypeMapper.DfMapperResource;
import org.seasar.dbflute.unit.core.PlainTestCase;

/**
 * @author jflute
 * @since 0.9.5 (2009/04/21 Tuesday)
 */
public class DfJdbcTypeMapperTest extends PlainTestCase {

    public void test_getColumnTorqueType_NameToTorqueTypeMap() {
        initializeEmptyProperty();
        Map<String, String> nameToTorqueTypeMap = new LinkedHashMap<String, String>();
        nameToTorqueTypeMap.put("foo", "bar");
        DfJdbcTypeMapper mapper = createMapperOracle(nameToTorqueTypeMap);
        // ## Act & Assert ##
        assertEquals("bar", mapper.getColumnJdbcType(Types.TIMESTAMP, "foo"));
        assertEquals(TypeMap.TIMESTAMP, mapper.getColumnJdbcType(Types.TIMESTAMP, "bar"));
    }

    public void test_getColumnTorqueType_Java_Oracle() {
        initializeEmptyProperty();
        Map<String, String> nameToTorqueTypeMap = new LinkedHashMap<String, String>();
        DfJdbcTypeMapper mapper = createMapperOracle(nameToTorqueTypeMap);

        // ## Act & Assert ##
        assertEquals(TypeMap.TIMESTAMP, mapper.getColumnJdbcType(Types.TIMESTAMP, "timestamp"));
        assertEquals(TypeMap.DATE, mapper.getColumnJdbcType(Types.TIMESTAMP, "date"));
        assertEquals(TypeMap.DATE, mapper.getColumnJdbcType(Types.DATE, "date"));
        assertEquals(TypeMap.VARCHAR, mapper.getColumnJdbcType(Types.VARCHAR, "varchar"));
        assertEquals(TypeMap.VARCHAR, mapper.getColumnJdbcType(Types.OTHER, "nvarchar"));
    }

    public void test_getColumnTorqueType_Java_PostgreSQL() {
        initializeEmptyProperty();
        Map<String, String> nameToTorqueTypeMap = new LinkedHashMap<String, String>();
        DfJdbcTypeMapper mapper = createMapperPostgreSQL(nameToTorqueTypeMap);

        // ## Act & Assert ##
        assertEquals(TypeMap.TIMESTAMP, mapper.getColumnJdbcType(Types.TIMESTAMP, "timestamp"));
        assertEquals(TypeMap.TIMESTAMP, mapper.getColumnJdbcType(Types.TIMESTAMP, "date"));
        assertEquals(TypeMap.DATE, mapper.getColumnJdbcType(Types.DATE, "date"));
        assertEquals(TypeMap.VARCHAR, mapper.getColumnJdbcType(Types.VARCHAR, "varchar"));
        assertEquals(TypeMap.VARCHAR, mapper.getColumnJdbcType(Types.OTHER, "nvarchar"));
        assertEquals(TypeMap.BLOB, mapper.getColumnJdbcType(Types.OTHER, "oid"));
        assertEquals(TypeMap.UUID, mapper.getColumnJdbcType(Types.OTHER, "uuid"));
    }

    public void test_getColumnTorqueType_OriginalMapping() {
        // ## Arrange ##
        Properties prop = new Properties();
        prop.setProperty("torque.typeMappingMap", "map:{FOO=java.bar.Tender}");
        initializeTestProperty(prop);
        Map<String, String> nameToTorqueTypeMap = new LinkedHashMap<String, String>();
        nameToTorqueTypeMap.put("__int4", "FOO");
        DfJdbcTypeMapper mapper = createMapperOracle(nameToTorqueTypeMap);

        // ## Act & Assert ##
        assertEquals("FOO", mapper.getColumnJdbcType(Types.TIMESTAMP, "__int4"));
        assertEquals("java.bar.Tender", TypeMap.findJavaNativeByJdbcType("FOO", 0, 0));
    }

    public void test_isOracleNCharOrNVarchar_basic() {
        // ## Arrange ##
        initializeEmptyProperty();
        Map<String, String> nameToTorqueTypeMap = new LinkedHashMap<String, String>();
        nameToTorqueTypeMap.put("foo", "bar");
        DfJdbcTypeMapper mapper = createMapperOracle(nameToTorqueTypeMap);

        // ## Act & Assert ##
        assertTrue(mapper.isOracleNCharOrNVarchar("NVARCHAR2"));
        assertTrue(mapper.isOracleNCharOrNVarchar("NCHAR2"));
        assertFalse(mapper.isOracleNCharOrNVarchar("VARCHAR"));
        assertFalse(mapper.isOracleNCharOrNVarchar("VARCHAR2"));
        assertFalse(mapper.isOracleNCharOrNVarchar("CHAR"));
        assertFalse(mapper.isOracleNCharOrNVarchar("CLOB"));
        assertFalse(mapper.isOracleNCharOrNVarchar("NCLOB"));
    }

    public void test_isOracleNCharOrNVarcharOrNClob_basic() {
        // ## Arrange ##
        initializeEmptyProperty();
        Map<String, String> nameToTorqueTypeMap = new LinkedHashMap<String, String>();
        nameToTorqueTypeMap.put("foo", "bar");
        DfJdbcTypeMapper mapper = createMapperOracle(nameToTorqueTypeMap);

        // ## Act & Assert ##
        assertTrue(mapper.isOracleNCharOrNVarcharOrNClob("NVARCHAR2"));
        assertTrue(mapper.isOracleNCharOrNVarcharOrNClob("NCHAR2"));
        assertTrue(mapper.isOracleNCharOrNVarcharOrNClob("NCLOB"));
        assertFalse(mapper.isOracleNCharOrNVarcharOrNClob("VARCHAR"));
        assertFalse(mapper.isOracleNCharOrNVarcharOrNClob("VARCHAR2"));
        assertFalse(mapper.isOracleNCharOrNVarcharOrNClob("CHAR"));
        assertFalse(mapper.isOracleNCharOrNVarcharOrNClob("CLOB"));
    }

    protected DfJdbcTypeMapper createMapperOracle(Map<String, String> nameToTorqueTypeMap) {
        Map<String, Map<String, String>> pointMap = newHashMap();
        return new DfJdbcTypeMapper(nameToTorqueTypeMap, pointMap, new TestResource().java().oracle());
    }

    protected DfJdbcTypeMapper createMapperPostgreSQL(Map<String, String> nameToTorqueTypeMap) {
        Map<String, Map<String, String>> pointMap = newHashMap();
        return new DfJdbcTypeMapper(nameToTorqueTypeMap, pointMap, new TestResource().java().postgreSQL());
    }

    protected void initializeEmptyProperty() {
        DfBuildProperties.getInstance().setProperties(new Properties());
        DfBuildProperties.getInstance().getHandler().reload();
        TypeMap.reload();
    }

    protected void initializeTestProperty(Properties prop) {
        DfBuildProperties.getInstance().setProperties(prop);
        DfBuildProperties.getInstance().getHandler().reload();
        TypeMap.reload();
    }

    protected static class TestResource implements DfMapperResource {
        protected boolean _targetLanguageJava;
        protected boolean _databaseOracle;
        protected boolean _databasePostgreSQL;
        protected boolean _databaseSQLServer;

        public TestResource java() {
            _targetLanguageJava = true;
            return this;
        }

        public TestResource oracle() {
            _databaseOracle = true;
            return this;
        }

        public TestResource postgreSQL() {
            _databasePostgreSQL = true;
            return this;
        }

        public TestResource sqlServer() {
            _databaseSQLServer = true;
            return this;
        }

        public DfLanguageDependency getLang() {
            return new DfLanguageDependencyJava();
        }

        public boolean isDbmsOracle() {
            return _databaseOracle;
        }

        public boolean isDbmsPostgreSQL() {
            return _databasePostgreSQL;
        }

        public boolean isDbmsSQLServer() {
            return _databaseSQLServer;
        }

        public boolean isDbmsDB2() {
            return false;
        }

        public boolean isDbmsDerby() {
            return false;
        }
    }
}
