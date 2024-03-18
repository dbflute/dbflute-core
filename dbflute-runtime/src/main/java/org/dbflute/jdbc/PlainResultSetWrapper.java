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
package org.dbflute.jdbc;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

/**
 * @author jflute
 */
public class PlainResultSetWrapper implements ResultSet {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    private ResultSet _original;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public PlainResultSetWrapper(ResultSet original) {
        this._original = original;
    }

    // ===================================================================================
    //                                                                Java6 Implementation
    //                                                                ====================
    public int getConcurrency() throws SQLException {
        return _original.getConcurrency();
    }

    public int getFetchDirection() throws SQLException {
        return _original.getFetchDirection();
    }

    public int getFetchSize() throws SQLException {
        return _original.getFetchSize();
    }

    public int getRow() throws SQLException {
        return _original.getRow();
    }

    public int getType() throws SQLException {
        return _original.getType();
    }

    public void afterLast() throws SQLException {
        _original.afterLast();
    }

    public void beforeFirst() throws SQLException {
        _original.beforeFirst();
    }

    public void cancelRowUpdates() throws SQLException {
        _original.cancelRowUpdates();
    }

    public void clearWarnings() throws SQLException {
        _original.clearWarnings();
    }

    public void close() throws SQLException {
        _original.close();
    }

    public void deleteRow() throws SQLException {
        _original.deleteRow();
    }

    public void insertRow() throws SQLException {
        _original.insertRow();
    }

    public void moveToCurrentRow() throws SQLException {
        _original.moveToCurrentRow();
    }

    public void moveToInsertRow() throws SQLException {
        _original.moveToInsertRow();
    }

    public void refreshRow() throws SQLException {
        _original.refreshRow();
    }

    public void updateRow() throws SQLException {
        _original.updateRow();
    }

    public boolean first() throws SQLException {
        return _original.first();
    }

    public boolean isAfterLast() throws SQLException {
        return _original.isAfterLast();
    }

    public boolean isBeforeFirst() throws SQLException {
        return _original.isBeforeFirst();
    }

    public boolean isFirst() throws SQLException {
        return _original.isFirst();
    }

    public boolean isLast() throws SQLException {
        return _original.isLast();
    }

    public boolean last() throws SQLException {
        return _original.last();
    }

    public boolean next() throws SQLException {
        return _original.next();
    }

    public boolean previous() throws SQLException {
        return _original.previous();
    }

    public boolean rowDeleted() throws SQLException {
        return _original.rowDeleted();
    }

    public boolean rowInserted() throws SQLException {
        return _original.rowInserted();
    }

    public boolean rowUpdated() throws SQLException {
        return _original.rowUpdated();
    }

    public boolean wasNull() throws SQLException {
        return _original.wasNull();
    }

    public byte getByte(int columnIndex) throws SQLException {
        return _original.getByte(columnIndex);
    }

    public double getDouble(int columnIndex) throws SQLException {
        return _original.getDouble(columnIndex);
    }

    public float getFloat(int columnIndex) throws SQLException {
        return _original.getFloat(columnIndex);
    }

    public int getInt(int columnIndex) throws SQLException {
        return _original.getInt(columnIndex);
    }

    public long getLong(int columnIndex) throws SQLException {
        return _original.getLong(columnIndex);
    }

    public short getShort(int columnIndex) throws SQLException {
        return _original.getShort(columnIndex);
    }

    public void setFetchDirection(int direction) throws SQLException {
        _original.setFetchDirection(direction);
    }

    public void setFetchSize(int rows) throws SQLException {
        _original.setFetchSize(rows);
    }

    public void updateNull(int columnIndex) throws SQLException {
        _original.updateNull(columnIndex);
    }

    public boolean absolute(int row) throws SQLException {
        return _original.absolute(row);
    }

    public boolean getBoolean(int columnIndex) throws SQLException {
        return _original.getBoolean(columnIndex);
    }

    public boolean relative(int rows) throws SQLException {
        return _original.relative(rows);
    }

    public byte[] getBytes(int columnIndex) throws SQLException {
        return _original.getBytes(columnIndex);
    }

    public void updateByte(int columnIndex, byte x) throws SQLException {
        _original.updateByte(columnIndex, x);

    }

    public void updateDouble(int columnIndex, double x) throws SQLException {
        _original.updateDouble(columnIndex, x);
    }

