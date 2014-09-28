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
package org.seasar.dbflute.logic;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.exception.DfDBFluteTaskCancelledException;
import org.seasar.dbflute.exception.DfDBFluteTaskFailureException;
import org.seasar.dbflute.exception.DfJDBCException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.friends.log4j.DfFlutistEmergencyLog4JLogger;
import org.seasar.dbflute.helper.jdbc.connection.DfConnectionMetaInfo;
import org.seasar.dbflute.properties.DfBasicProperties;
import org.seasar.dbflute.properties.DfDatabaseProperties;
import org.seasar.dbflute.resource.DBFluteSystem;
import org.seasar.dbflute.util.DfStringUtil;

/**
 * Utilities for DBFlute task.
 * @author jflute
 */
public final class DfDBFluteTaskUtil {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log-instance. */
    private static final Log _log;
    static {
        final File emergencyFile = new File("./log/emergency-logging.dfmark");
        if (emergencyFile.exists()) { // for log4j accident
            _log = new DfFlutistEmergencyLog4JLogger();
        } else {
            _log = LogFactory.getLog(DfDBFluteTaskUtil.class);
        }
    }

    // ===================================================================================
    //                                                             Build-Properties Set up
    //                                                             =======================
    /**
     * Get the property object that saves 'build-properties'.
     * @param file File-full-path-comma-string.
     * @param project Project-instance of ANT.
     * @return Context-properties.
     */
    public static Properties getBuildProperties(String file, Project project) {
        final Properties prop = new Properties();
        try {
            final String sources[] = DfStringUtil.splitList(file, ",").toArray(new String[] {});
            for (int i = 0; i < sources.length; i++) {
                final String source = sources[i];
                final Properties currentProp = new Properties();
                FileInputStream fis = null;
                try {
                    final File currentDirFile = new File(source);
                    final File targetFile;
                    if (currentDirFile.exists()) { // basically true
                        // from DBFlute client directory
                        targetFile = currentDirFile;
                    } else {
                        // from DBFlute module directory (old style)
                        targetFile = project.resolveFile(source);
                    }
                    fis = new FileInputStream(targetFile);
                    currentProp.load(fis);
                } catch (IOException e) {
                    // retry getting from class-path (basically unused)
                    final ClassLoader classLoader = project.getClass().getClassLoader();
                    InputStream ins = null;
                    try {
                        ins = classLoader.getResourceAsStream(source);
                        if (ins == null) {
                            String msg = "Context properties file " + source;
                            msg = msg + " could not be found in the file system or on the classpath!";
                            throw new BuildException(msg, e);
                        }
                        currentProp.load(ins);
                    } catch (IOException ignored) {
                        String msg = "Failed to load contextProperties:";
                        msg = msg + " file=" + source + " project=" + project;
                        throw new BuildException(msg, e);
                    } finally {
                        if (ins != null) {
                            try {
                                ins.close();
                            } catch (IOException ignored) {
                            }
                        }
                    }
                } finally {
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException ignored) {
                        }
                    }
                }
                final Set<Entry<Object, Object>> entrySet = currentProp.entrySet();
                for (Entry<Object, Object> entry : entrySet) {
                    prop.setProperty((String) entry.getKey(), (String) entry.getValue());
                }
            }
        } catch (RuntimeException e) {
            final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
            br.addNotice("Failed to get build.properties.");
            br.addItem("Advice");
            br.addElement("Check the existence of build.properties on DBFlute client directory");
            br.addItem("File");
            br.addElement(file);
            br.addItem("Project");
            br.addElement(project);
            final String msg = br.buildExceptionMessage();
            throw new BuildException(msg, e);
        }
        return prop;
    }

    // ===================================================================================
    //                                                                             Logging
    //                                                                             =======
    public static void logFinalMessage(String msg) {
        _log.info(msg);
    }

    public static void logException(Exception e, String taskName, DfConnectionMetaInfo metaInfo) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        setupCommonMessage(br, taskName, metaInfo);
        if (e instanceof SQLException) {
            // for showing next exception of SQLException
            buildSQLExceptionMessage(br, (SQLException) e);
        }
        final String msg = br.buildExceptionMessage();
        _log.error(msg, e);
    }

    protected static void setupCommonMessage(ExceptionMessageBuilder br, String taskName, DfConnectionMetaInfo metaInfo) {
        br.addNotice("Failed to execute DBFlute Task '" + taskName + "'.");
        br.addItem("Advice");
        br.addElement("Check the exception messages and the stack traces.");
        if (metaInfo != null) {
            br.addItem("Database Product");
            br.addElement(metaInfo.getProductDisp());
            br.addItem("JDBC Driver");
            br.addElement(metaInfo.getDriverDisp());
        }
    }

    protected static void buildSQLExceptionMessage(ExceptionMessageBuilder br, SQLException e) {
        final String sqlState = DfJDBCException.extractSQLState(e);
        br.addItem("SQLState");
        br.addElement(sqlState);
        final Integer errorCode = DfJDBCException.extractErrorCode(e);
        br.addItem("ErrorCode");
        br.addElement(errorCode);
        br.addItem("SQLException");
        br.addElement(e.getClass().getName());
        if (e instanceof DfJDBCException) {
            br.addElement("*Look at the message on the stack trace");
        } else {
            br.addElement(DfJDBCException.extractMessage(e));
        }
        final SQLException nextEx = e.getNextException();
        if (nextEx != null) {
            br.addItem("NextException");
            br.addElement(nextEx.getClass().getName());
            br.addElement(DfJDBCException.extractMessage(nextEx));
            final SQLException nextNextEx = nextEx.getNextException();
            if (nextNextEx != null) {
                br.addItem("NextNextException");
                br.addElement(nextNextEx.getClass().getName());
                br.addElement(DfJDBCException.extractMessage(nextNextEx));
            }
        }
    }

    public static void logError(Error e, String taskName, DfConnectionMetaInfo metaInfo) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        setupCommonMessage(br, taskName, metaInfo);
        final String msg = br.buildExceptionMessage();
        _log.error(msg, e);
    }

    public static String getDisplayTaskName(String taskName) {
        if (taskName.endsWith("df-jdbc")) {
            return "JDBC";
        } else if (taskName.equals("df-doc")) {
            return "Doc";
        } else if (taskName.equals("df-generate")) {
            return "Generate";
        } else if (taskName.equals("df-sql2entity")) {
            return "Sql2Entity";
        } else if (taskName.equals("df-outside-sql-test")) {
            return "OutsideSqlTest";
        } else if (taskName.equals("df-replace-schema")) {
            return "ReplaceSchema";
        } else if (taskName.equals("df-create-schema")) { // old style
            return "ReplaceSchema";
        } else if (taskName.equals("df-load-data")) { // old style
            return "ReplaceSchema";
        } else if (taskName.equals("df-take-finally")) { // old style
            return "ReplaceSchema";
        } else {
            return taskName;
        }
    }

    public static DfDBFluteTaskCancelledException createTaskCancelledException(String displayTaskName) {
        String msg = ln() + "/* * * * * * * * * * * * * * * * * * * * *";
        msg = msg + ln() + "Cancelled the DBFlute task: " + displayTaskName;
        msg = msg + ln() + "* * * * * * * * * */";
        return new DfDBFluteTaskCancelledException(msg);
    }

    public static void throwTaskFailure(String displayTaskName) {
        String msg = ln() + "/* * * * * * * * * * * * * * * * * * * * * * * * *";
        msg = msg + ln() + "Failed to execute the DBFlute task: " + displayTaskName;
        msg = msg + ln() + "Look at the log: console or dbflute.log";
        msg = msg + ln() + "* * * * * * * * * */";
        throw new DfDBFluteTaskFailureException(msg);
    }

    // ===================================================================================
    //                                                                          Connection
    //                                                                          ==========
    public static void shutdownIfDerbyEmbedded(String driver) throws SQLException {
        if (!driver.startsWith("org.apache.derby.") || !driver.endsWith(".EmbeddedDriver")) {
            return;
        }
        final String shutdownUrl = "jdbc:derby:;shutdown=true";
        Connection conn = null;
        try {
            _log.info("...Shutting down the connection to Derby");
            conn = DriverManager.getConnection(shutdownUrl);
        } catch (SQLException e) {
            if ("XJ015".equals(e.getSQLState())) {
                _log.info(" -> success: " + e.getMessage());
            } else {
                String msg = "Failed to shut down the connection to Derby:";
                msg = msg + " shutdownUrl=" + shutdownUrl;
                throw new DfJDBCException(msg, e);
            }
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected static DfBuildProperties getProperties() {
        return DfBuildProperties.getInstance();
    }

    protected static DfBasicProperties getBasicProperties() {
        return getProperties().getBasicProperties();
    }

    protected static DfDatabaseProperties getDatabaseProperties() {
        return getProperties().getDatabaseProperties();
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected static String ln() {
        return DBFluteSystem.getBasicLn();
    }
}