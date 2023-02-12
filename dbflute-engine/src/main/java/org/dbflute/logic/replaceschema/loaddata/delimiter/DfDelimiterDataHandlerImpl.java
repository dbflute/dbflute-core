/*
 * Copyright 2014-2022 the original author or authors.
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
package org.dbflute.logic.replaceschema.loaddata.delimiter;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.sql.DataSource;

import org.apache.torque.engine.database.model.UnifiedSchema;
import org.dbflute.exception.DfDelimiterDataRegistrationFailureException;
import org.dbflute.logic.replaceschema.loaddata.base.DfLoadedDataInfo;
import org.dbflute.logic.replaceschema.loaddata.base.dataprop.DfConvertValueProp;
import org.dbflute.logic.replaceschema.loaddata.base.dataprop.DfDefaultValueProp;
import org.dbflute.logic.replaceschema.loaddata.base.dataprop.DfLoadingControlProp;
import org.dbflute.logic.replaceschema.loaddata.base.interceptor.DfDataWritingInterceptor;
import org.dbflute.logic.replaceschema.loaddata.delimiter.secretary.DfDelimiterDataEncodingDirectoryExtractor;
import org.dbflute.logic.replaceschema.loaddata.delimiter.secretary.DfDelimiterDataOutputResultMarker;
import org.dbflute.system.DBFluteSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 */
public class DfDelimiterDataHandlerImpl implements DfDelimiterDataHandler {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The logger instance for this class. (NotNull) */
    private static final Logger _log = LoggerFactory.getLogger(DfDelimiterDataHandlerImpl.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DataSource _dataSource;
    protected final UnifiedSchema _unifiedSchema;

    protected boolean _loggingInsertSql;
    protected boolean _suppressBatchUpdate;
    protected boolean _suppressCheckColumnDef;
    protected boolean _suppressCheckImplicitSet;
    protected DfDataWritingInterceptor _dataWritingInterceptor;

    /** The data-prop of convert value map. (NotNull: after initialization) */
    protected DfConvertValueProp _convertValueProp;

    /** The data-prop of default value map. (NotNull: after initialization) */
    protected DfDefaultValueProp _defaultValueProp;

    /** The data-prop of loading control map. (NotNull: after initialization) */
    protected DfLoadingControlProp _loadingControlProp;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfDelimiterDataHandlerImpl(DataSource dataSource, UnifiedSchema unifiedSchema) {
        _dataSource = dataSource;
        _unifiedSchema = unifiedSchema;
    }

    // ===================================================================================
    //                                                                                Main
    //                                                                                ====
    public DfDelimiterDataResultInfo writeSeveralData(DfDelimiterDataResource resource, DfLoadedDataInfo loadedDataInfo) {
        final DfDelimiterDataResultInfo resultInfo = new DfDelimiterDataResultInfo();
        final String basePath = resource.getBasePath(); // e.g. .../ut/reversetsv, .../ut/tsv
        final File baseDir = new File(basePath);
        final List<String> encodingDirectoryList = extractEncodingDirectoryList(baseDir); // e.g. UTF-8, Shift_JIS
        if (encodingDirectoryList.isEmpty()) {
            return resultInfo;
        }
        try {
            for (String encoding : encodingDirectoryList) {
                if (isUnsupportedEncodingDirectory(encoding)) {
                    _log.warn("The encoding(directory name) is unsupported: encoding=" + encoding);
                    continue;
                }

                final String dataDirPath = basePath + "/" + encoding;
                final File encodingNameDir = new File(dataDirPath);
                final String[] fileNameList = encodingNameDir.list((dir, name) -> name.endsWith("." + resource.getFileType()));

                final SortedSet<String> sortedFileNameSet = new TreeSet<String>((o1, o2) -> o1.compareTo(o2));
                for (String fileName : fileNameList) {
                    sortedFileNameSet.add(fileName);
                }

                final Map<String, Map<String, String>> convertValueMap = getConvertValueMap(resource, encoding);
                final Map<String, String> defaultValueMap = getDefaultValueMap(resource, encoding);
                for (String fileName : sortedFileNameSet) {
                    final String filePath = dataDirPath + "/" + fileName;
                    final DfDelimiterDataWriterImpl writer = new DfDelimiterDataWriterImpl(_dataSource, _unifiedSchema);
                    writer.setLoggingInsertSql(isLoggingInsertSql());
                    writer.setFilePath(filePath);
                    writer.setEncoding(encoding);
                    writer.setDelimiter(resource.getDelimiter());
                    writer.setConvertValueMap(convertValueMap);
                    writer.setDefaultValueMap(defaultValueMap);
                    writer.setSuppressBatchUpdate(isSuppressBatchUpdate());
                    writer.setSuppressCheckColumnDef(isSuppressCheckColumnDef());
                    writer.setSuppressCheckImplicitSet(isSuppressCheckImplicitSet());
                    writer.setDataWritingInterceptor(_dataWritingInterceptor);
                    writer.setConvertValueProp(_convertValueProp);
                    writer.setDefaultValueProp(_defaultValueProp);
                    writer.setLoadingControlProp(_loadingControlProp);
                    writer.writeData(resultInfo);
                    prepareImplicitClassificationLazyCheck(loadedDataInfo, writer);

                    final String loadType = resource.getLoadType();
                    final String fileType = resource.getFileType();
                    final boolean warned = resultInfo.containsColumnCountDiff(filePath);
                    loadedDataInfo.addLoadedFile(loadType, fileType, encoding, fileName, warned);
                }
                outputResultMark(resource, resultInfo, dataDirPath);
            }
        } catch (IOException e) {
            String msg = "Failed to register delimiter data: " + resource;
            throw new DfDelimiterDataRegistrationFailureException(msg, e);
        }
        return resultInfo;
    }

    protected List<String> extractEncodingDirectoryList(File baseDir) { // e.g. .../ut/reversetsv, .../ut/tsv
        return new DfDelimiterDataEncodingDirectoryExtractor(baseDir).extractEncodingDirectoryList();
    }

    protected boolean isUnsupportedEncodingDirectory(String encoding) {
        try {
            new String(new byte[0], 0, 0, encoding); // throw if unknown
            return false;
        } catch (UnsupportedEncodingException e) {
            return true;
        }
    }

    protected Map<String, String> getDefaultValueMap(DfDelimiterDataResource res, String encoding) {
        final String dataDirectory = res.getBasePath() + "/" + encoding;
        return _defaultValueProp.findDefaultValueMap(dataDirectory);
    }

    protected Map<String, Map<String, String>> getConvertValueMap(DfDelimiterDataResource res, String encoding) {
        final String dataDirectory = res.getBasePath() + "/" + encoding;
        return _convertValueProp.findConvertValueMap(dataDirectory);
    }

    protected FilenameFilter createSpecifiedExtFilenameFilter(final String typeName) {
        return (dir, name) -> name.endsWith("." + typeName);
    }

    protected void prepareImplicitClassificationLazyCheck(DfLoadedDataInfo info, DfDelimiterDataWriterImpl writer) {
        info.acceptImplicitClassificationLazyCheck(writer.getImplicitClassificationLazyCheckerList());
    }

    // ===================================================================================
    //                                                                         Result Mark
    //                                                                         ===========
    protected void outputResultMark(DfDelimiterDataResource resource, DfDelimiterDataResultInfo resultInfo, String dataDirectory) {
        new DfDelimiterDataOutputResultMarker().outputResultMark(resource, resultInfo, dataDirectory);
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String ln() {
        return DBFluteSystem.ln();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public DataSource getDataSource() {
        return _dataSource;
    }

    public UnifiedSchema getUnifiedSchema() {
        return _unifiedSchema;
    }

    public boolean isLoggingInsertSql() {
        return _loggingInsertSql;
    }

    public void setLoggingInsertSql(boolean loggingInsertSql) {
        _loggingInsertSql = loggingInsertSql;
    }

    public boolean isSuppressBatchUpdate() {
        return _suppressBatchUpdate;
    }

    public void setSuppressBatchUpdate(boolean suppressBatchUpdate) {
        _suppressBatchUpdate = suppressBatchUpdate;
    }

    public boolean isSuppressCheckColumnDef() {
        return _suppressCheckColumnDef;
    }

    public void setSuppressCheckColumnDef(boolean suppressCheckColumnDef) {
        _suppressCheckColumnDef = suppressCheckColumnDef;
    }

    public boolean isSuppressCheckImplicitSet() {
        return _suppressCheckImplicitSet;
    }

    public void setSuppressCheckImplicitSet(boolean suppressCheckImplicitSet) {
        _suppressCheckImplicitSet = suppressCheckImplicitSet;
    }

    public DfDataWritingInterceptor getDataWritingInterceptor() {
        return _dataWritingInterceptor;
    }

    public void setDataWritingInterceptor(DfDataWritingInterceptor dataWritingInterceptor) {
        _dataWritingInterceptor = dataWritingInterceptor;
    }

    public DfConvertValueProp getConvertValueProp() {
        return _convertValueProp;
    }

    public void setConvertValueProp(DfConvertValueProp convertValueProp) {
        _convertValueProp = convertValueProp;
    }

    public DfDefaultValueProp getDefaultValueProp() {
        return _defaultValueProp;
    }

    public void setDefaultValueProp(DfDefaultValueProp defaultValueProp) {
        _defaultValueProp = defaultValueProp;
    }

    public DfLoadingControlProp getLoadingControlProp() {
        return _loadingControlProp;
    }

    public void setLoadingControlProp(DfLoadingControlProp loadingControlProp) {
        _loadingControlProp = loadingControlProp;
    }
}
