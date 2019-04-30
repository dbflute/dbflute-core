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
package org.dbflute.task.bs;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.tools.ant.BuildException;
import org.apache.torque.engine.database.model.UnifiedSchema;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.texen.ant.TexenTask;
import org.dbflute.DfBuildProperties;
import org.dbflute.friends.velocity.DfFlutistLog4JLogSystem;
import org.dbflute.friends.velocity.DfGenerator;
import org.dbflute.friends.velocity.DfVelocityContextFactory;
import org.dbflute.helper.jdbc.connection.DfConnectionMetaInfo;
import org.dbflute.helper.jdbc.connection.DfDataSourceHandler;
import org.dbflute.helper.jdbc.context.DfSchemaSource;
import org.dbflute.logic.DfDBFluteTaskUtil;
import org.dbflute.logic.sql2entity.analyzer.DfOutsideSqlPack;
import org.dbflute.properties.DfBasicProperties;
import org.dbflute.properties.DfDatabaseProperties;
import org.dbflute.properties.DfLittleAdjustmentProperties;
import org.dbflute.properties.DfRefreshProperties;
import org.dbflute.properties.facade.DfDatabaseTypeFacadeProp;
import org.dbflute.task.bs.assistant.DfDocumentSelector;
import org.dbflute.task.bs.assistant.DfTaskBasicController;
import org.dbflute.task.bs.assistant.DfTaskControlCallback;
import org.dbflute.task.bs.assistant.DfTaskControlLogic;
import org.dbflute.task.bs.assistant.DfTaskDatabaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The abstract class of texen task.
 * @author jflute
 */
