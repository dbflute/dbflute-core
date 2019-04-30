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
package org.dbflute.s2dao.valuetype.plugin;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.dbflute.jdbc.PhysicalConnectionDigger;

/**
 * @author jflute
 */
public interface OracleAgent {

    // ===================================================================================
    //                                                                         Oracle DATE
    //                                                                         ===========
    /**
     * Convert the time-stamp to Oracle's date.
     * @param timestamp The value of time-stamp. (NotNull) 
     * @return The instance of oracle.sql.DATE for the time-stamp argument. (NotNull)
     */
    Object toOracleDate(Timestamp timestamp);

    // ===================================================================================
    //                                                                        Oracle ARRAY
    //                                                                        ============
    /**
     * Convert an array value to the Oracle's ARRAY.
     * @param conn The Oracle native connection for the database. (NotNull)
     * @param arrayTypeName The name of ARRAY type for Oracle. (NotNull)
     * @param arrayValue The value of array. (NotNull) 
     * @return The instance of oracle.sql.ARRAY for the array argument. (NotNull)
     * @throws SQLException When it fails to handle the connection or result value
     */
    Object toOracleArray(Connection conn, String arrayTypeName, Object arrayValue) throws SQLException;

    /**
     * Convert the Oracle's ARRAY to a standard array.
     * @param oracleArray The value of Oracle's ARRAY (oracle.sql.ARRAY). (NotNull) 
     * @return The instance of standard array for the Oracle's array argument. (NotNull)
     * @throws SQLException When it fails to handle the connection or result value
     */
    Object toStandardArray(Object oracleArray) throws SQLException;

    /**
     * Is this object Oracle's ARRAY?
     * @param obj The doubtful instance. (NotNull)
     * @return The determination, true or false.
     */
    boolean isOracleArray(Object obj);

    // ===================================================================================
    //                                                                       Oracle STRUCT
    //                                                                       =============
    /**
     * Convert the Oracle's STRUCT to a standard attributes.
     * @param conn The Oracle native connection for the database. (NotNull)
     * @param structTypeName The name of STRUCT type for Oracle. (NotNull)
     * @param attrs The array of attribute value. (NotNull) 
     * @return The STRUCT type contained to attribute values. (NotNull)
     * @throws SQLException When it fails to handle the connection or result value
     */
    Object toOracleStruct(Connection conn, String structTypeName, Object[] attrs) throws SQLException;

    /**
     * Convert the Oracle's STRUCT to a standard attributes.
     * @param oracleStruct The value of Oracle's STRUCT (oracle.sql.STRUCT). (NotNull) 
     * @return The array of attribute value as standard type. (NotNull)
     * @throws SQLException When it fails to handle the connection or result value
     */
    Object[] toStandardStructAttributes(Object oracleStruct) throws SQLException;

    /**
     * Is this object Oracle's STRUCT?
     * @param obj The doubtful instance. (NotNull)
     * @return The determination, true or false.
     */
    boolean isOracleStruct(Object obj);

    // ===================================================================================
    //                                                                          Connection
    //                                                                          ==========
    PhysicalConnectionDigger getPhysicalConnectionDigger();
}