    public void updateFloat(int columnIndex, float x) throws SQLException {
        _original.updateFloat(columnIndex, x);
    }

    public void updateInt(int columnIndex, int x) throws SQLException {
        _original.updateInt(columnIndex, x);
    }

    public void updateLong(int columnIndex, long x) throws SQLException {
        _original.updateLong(columnIndex, x);
    }

    public void updateShort(int columnIndex, short x) throws SQLException {
        _original.updateShort(columnIndex, x);

    }

    public void updateBoolean(int columnIndex, boolean x) throws SQLException {
        _original.updateBoolean(columnIndex, x);
    }

    public void updateBytes(int columnIndex, byte[] x) throws SQLException {
        _original.updateBytes(columnIndex, x);
    }

    public InputStream getAsciiStream(int columnIndex) throws SQLException {
        return _original.getAsciiStream(columnIndex);
    }

    public InputStream getBinaryStream(int columnIndex) throws SQLException {
        return _original.getBinaryStream(columnIndex);
    }

    /**
     * @param columnIndex The index of column.
     * @return The value as InputStream.
     * @deprecated inherits the original method
     * @throws SQLException When it fails to handle the SQL.
     */
    public InputStream getUnicodeStream(int columnIndex) throws SQLException {
        return _original.getUnicodeStream(columnIndex);
    }