public abstract class DfAbstractTexenTask extends TexenTask {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The logger instance for this class. (NotNull) */
    private static final Logger _log = LoggerFactory.getLogger(DfAbstractTexenTask.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The resource of database info for the task. (NotNull) */
    protected final DfTaskDatabaseResource _databaseResource = new DfTaskDatabaseResource();

    /** The basic controller of task process. (NotNull) */
    protected final DfTaskBasicController _controller = createBasicTaskController(_databaseResource);

    /** The logic of task control. (NotNull) */
    protected final DfTaskControlLogic _controlLogic = createTaskControlLogic(_databaseResource);

    /** The selector of documents for velocity context. (NotNull) */
    protected final DfDocumentSelector _documentSelector = new DfDocumentSelector();

    // ===================================================================================
    //                                                                     Task Controller
    //                                                                     ===============
    protected DfTaskBasicController createBasicTaskController(DfTaskDatabaseResource databaseResource) {
        return new DfTaskBasicController(createTaskControlCallback(), databaseResource);
    }

    protected DfTaskControlCallback createTaskControlCallback() {
        return new DfTaskControlCallback() {

            public boolean callBegin() {
                return begin();
            }

            public void callInitializeDatabaseInfo() {
                initializeDatabaseInfo();
            }

            public void callInitializeVariousEnvironment() {
                initializeVariousEnvironment();
            }

            public boolean callUseDataSource() {
                return isUseDataSource();
            }

            public void callSetupDataSource() throws SQLException {
                setupDataSource();
            }

            public void callCommitDataSource() throws SQLException {
                commitDataSource();
            }

            public void callDestroyDataSource() throws SQLException {
                destroyDataSource();
            }

            public DfConnectionMetaInfo callGetConnectionMetaInfo() {
                return getConnectionMetaInfo();
            }

            public void callActualExecute() {
                doExecute();
            }

            public void callShowFinalMessage(long before, long after, boolean abort) {
                showFinalMessage(before, after, abort);
            }

            public String callGetTaskName() {
                return getTaskName();
            }
        };
    }

    protected DfTaskControlLogic createTaskControlLogic(DfTaskDatabaseResource databaseResource) {
        return new DfTaskControlLogic(databaseResource);
    }

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    @Override
    public final void execute() { // completely override
        _controller.execute();
    }

    // ===================================================================================
    //                                                                   Prepare Execution
    //                                                                   =================
    protected abstract boolean begin();

    protected void initializeDatabaseInfo() {
        _controlLogic.initializeDatabaseInfo();
    }

    protected void initializeVariousEnvironment() {
        _controlLogic.initializeVariousEnvironment();
    }

    // ===================================================================================
    //                                                                         Data Source
    //                                                                         ===========
    protected abstract boolean isUseDataSource();

    protected void setupDataSource() throws SQLException {
        _controlLogic.setupDataSource();
    }

    protected void commitDataSource() throws SQLException {
        _controlLogic.commitDataSource();
    }

    protected void destroyDataSource() throws SQLException {
        _controlLogic.destroyDataSource();
    }

    /**
     * Get data source for main connection. <br>
     * It returns valid data source after setupDataSource() success. <br>
     * Basically not null but when data source does not exist on thread, it returns null.
     * @return The data source with schema. (NullAllowed: when data source does not exist on thread, e.g. lazy connection)
     */
    protected DfSchemaSource getDataSource() {
        return _controlLogic.getDataSource();
    }

    protected DfConnectionMetaInfo getConnectionMetaInfo() {
        return _controlLogic.getConnectionMetaInfo();
    }

    // ===================================================================================
    //                                                                    Actual Execution
    //                                                                    ================
    protected abstract void doExecute();

    // ===================================================================================
    //                                                                       Final Message
    //                                                                       =============
    protected void showFinalMessage(long before, long after, boolean abort) {
        _controlLogic.showFinalMessage(before, after, abort, getTaskName(), getFinalInformation());
    }

    protected String getFinalInformation() {
        return null; // as default
    }

    // ===================================================================================
    //                                                                 SQL File Collecting
    //                                                                 ===================
    /**
     * Collect outside-SQL containing its file info as pack with directory check.
     * @return The pack object for outside-SQL files. (NotNull)
     */
    protected DfOutsideSqlPack collectOutsideSqlChecked() {
        return _controlLogic.collectOutsideSqlChecked();
    }

    // ===================================================================================
    //                                                                    Refresh Resource
    //                                                                    ================
    /**
     * Refresh resources of Eclipse projects.
     */
    protected void refreshResources() {
        _controlLogic.refreshResources();
    }

    // ===================================================================================
    //                                                                    Velocity Process
    //                                                                    ================
    protected DfGenerator getGenerator() {
        return DfGenerator.getInstance();
    }

    protected void fireVelocityProcess() {
        assertBasicAntParameter();

        // set up the encoding of templates from DBFlute property
        setInputEncoding(getBasicProperties().getTemplateFileEncoding());
        setOutputEncoding(getBasicProperties().getSourceFileEncoding());

        try {
            initializeGeneratorInstance();
            final DfGenerator generator = setupGenerator();
            final Context ctx = setupControlContext();
            _log.info("generator.parse(\"" + controlTemplate + "\", ctx);");
            generator.parse(controlTemplate, ctx);
            generator.shutdown();
            cleanup();
        } catch (BuildException e) {
            throw e;
        } catch (MethodInvocationException e) {
            final String method = e.getReferenceName() + "." + e.getMethodName() + "()";
            String msg = "Exception thrown by " + method + ": control=" + controlTemplate;
            throw new IllegalStateException(msg, e.getWrappedThrowable());
        } catch (ParseErrorException e) {
            throw new IllegalStateException("Velocity syntax error: control=" + controlTemplate, e);
        } catch (ResourceNotFoundException e) {
            throw new IllegalStateException("Resource not found: control=" + controlTemplate, e);
        } catch (Exception e) {
            throw new IllegalStateException("Generation failed: control=" + controlTemplate, e);
        }
    }

    protected void assertBasicAntParameter() {
        if (templatePath == null && !useClasspath) {
            String msg = "The template path needs to be defined if you are not using the classpath for locating templates!";
            throw new IllegalStateException(msg);
        }
        if (controlTemplate == null) {
            throw new IllegalStateException("The control template needs to be defined!");
        }
    }

    protected void initializeGeneratorInstance() {
        final DfGenerator generator = getGenerator();
        generator.initializeEngine(); // re-create if already exists
        if (templatePath != null) {
            generator.setProperty("file.resource.loader.path", templatePath);
        }
        if (useClasspath) {
            final String resourceLoaderName = "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader";
            generator.addProperty("resource.loader", "classpath");
            generator.setProperty("classpath.resource.loader.class", resourceLoaderName);
            generator.setProperty("classpath.resource.loader.cache", "false");
            generator.setProperty("classpath.resource.loader.modificationCheckInterval", "2");
        }
        generator.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM_CLASS, DfFlutistLog4JLogSystem.class.getName());
    }

