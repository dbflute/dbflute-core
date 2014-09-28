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
package org.seasar.dbflute.exception.handler;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.dbflute.DBDef;
import org.seasar.dbflute.cbean.ConditionBean;
import org.seasar.dbflute.cbean.ConditionBeanContext;
import org.seasar.dbflute.exception.EntityAlreadyExistsException;
import org.seasar.dbflute.exception.SQLFailureException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.outsidesql.OutsideSqlContext;
import org.seasar.dbflute.resource.DBFluteSystem;
import org.seasar.dbflute.resource.InternalMapContext;
import org.seasar.dbflute.resource.InternalMapContext.InvokePathProvider;
import org.seasar.dbflute.resource.ResourceContext;

/**
 * @author jflute
 */
public class SQLExceptionHandler {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Log _log = LogFactory.getLog(SQLExceptionHandler.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final SQLExceptionAdviser _adviser = createAdviser();

    // ===================================================================================
    //                                                                              Handle
    //                                                                              ======
    /**
     * @param e The instance of SQLException. (NotNull)
     * @param resource The resource, item and elements, of SQLException message. (NotNull)
     */
    public void handleSQLException(SQLException e, SQLExceptionResource resource) {
        if (resource.isUniqueConstraintHandling() && isUniqueConstraintException(e)) {
            throwEntityAlreadyExistsException(e, resource);
        } else {
            throwSQLFailureException(e, resource);
        }
    }

    protected boolean isUniqueConstraintException(SQLException e) {
        if (!ResourceContext.isExistResourceContextOnThread()) {
            return false;
        }
        return ResourceContext.isUniqueConstraintException(extractSQLState(e), e.getErrorCode());
    }

    // ===================================================================================
    //                                                                               Throw
    //                                                                               =====
    protected void throwEntityAlreadyExistsException(SQLException e, SQLExceptionResource resource) {
        final ExceptionMessageBuilder br = createExceptionMessageBuilder();
        br.addNotice("The entity already exists on the database.");
        br.addItem("Advice");
        br.addElement("Please confirm the primary key whether it already exists on the database.");
        br.addElement("And also confirm the unique constraint for other columns.");
        setupCommonElement(br, e, resource);
        final String msg = br.buildExceptionMessage();
        throw new EntityAlreadyExistsException(msg, e);
    }

    protected void throwSQLFailureException(SQLException e, SQLExceptionResource resource) {
        final ExceptionMessageBuilder br = createExceptionMessageBuilder();
        final List<String> noticeList = resource.getNoticeList();
        if (!noticeList.isEmpty()) {
            for (String notice : noticeList) {
                br.addNotice(notice);
            }
        } else {
            br.addNotice("The SQL failed to execute."); // as default
        }
        br.addItem("Advice");
        br.addElement("Read the SQLException message.");
        final String advice = askAdvice(e, ResourceContext.currentDBDef());
        if (advice != null && advice.trim().length() > 0) {
            br.addElement("*" + advice);
        }
        setupCommonElement(br, e, resource);
        final String msg = br.buildExceptionMessage();
        throw new SQLFailureException(msg, e);
    }

    protected ExceptionMessageBuilder createExceptionMessageBuilder() {
        return new ExceptionMessageBuilder();
    }

    protected String askAdvice(SQLException e, DBDef dbdef) {
        return _adviser.askAdvice(e, dbdef);
    }

    // ===================================================================================
    //                                                                             Element
    //                                                                             =======
    protected void setupCommonElement(ExceptionMessageBuilder br, SQLException e, SQLExceptionResource resource) {
        br.addItem("SQLState");
        br.addElement(extractSQLState(e));
        br.addItem("ErrorCode");
        br.addElement(e.getErrorCode());
        setupSQLExceptionElement(br, e);
        final Map<String, List<Object>> resourceMap = resource.getResourceMap();
        for (Entry<String, List<Object>> entry : resourceMap.entrySet()) {
            br.addItem(entry.getKey());
            final List<Object> elementList = entry.getValue();
            for (Object element : elementList) {
                br.addElement(element);
            }
        }
        setupBehaviorElement(br);
        setupConditionBeanElement(br);
        setupOutsideSqlElement(br);
        setupTargetSqlElement(br, resource);
    }

    protected void setupSQLExceptionElement(ExceptionMessageBuilder br, SQLException e) {
        br.addItem("SQLException");
        br.addElement(e.getClass().getName());
        br.addElement(extractMessage(e));
        final SQLException nextEx = e.getNextException();
        if (nextEx != null) {
            br.addItem("NextException");
            br.addElement(nextEx.getClass().getName());
            br.addElement(extractMessage(nextEx));
            final SQLException nextNextEx = nextEx.getNextException();
            if (nextNextEx != null) {
                br.addItem("NextNextException");
                br.addElement(nextNextEx.getClass().getName());
                br.addElement(extractMessage(nextNextEx));
            }
        }
    }

    protected void setupBehaviorElement(ExceptionMessageBuilder br) {
        final Object invokePath = extractBehaviorInvokePath();
        if (invokePath != null) {
            br.addItem("Behavior");
            br.addElement(invokePath);
        }
    }

    protected void setupConditionBeanElement(ExceptionMessageBuilder br) {
        if (hasConditionBean()) {
            br.addItem("ConditionBean"); // only class name because of already existing displaySql
            br.addElement(getConditionBean().getClass().getName());
        }
    }

    protected void setupOutsideSqlElement(ExceptionMessageBuilder br) {
        if (hasOutsideSqlContext()) {
            br.addItem("OutsideSql");
            br.addElement(getOutsideSqlContext().getOutsideSqlPath());
        }
    }

    // *because displaySql exists instead which is enough to debug the exception
    //  (and for security to application data)
    //protected void setupParameterBeanElement(ExceptionMessageBuilder br) {
    //    if (hasOutsideSqlContext()) {
    //        br.addItem("ParameterBean");
    //        br.addElement(getOutsideSqlContext().getParameterBean());
    //    }
    //}

    // *because it's not an important thing
    //protected void setupStatementElement(ExceptionMessageBuilder br, Statement st) {
    //    if (st != null) {
    //        br.addItem("Statement");
    //        br.addElement(st.getClass().getName());
    //    }
    //}

    /**
     * Set up the element of target SQL. <br />
     * It uses displaySql as default.
     * <p>
     * If you want to hide application data on exception message,
     * you should override and use executedSql instead of displaySql or set up nothing.
     * But you should consider the following things:
     * </p>
     * <ul>
     *     <li>Debug process becomes more difficult.</li>
     *     <li>If you use embedded variables in the SQL, executedSql may also have application data.</li>
     *     <li>JDBC driver's message may also have application data about exception's cause.</li>
     * </ul>
     * <p>
     * So if you want to COMPLETELY hide application data on exception message,
     * you should cipher your application logs (files).
     * (If you hide JDBC driver's message too, you'll be at a loss when you debug)
     * </p>
     * @param br The builder of exception message. (NotNull)
     * @param resource The resource, item and elements, of SQLException message. (NotNull)
     */
    protected void setupTargetSqlElement(ExceptionMessageBuilder br, SQLExceptionResource resource) {
        final String displaySql = resource.getDisplaySql();
        if (displaySql != null) {
            if (resource.isDisplaySqlPartHandling()) {
                br.addItem("Display SQL (part of SQLs)");
            } else {
                br.addItem("Display SQL");
            }
            br.addElement(displaySql);
        }
        // this is example to use executed SQL
        //final String executedSql = resource.getExecutedSql();
        //if (executedSql != null) {
        //    br.addItem("Executed SQL");
        //    br.addElement(executedSql);
        //    br.addElement("*NOT use displaySql for security");
        //}
    }

    // ===================================================================================
    //                                                                             Extract
    //                                                                             =======
    protected String extractMessage(SQLException e) {
        String message = e.getMessage();

        // because a message of Oracle contains a line separator
        return message != null ? message.trim() : message;
    }

    protected String extractSQLState(SQLException e) {
        String sqlState = e.getSQLState();
        if (sqlState != null) {
            return sqlState;
        }

        // Next
        SQLException nextEx = e.getNextException();
        if (nextEx == null) {
            return null;
        }
        sqlState = nextEx.getSQLState();
        if (sqlState != null) {
            return sqlState;
        }

        // Next Next
        SQLException nextNextEx = nextEx.getNextException();
        if (nextNextEx == null) {
            return null;
        }
        sqlState = nextNextEx.getSQLState();
        if (sqlState != null) {
            return sqlState;
        }

        // Next Next Next
        SQLException nextNextNextEx = nextNextEx.getNextException();
        if (nextNextNextEx == null) {
            return null;
        }
        sqlState = nextNextNextEx.getSQLState();
        if (sqlState != null) {
            return sqlState;
        }

        // It doesn't use recursive call by design because JDBC is unpredictable fellow.
        return null;
    }

    protected String extractBehaviorInvokePath() {
        try {
            final String provided = doExtractBehaviorInvokePathFromProvider();
            if (provided != null) { // basically not null ()
                return provided;
            }
            return doExtractBehabiorInvokePathFromSeparatedParts();
        } catch (RuntimeException continued) {
            // this is additional info for debug so continue
            if (_log.isDebugEnabled()) {
                _log.debug("Failed to extract behavior invoke path for debug.", continued);
            }
            return null;
        }
    }

    protected String doExtractBehaviorInvokePathFromProvider() {
        final InvokePathProvider provider = InternalMapContext.getInvokePathProvider();
        return provider != null ? provider.provide() : null;
    }

    protected String doExtractBehabiorInvokePathFromSeparatedParts() {
        final Object behaviorInvokeName = InternalMapContext.getBehaviorInvokeName();
        if (behaviorInvokeName == null) {
            return null;
        }
        final Object clientInvokeName = InternalMapContext.getClientInvokeName();
        final Object byPassInvokeName = InternalMapContext.getByPassInvokeName();
        final StringBuilder sb = new StringBuilder();
        boolean existsPath = false;
        if (clientInvokeName != null) {
            existsPath = true;
            sb.append(clientInvokeName);
        }
        if (byPassInvokeName != null) {
            existsPath = true;
            sb.append(byPassInvokeName);
        }
        sb.append(behaviorInvokeName);
        if (existsPath) {
            sb.append("...");
        }
        return sb.toString();
    }

    // ===================================================================================
    //                                                                             Adviser
    //                                                                             =======
    protected SQLExceptionAdviser createAdviser() {
        return new SQLExceptionAdviser();
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    protected boolean hasConditionBean() {
        return ConditionBeanContext.isExistConditionBeanOnThread();
    }

    protected ConditionBean getConditionBean() {
        return ConditionBeanContext.getConditionBeanOnThread();
    }

    protected boolean hasOutsideSqlContext() {
        return OutsideSqlContext.isExistOutsideSqlContextOnThread();
    }

    protected OutsideSqlContext getOutsideSqlContext() {
        return OutsideSqlContext.getOutsideSqlContextOnThread();
    }

    protected String ln() {
        return DBFluteSystem.getBasicLn();
    }
}