    public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
        _original.updateAsciiStream(columnIndex, x, length);
    }

    public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
        _original.updateBinaryStream(columnIndex, x, length);
    }

    public Reader getCharacterStream(int columnIndex) throws SQLException {
        return _original.getCharacterStream(columnIndex);
    }

    public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
        _original.updateCharacterStream(columnIndex, x, length);
    }

    public Object getObject(int columnIndex) throws SQLException {
        return _original.getObject(columnIndex);
    }

    public void updateObject(int columnIndex, Object x) throws SQLException {
        _original.updateObject(columnIndex, x);
    }

    public void updateObject(int columnIndex, Object x, int scale) throws SQLException {
        _original.updateObject(columnIndex, x, scale);
    }

    public String getCursorName() throws SQLException {
        return _original.getCursorName();
    }

    public String getString(int columnIndex) throws SQLException {
        return _original.getString(columnIndex);
    }

    public void updateString(int columnIndex, String x) throws SQLException {
        _original.updateString(columnIndex, x);
    }

    public byte getByte(String columnName) throws SQLException {
        return _original.getByte(columnName);
    }

    public double getDouble(String columnName) throws SQLException {
        return _original.getDouble(columnName);
    }

    public float getFloat(String columnName) throws SQLException {
        return _original.getFloat(columnName);
    }

    public int findColumn(String columnName) throws SQLException {
        return _original.findColumn(columnName);
    }

    public int getInt(String columnName) throws SQLException {
        return _original.getInt(columnName);
    }

    public long getLong(String columnName) throws SQLException {
        return _original.getLong(columnName);
    }

    public short getShort(String columnName) throws SQLException {
        return _original.getShort(columnName);
    }

    public void updateNull(String columnName) throws SQLException {
        _original.updateNull(columnName);
    }

    public boolean getBoolean(String columnName) throws SQLException {
        return _original.getBoolean(columnName);
    }

    public byte[] getBytes(String columnName) throws SQLException {
        return _original.getBytes(columnName);
    }

    public void updateByte(String columnName, byte x) throws SQLException {
        _original.updateByte(columnName, x);
    }

    public void updateDouble(String columnName, double x) throws SQLException {
        _original.updateDouble(columnName, x);
    }

    public void updateFloat(String columnName, float x) throws SQLException {
        _original.updateFloat(columnName, x);
    }

    public void updateInt(String columnName, int x) throws SQLException {
        _original.updateInt(columnName, x);
    }

    public void updateLong(String columnName, long x) throws SQLException {
        _original.updateLong(columnName, x);
    }

    public void updateShort(String columnName, short x) throws SQLException {
        _original.updateShort(columnName, x);
    }

    public void updateBoolean(String columnName, boolean x) throws SQLException {
        _original.updateBoolean(columnName, x);
    }

    public void updateBytes(String columnName, byte[] x) throws SQLException {
        _original.updateBytes(columnName, x);
    }

    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        return _original.getBigDecimal(columnIndex);
    }

    /**
     * @param columnIndex The index of column.
     * @param scale The number of scale.
     * @return The value as BigDecimal.
     * @deprecated inherits the original method
     * @throws SQLException When it fails to handle the SQL.
     */
    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        return _original.getBigDecimal(columnIndex, scale);
    }

    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {

        _original.updateBigDecimal(columnIndex, x);
    }

    public URL getURL(int columnIndex) throws SQLException {
        return _original.getURL(columnIndex);
    }

    public Array getArray(int i) throws SQLException {
        return _original.getArray(i);
    }

    public void updateArray(int columnIndex, Array x) throws SQLException {
        _original.updateArray(columnIndex, x);
    }

    public Blob getBlob(int i) throws SQLException {
        return _original.getBlob(i);
    }

    public void updateBlob(int columnIndex, Blob x) throws SQLException {
        _original.updateBlob(columnIndex, x);
    }

    public Clob getClob(int i) throws SQLException {
        return _original.getClob(i);
    }

    public void updateClob(int columnIndex, Clob x) throws SQLException {
        _original.updateClob(columnIndex, x);
    }

    public Date getDate(int columnIndex) throws SQLException {
        return _original.getDate(columnIndex);
    }

    public void updateDate(int columnIndex, Date x) throws SQLException {
        _original.updateDate(columnIndex, x);
    }

    public Ref getRef(int i) throws SQLException {
        return _original.getRef(i);
    }

    public void updateRef(int columnIndex, Ref x) throws SQLException {
        _original.updateRef(columnIndex, x);
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        return _original.getMetaData();
    }

    public SQLWarning getWarnings() throws SQLException {
        return _original.getWarnings();
    }

    public Statement getStatement() throws SQLException {
        return _original.getStatement();
    }

    public Time getTime(int columnIndex) throws SQLException {
        return _original.getTime(columnIndex);
    }

    public void updateTime(int columnIndex, Time x) throws SQLException {
        _original.updateTime(columnIndex, x);
    }

    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        return _original.getTimestamp(columnIndex);
    }

    public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
        _original.updateTimestamp(columnIndex, x);
    }

    public InputStream getAsciiStream(String columnName) throws SQLException {
        return _original.getAsciiStream(columnName);
    }

    public InputStream getBinaryStream(String columnName) throws SQLException {
        return _original.getBinaryStream(columnName);
    }

    /**
     * @param columnName The name of column. (NotNull)
     * @return The value as InputStream.
     * @deprecated inherits the original method 
     * @throws SQLException When it fails to handle the SQL.
     */
    public InputStream getUnicodeStream(String columnName) throws SQLException {
        return _original.getUnicodeStream(columnName);
    }

    public void updateAsciiStream(String columnName, InputStream x, int length) throws SQLException {
        _original.updateAsciiStream(columnName, x, length);
    }

    public void updateBinaryStream(String columnName, InputStream x, int length) throws SQLException {
        _original.updateBinaryStream(columnName, x, length);
    }

    public Reader getCharacterStream(String columnName) throws SQLException {
        return _original.getCharacterStream(columnName);
    }

    public void updateCharacterStream(String columnName, Reader reader, int length) throws SQLException {
        _original.updateCharacterStream(columnName, reader, length);
    }

    public Object getObject(String columnName) throws SQLException {
        return _original.getObject(columnName);
    }

    public void updateObject(String columnName, Object x) throws SQLException {
        _original.updateObject(columnName, x);
    }

    public void updateObject(String columnName, Object x, int scale) throws SQLException {

        _original.updateObject(columnName, x, scale);
    }

    public Object getObject(int i, Map<String, Class<?>> map) throws SQLException {
        return _original.getObject(i, map);
    }

    public String getString(String columnName) throws SQLException {
        return _original.getString(columnName);
    }

    public void updateString(String columnName, String x) throws SQLException {
        _original.updateString(columnName, x);
    }

    public BigDecimal getBigDecimal(String columnName) throws SQLException {
        return _original.getBigDecimal(columnName);
    }

    /**
     * @param columnName The name of column. (NotNull)
     * @param scale The number of scale.
     * @return The value as BigDecimal.
     * @deprecated inherits the original method 
     * @throws SQLException When it fails to handle the SQL.
     */
    public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException {
        return _original.getBigDecimal(columnName, scale);
    }

    public void updateBigDecimal(String columnName, BigDecimal x) throws SQLException {

        _original.updateBigDecimal(columnName, x);
    }

    public URL getURL(String columnName) throws SQLException {
        return _original.getURL(columnName);
    }

    public Array getArray(String colName) throws SQLException {
        return _original.getArray(colName);
    }

    public void updateArray(String columnName, Array x) throws SQLException {
        _original.updateArray(columnName, x);
    }

    public Blob getBlob(String colName) throws SQLException {
        return _original.getBlob(colName);
    }

    public void updateBlob(String columnName, Blob x) throws SQLException {
        _original.updateBlob(columnName, x);
    }

    public Clob getClob(String colName) throws SQLException {
        return _original.getClob(colName);
    }

    public void updateClob(String columnName, Clob x) throws SQLException {
        _original.updateClob(columnName, x);
    }

    public Date getDate(String columnName) throws SQLException {
        return _original.getDate(columnName);
    }

    public void updateDate(String columnName, Date x) throws SQLException {
        _original.updateDate(columnName, x);
    }

    public Date getDate(int columnIndex, Calendar cal) throws SQLException {
        return _original.getDate(columnIndex, cal);
    }

    public Ref getRef(String colName) throws SQLException {
        return _original.getRef(colName);
    }

    public void updateRef(String columnName, Ref x) throws SQLException {
        _original.updateRef(columnName, x);
    }

    public Time getTime(String columnName) throws SQLException {
        return _original.getTime(columnName);
    }

    public void updateTime(String columnName, Time x) throws SQLException {
        _original.updateTime(columnName, x);
    }

    public Time getTime(int columnIndex, Calendar cal) throws SQLException {
        return _original.getTime(columnIndex, cal);
    }

    public Timestamp getTimestamp(String columnName) throws SQLException {
        return _original.getTimestamp(columnName);
    }

    public void updateTimestamp(String columnName, Timestamp x) throws SQLException {
        _original.updateTimestamp(columnName, x);
    }

    public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
        return _original.getTimestamp(columnIndex, cal);
    }

    public Object getObject(String colName, Map<String, Class<?>> map) throws SQLException {
        return _original.getObject(colName, map);
    }

    public Date getDate(String columnName, Calendar cal) throws SQLException {
        return _original.getDate(columnName, cal);
    }

    public Time getTime(String columnName, Calendar cal) throws SQLException {
        return _original.getTime(columnName, cal);
    }

    public Timestamp getTimestamp(String columnName, Calendar cal) throws SQLException {
        return _original.getTimestamp(columnName, cal);
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        return _original.unwrap(iface);
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return _original.isWrapperFor(iface);
    }

    public RowId getRowId(int i) throws SQLException {
        return _original.getRowId(i);
    }

    public RowId getRowId(String s) throws SQLException {
        return _original.getRowId(s);
    }

    public void updateRowId(int i, RowId rowid) throws SQLException {
        _original.updateRowId(i, rowid);
    }

    public void updateRowId(String s, RowId rowid) throws SQLException {
        _original.updateRowId(s, rowid);
    }

    public int getHoldability() throws SQLException {
        return _original.getHoldability();
    }

    public boolean isClosed() throws SQLException {
        return _original.isClosed();
    }

    public void updateNString(int i, String s) throws SQLException {
        _original.updateNString(i, s);
    }

    public void updateNString(String s, String s1) throws SQLException {
        _original.updateNString(s, s1);
    }

    public void updateNClob(int i, NClob nclob) throws SQLException {
        _original.updateNClob(i, nclob);
    }

    public void updateNClob(String s, NClob nclob) throws SQLException {
        _original.updateNClob(s, nclob);
    }

    public NClob getNClob(int i) throws SQLException {
        return _original.getNClob(i);
    }

    public NClob getNClob(String s) throws SQLException {
        return _original.getNClob(s);
    }

    public SQLXML getSQLXML(int i) throws SQLException {
        return _original.getSQLXML(i);
    }

    public SQLXML getSQLXML(String s) throws SQLException {
        return _original.getSQLXML(s);
    }

    public void updateSQLXML(int i, SQLXML sqlxml) throws SQLException {
        _original.updateSQLXML(i, sqlxml);
    }

    public void updateSQLXML(String s, SQLXML sqlxml) throws SQLException {
        _original.updateSQLXML(s, sqlxml);
    }

    public String getNString(int i) throws SQLException {
        return _original.getNString(i);
    }

    public String getNString(String s) throws SQLException {
        return _original.getNString(s);
    }

    public Reader getNCharacterStream(int i) throws SQLException {
        return _original.getNCharacterStream(i);
    }

    public Reader getNCharacterStream(String s) throws SQLException {
        return _original.getNCharacterStream(s);
    }

    public void updateNCharacterStream(int i, Reader reader, long l) throws SQLException {
        _original.updateNCharacterStream(i, reader, l);
    }

    public void updateNCharacterStream(String s, Reader reader, long l) throws SQLException {
        _original.updateNCharacterStream(s, reader, l);
    }

    public void updateAsciiStream(int i, InputStream inputstream, long l) throws SQLException {
        _original.updateAsciiStream(i, inputstream, l);
    }

    public void updateBinaryStream(int i, InputStream inputstream, long l) throws SQLException {
        _original.updateBinaryStream(i, inputstream, l);
    }

    public void updateCharacterStream(int i, Reader reader, long l) throws SQLException {
        _original.updateCharacterStream(i, reader, l);
    }

    public void updateAsciiStream(String s, InputStream inputstream, long l) throws SQLException {
        _original.updateAsciiStream(s, inputstream, l);
    }

    public void updateBinaryStream(String s, InputStream inputstream, long l) throws SQLException {
        _original.updateBinaryStream(s, inputstream, l);
    }

    public void updateCharacterStream(String s, Reader reader, long l) throws SQLException {
        _original.updateCharacterStream(s, reader, l);
    }

    public void updateBlob(int i, InputStream inputstream, long l) throws SQLException {
        _original.updateBlob(i, inputstream, l);
    }

    public void updateBlob(String s, InputStream inputstream, long l) throws SQLException {
        _original.updateBlob(s, inputstream, l);
    }

    public void updateClob(int i, Reader reader, long l) throws SQLException {
        _original.updateClob(i, reader, l);
    }

    public void updateClob(String s, Reader reader, long l) throws SQLException {
        _original.updateClob(s, reader, l);
    }

    public void updateNClob(int i, Reader reader, long l) throws SQLException {
        _original.updateNClob(i, reader, l);
    }

    public void updateNClob(String s, Reader reader, long l) throws SQLException {
        _original.updateNClob(s, reader, l);
    }

    public void updateNCharacterStream(int i, Reader reader) throws SQLException {
        _original.updateNCharacterStream(i, reader);
    }

    public void updateNCharacterStream(String s, Reader reader) throws SQLException {
        _original.updateNCharacterStream(s, reader);
    }

    public void updateAsciiStream(int i, InputStream inputstream) throws SQLException {
        _original.updateAsciiStream(i, inputstream);
    }

    public void updateBinaryStream(int i, InputStream inputstream) throws SQLException {
        _original.updateBinaryStream(i, inputstream);
    }

    public void updateCharacterStream(int i, Reader reader) throws SQLException {
        _original.updateCharacterStream(i, reader);
    }

    public void updateAsciiStream(String s, InputStream inputstream) throws SQLException {
        _original.updateAsciiStream(s, inputstream);
    }

    public void updateBinaryStream(String s, InputStream inputstream) throws SQLException {
        _original.updateBinaryStream(s, inputstream);
    }

    public void updateCharacterStream(String s, Reader reader) throws SQLException {
        _original.updateCharacterStream(s, reader);
    }

    public void updateBlob(int i, InputStream inputstream) throws SQLException {
        _original.updateBlob(i, inputstream);
    }

    public void updateBlob(String s, InputStream inputstream) throws SQLException {
        _original.updateBlob(s, inputstream);
    }

    public void updateClob(int i, Reader reader) throws SQLException {
        _original.updateClob(i, reader);
    }

    public void updateClob(String s, Reader reader) throws SQLException {
        _original.updateClob(s, reader);
    }

    public void updateNClob(int i, Reader reader) throws SQLException {
        _original.updateNClob(i, reader);
    }

    public void updateNClob(String s, Reader reader) throws SQLException {
        _original.updateNClob(s, reader);
    }

    // ===================================================================================
    //                                                                Java8 Implementation
    //                                                                ====================
    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        return _original.getObject(columnIndex, type);
    }

    public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
        return _original.getObject(columnLabel, type);
    }
}