    protected DfGenerator setupGenerator() {
        final DfGenerator generator = getGenerator();

        // *set up later using DBFlute property (dfprop)
        //generator.setOutputPath(outputDirectory);

        // actually from DBFlute property (dfprop)
        // because these variables could be set up before here
        generator.setInputEncoding(inputEncoding);
        generator.setOutputEncoding(outputEncoding);

        if (templatePath != null) {
            generator.setTemplatePath(templatePath);
        }
        return generator;
    }

    protected Context setupControlContext() {
        final Context ctx;
        try {
            ctx = initControlContext();
        } catch (Exception e) {
            String msg = "Failed to initialize control context:";
            msg = msg + " templatePath=" + templatePath + " useClasspath=" + useClasspath;
            throw new IllegalStateException(msg, e);
        }
        try {
            populateInitialContext(ctx);
        } catch (Exception e) {
            String msg = "Failed to populate initial context:";
            msg = msg + " templatePath=" + templatePath + " useClasspath=" + useClasspath;
            throw new IllegalStateException(msg, e);
        }
        if (contextProperties != null) {
            for (Iterator<?> i = contextProperties.getKeys(); i.hasNext();) {
                String property = (String) i.next();
                String value = contextProperties.getString(property);
                try {
                    ctx.put(property, new Integer(value));
                } catch (NumberFormatException nfe) {
                    String booleanString = contextProperties.testBoolean(value);
                    if (booleanString != null) {
                        ctx.put(property, Boolean.valueOf(booleanString));
                    } else {
                        if (property.endsWith("file.contents")) {
                            final String canonicalPath;
                            try {
                                canonicalPath = getProject().resolveFile(value).getCanonicalPath();
                            } catch (IOException e) {
                                String msg = "Failed to get the canonical path:";
                                msg = msg + " property=" + property + " value=" + value;
                                throw new IllegalStateException(msg, e);
                            }
                            value = fileContentsToString(canonicalPath);
                            property = property.substring(0, property.indexOf("file.contents") - 1);
                        }
                        ctx.put(property, value);
                    }
                }
            }
        }
        return ctx;
    }

    protected static String fileContentsToString(String filePath) {
        String contents = "";
        final File file = new File(filePath);
        if (file.exists()) {
            FileReader fr = null;
            try {
                fr = new FileReader(file);
                final char template[] = new char[(int) file.length()];
                fr.read(template);
                contents = new String(template);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to read the file: " + filePath, e);
            } finally {
                if (fr != null) {
                    try {
                        fr.close();
                    } catch (IOException ignored) {}
                }
            }
        }
        return contents;
    }

    protected DfVelocityContextFactory createVelocityContextFactory() {
        return new DfVelocityContextFactory();
    }

