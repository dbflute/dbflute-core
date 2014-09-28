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
package org.seasar.dbflute.helper.dataset.states;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.dbflute.helper.dataset.DfDataRow;

/**
 * @author modified by jflute (originated in Seasar2)
 * @since 0.8.3 (2008/10/28 Tuesday)
 */
public abstract class DfDtsAbstractRowState implements DfDtsRowState {

    private static final Log _log = LogFactory.getLog(DfDtsAbstractRowState.class);

    DfDtsAbstractRowState() {
    }

    public void update(DataSource dataSource, DfDataRow row) {
        final DfDtsSqlContext ctx = getSqlContext(row);
        execute(dataSource, ctx.getSql(), ctx.getArgs(), ctx.getArgTypes(), row);
    }

    protected void execute(DataSource dataSource, String sql, Object[] args, Class<?>[] argTypes, DfDataRow row) {
        final String tableName = row.getTable().getTableDbName();
        final Connection conn = getConnection(dataSource);
        try {
            final PreparedStatement ps = prepareStatement(conn, sql);
            try {
                _log.info(getSql4Log(tableName, Arrays.asList(args)));
                bindArgs(ps, args, argTypes);
                ps.executeUpdate();
            } catch (SQLException e) {
                String msg = "The SQL threw the exception: " + sql;
                throw new IllegalStateException(msg, e);
            } finally {
                if (ps != null) {
                    try {
                        ps.close();
                    } catch (SQLException ignored) {
                    }
                }
            }
        } finally {
            close(conn);
        }
    }

    protected String getSql4Log(String tableName, final List<? extends Object> bindParameters) {
        String bindParameterString = bindParameters.toString();
        bindParameterString = bindParameterString.substring(1, bindParameterString.length() - 1);
        return tableName + ":{" + bindParameterString + "}";
    }

    protected void bindArgs(PreparedStatement ps, Object[] args, Class<?>[] argTypes) throws SQLException {
        if (args == null) {
            return;
        }
        for (int i = 0; i < args.length; ++i) {
            final Object value = args[i];
            final Class<?> type = argTypes[i];
            final int parameterIndex = (i + 1);
            if (String.class.isAssignableFrom(type)) {
                if (value != null) {
                    if (isTimestampValue((String) value)) {
                        final Timestamp timestamp = getTimestampValue((String) value);
                        ps.setTimestamp(parameterIndex, timestamp);
                    } else {
                        ps.setString(parameterIndex, (String) value);
                    }
                } else {
                    ps.setNull(parameterIndex, Types.VARCHAR);
                }
            } else if (Number.class.isAssignableFrom(type)) {
                if (value != null) {
                    ps.setBigDecimal(parameterIndex, new BigDecimal(value.toString()));
                } else {
                    ps.setNull(parameterIndex, Types.NUMERIC);
                }
            } else if (java.util.Date.class.isAssignableFrom(type)) {
                if (value != null) {
                    if (value instanceof String) {
                        final Timestamp timestamp = getTimestampValue((String) value);
                        ps.setTimestamp(parameterIndex, timestamp);
                    } else {
                        if (value instanceof Timestamp) {
                            ps.setTimestamp(parameterIndex, (Timestamp) value);
                        } else {
                            ps.setDate(parameterIndex, new java.sql.Date(((java.util.Date) value).getTime()));
                        }
                    }
                } else {
                    ps.setNull(parameterIndex, Types.DATE);
                }
            } else {
                if (value != null) {
                    ps.setObject(parameterIndex, value);
                } else {
                    ps.setNull(parameterIndex, Types.VARCHAR);
                }
            }
        }
    }

    protected boolean isTimestampValue(String value) {
        if (value == null) {
            return false;
        }
        value = filterTimestampValue(value);
        try {
            Timestamp.valueOf(value);
            return true;
        } catch (RuntimeException e) {
        }
        return false;
    }

    protected Timestamp getTimestampValue(String value) {
        final String filteredTimestampValue = filterTimestampValue(value);
        try {
            return Timestamp.valueOf(filteredTimestampValue);
        } catch (RuntimeException e) {
            String msg = "The value cannot be convert to timestamp:";
            msg = msg + " value=" + value + " filtered=" + filteredTimestampValue;
            throw new IllegalStateException(msg, e);
        }
    }

    protected String filterTimestampValue(String value) {
        value = value.trim();
        if (value.indexOf("/") == 4 && value.lastIndexOf("/") == 7) {
            value = value.replaceAll("/", "-");
        }
        if (value.indexOf("-") == 4 && value.lastIndexOf("-") == 7) {
            if (value.length() == "2007-07-09".length()) {
                value = value + " 00:00:00";
            }
        }
        return value;
    }

    protected abstract DfDtsSqlContext getSqlContext(DfDataRow row);

    protected DfDtsSqlContext createDtsSqlContext(String sql, List<Object> argList, List<Class<?>> argTypeList) {
        return new DfDtsSqlContext(sql, argList.toArray(), argTypeList.toArray(new Class[] {}));
    }

    private static Connection getConnection(DataSource dataSource) {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private static PreparedStatement prepareStatement(Connection conn, String sql) {
        try {
            return conn.prepareStatement(sql);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void close(Connection conn) {
        if (conn == null)
            return;
        try {
            conn.close();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }
}