    // ===================================================================================
    //                                                                    Skip Information
    //                                                                    ================
    protected void showSkippedFileInformation() {
        final StringBuilder sb = new StringBuilder();
        sb.append(ln()).append("/- - - - - - - - - - - - - - - - - - - - - - - -");
        final boolean skipGenerateIfSameFile = getLittleAdjustmentProperties().isSkipGenerateIfSameFile();
        if (!skipGenerateIfSameFile) {
            sb.append(ln()).append("All class files have been generated. (overrided)");
            sb.append(ln()).append("- - - - - - - - - -/");
            _log.info(sb.toString());
            return;
        }
        final List<String> parseFileNameList = DfGenerator.getInstance().getParseFileNameList();
        final int parseSize = parseFileNameList.size();
        if (parseSize == 0) {
            sb.append(ln()).append("No class file has been parsed.");
            sb.append(ln()).append("- - - - - - - - - -/");
            return;
        }
        final List<String> skipFileNameList = DfGenerator.getInstance().getSkipFileNameList();
        final int skipSize = skipFileNameList.size();
        if (skipSize == 0) {
            sb.append(ln()).append("All class files have been generated. (overrided)");
            sb.append(ln()).append("- - - - - - - - - -/");
            return;
        }
        if (skipSize == parseSize) {
            sb.append(ln()).append("All class files have been skipped generating");
            sb.append(ln()).append("                because they have no change.");
        } else {
            sb.append(ln()).append("Several class files have been skipped generating");
            sb.append(ln()).append("                    because they have no change.");
        }
        sb.append(ln());
        sb.append(ln()).append("    -> ").append(skipSize).append(" skipped (in ").append(parseSize).append(" files)");
        sb.append(ln()).append("- - - - - - - - - -/");
    }

    // ===================================================================================
    //                                                                  Context Properties
    //                                                                  ==================
    @Override
    public void setContextProperties(String file) { // called by ANT (and completely override)
        try {
            // /- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
            // Initialize torque properties as Properties and set up singleton class
            // that saves 'build.properties'.
            // - - - - - - - - - -/
            final Properties prop = DfDBFluteTaskUtil.getBuildProperties(file, getProject());
            DfBuildProperties.getInstance().setProperties(prop);

            // /- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
            // Initialize context properties for Velocity.
            // - - - - - - - - - -/
            contextProperties = new ExtendedProperties();
            final Set<Entry<Object, Object>> entrySet = prop.entrySet();
            for (Entry<Object, Object> entry : entrySet) {
                contextProperties.setProperty((String) entry.getKey(), entry.getValue());
            }
        } catch (RuntimeException e) {
            String msg = "Failed to set context properties:";
            msg = msg + " file=" + file + " contextProperties=" + contextProperties;
            _log.warn(msg, e); // logging because it throws to ANT world
            throw e;
        }
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfBuildProperties getProperties() {
        return DfBuildProperties.getInstance();
    }

    protected DfBasicProperties getBasicProperties() {
        return getProperties().getBasicProperties();
    }

    protected DfDatabaseTypeFacadeProp getDatabaseTypeFacadeProp() {
        return getBasicProperties().getDatabaseTypeFacadeProp();
    }

    protected DfDatabaseProperties getDatabaseProperties() {
        return getProperties().getDatabaseProperties();
    }

    protected DfLittleAdjustmentProperties getLittleAdjustmentProperties() {
        return getProperties().getLittleAdjustmentProperties();
    }

    protected DfRefreshProperties getRefreshProperties() {
        return getProperties().getRefreshProperties();
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String ln() {
        return "\n";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    protected String getDriver() {
        return _databaseResource.getDriver();
    }

    protected String getUrl() {
        return _databaseResource.getUrl();
    }

    protected UnifiedSchema getMainSchema() {
        return _databaseResource.getMainSchema();
    }

    protected String getUser() {
        return _databaseResource.getUser();
    }

    protected String getPassword() {
        return _databaseResource.getPassword();
    }

    protected DfDataSourceHandler getDataSourceHandler() {
        return _databaseResource.getDataSourceHandler();
    }

    public void setEnvironmentType(String environmentType) {
        _controlLogic.acceptEnvironmentType(environmentType);
    }
}