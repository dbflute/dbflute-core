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
package org.seasar.dbflute.logic.replaceschema.loaddata.impl;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.engine.database.model.UnifiedSchema;
import org.seasar.dbflute.exception.DfDelimiterDataRegistrationFailureException;
import org.seasar.dbflute.logic.replaceschema.loaddata.DfDelimiterDataHandler;
import org.seasar.dbflute.logic.replaceschema.loaddata.DfDelimiterDataResource;
import org.seasar.dbflute.logic.replaceschema.loaddata.DfDelimiterDataResultInfo;
import org.seasar.dbflute.logic.replaceschema.loaddata.DfLoadedDataInfo;
import org.seasar.dbflute.logic.replaceschema.loaddata.impl.dataprop.DfDefaultValueProp;
import org.seasar.dbflute.logic.replaceschema.loaddata.impl.dataprop.DfLoadingControlProp;
import org.seasar.dbflute.logic.replaceschema.loaddata.interceptor.DfDataWritingInterceptor;

/**
 * @author jflute
 */
public class DfDelimiterDataHandlerImpl implements DfDelimiterDataHandler {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log instance. */
    private static final Log _log = LogFactory.getLog(DfDelimiterDataHandlerImpl.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected boolean _loggingInsertSql;
    protected DataSource _dataSource;
    protected UnifiedSchema _unifiedSchema;
    protected boolean _suppressBatchUpdate;
    protected boolean _suppressCheckColumnDef;
    protected boolean _suppressCheckImplicitSet;
    protected DfDataWritingInterceptor _dataWritingInterceptor;

    /** The data-prop of default value map. (NotNull: after initialization) */
    protected DfDefaultValueProp _defaultValueProp;

    /** The data-prop of loading control map. (NotNull: after initialization) */
    protected DfLoadingControlProp _loadingControlProp;

    // ===================================================================================
    //                                                                                Main
    //                                                                                ====
    public DfDelimiterDataResultInfo writeSeveralData(DfDelimiterDataResource resource, DfLoadedDataInfo loadedDataInfo) {
        final DfDelimiterDataResultInfo resultInfo = new DfDelimiterDataResultInfo();
        final String basePath = resource.getBasePath();
        final File baseDir = new File(basePath);
        final String[] dataDirectoryElements = baseDir.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return !name.startsWith(".");
            }
        });
        if (dataDirectoryElements == null) {
            return resultInfo;
        }
        final FilenameFilter filter = createFilenameFilter(resource.getFileType());

        try {
            for (String encoding : dataDirectoryElements) {
                if (isUnsupportedEncodingDirectory(encoding)) {
                    _log.warn("The encoding(directory name) is unsupported: encoding=" + encoding);
                    continue;
                }

                final File encodingNameDirectory = new File(basePath + "/" + encoding);
                final String[] fileNameList = encodingNameDirectory.list(filter);

                final Comparator<String> fileNameAscComparator = new Comparator<String>() {
                    public int compare(String o1, String o2) {
                        return o1.compareTo(o2);
                    }
                };
                final SortedSet<String> sortedFileNameSet = new TreeSet<String>(fileNameAscComparator);
                for (String fileName : fileNameList) {
                    sortedFileNameSet.add(fileName);
                }

                final Map<String, Map<String, String>> convertValueMap = getConvertValueMap(resource, encoding);
                final Map<String, String> defaultValueMap = getDefaultValueMap(resource, encoding);
                for (String fileName : sortedFileNameSet) {
                    final String fileNamePath = basePath + "/" + encoding + "/" + fileName;
                    final DfDelimiterDataWriterImpl writer = new DfDelimiterDataWriterImpl(_dataSource, _unifiedSchema);
                    writer.setLoggingInsertSql(isLoggingInsertSql());
                    writer.setFileName(fileNamePath);
                    writer.setEncoding(encoding);
                    writer.setDelimiter(resource.getDelimiter());
                    writer.setConvertValueMap(convertValueMap);
                    writer.setDefaultValueMap(defaultValueMap);
                    writer.setSuppressBatchUpdate(isSuppressBatchUpdate());
                    writer.setSuppressCheckColumnDef(isSuppressCheckColumnDef());
                    writer.setSuppressCheckImplicitSet(isSuppressCheckImplicitSet());
                    writer.setDataWritingInterceptor(_dataWritingInterceptor);
                    writer.setDefaultValueProp(_defaultValueProp);
                    writer.setLoadingControlProp(_loadingControlProp);
                    writer.writeData(resultInfo);
                    prepareImplicitClassificationLazyCheck(loadedDataInfo, writer);

                    final String loadType = resource.getLoadType();
                    final String fileType = resource.getFileType();
                    final boolean warned = resultInfo.getWarningFileMap().containsKey(fileNamePath);
                    loadedDataInfo.addLoadedFile(loadType, fileType, encoding, fileName, warned);
                }
            }
        } catch (IOException e) {
            String msg = "Failed to register delimiter data.";
            throw new DfDelimiterDataRegistrationFailureException(msg, e);
        }
        return resultInfo;
    }

    protected boolean isUnsupportedEncodingDirectory(String encoding) {
        try {
            new String(new byte[0], 0, 0, encoding);
            return false;
        } catch (UnsupportedEncodingException e) {
            return true;
        }
    }

    protected Map<String, String> getDefaultValueMap(DfDelimiterDataResource res, String encoding) {
        final String dataDirectory = res.getBasePath() + "/" + encoding;
        return DfXlsDataHandlerImpl.doGetDefaultValueMap(dataDirectory);
    }

    protected Map<String, Map<String, String>> getConvertValueMap(DfDelimiterDataResource res, String encoding) {
        final String dataDirectory = res.getBasePath() + "/" + encoding;
        return DfXlsDataHandlerImpl.doGetConvertValueMap(dataDirectory);
    }

    protected FilenameFilter createFilenameFilter(final String typeName) {
        final FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith("." + typeName);
            }
        };
        return filter;
    }

    protected void prepareImplicitClassificationLazyCheck(DfLoadedDataInfo info, DfDelimiterDataWriterImpl writer) {
        final List<DfLoadedClassificationLazyChecker> checkerList = writer.getImplicitClassificationLazyCheckerList();
        info.acceptImplicitClassificationLazyCheck(checkerList);
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public boolean isLoggingInsertSql() {
        return _loggingInsertSql;
    }

    public void setLoggingInsertSql(boolean loggingInsertSql) {
        this._loggingInsertSql = loggingInsertSql;
    }

    public DataSource getDataSource() {
        return _dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this._dataSource = dataSource;
    }

    public UnifiedSchema getUnifiedSchema() {
        return _unifiedSchema;
    }

    public void setUnifiedSchema(UnifiedSchema unifiedSchema) {
        this._unifiedSchema = unifiedSchema;
    }

    public boolean isSuppressBatchUpdate() {
        return _suppressBatchUpdate;
    }

    public void setSuppressBatchUpdate(boolean suppressBatchUpdate) {
        this._suppressBatchUpdate = suppressBatchUpdate;
    }

    public boolean isSuppressCheckColumnDef() {
        return _suppressCheckColumnDef;
    }

    public void setSuppressCheckColumnDef(boolean suppressCheckColumnDef) {
        this._suppressCheckColumnDef = suppressCheckColumnDef;
    }

    public boolean isSuppressCheckImplicitSet() {
        return _suppressCheckImplicitSet;
    }

    public void setSuppressCheckImplicitSet(boolean suppressCheckImplicitSet) {
        this._suppressCheckImplicitSet = suppressCheckImplicitSet;
    }

    public DfDataWritingInterceptor getDataWritingInterceptor() {
        return _dataWritingInterceptor;
    }

    public void setDataWritingInterceptor(DfDataWritingInterceptor dataWritingInterceptor) {
        this._dataWritingInterceptor = dataWritingInterceptor;
    }

    public DfDefaultValueProp getDefaultValueProp() {
        return _defaultValueProp;
    }

    public void setDefaultValueProp(DfDefaultValueProp defaultValueProp) {
        this._defaultValueProp = defaultValueProp;
    }

    public DfLoadingControlProp getLoadingControlProp() {
        return _loadingControlProp;
    }

    public void setLoadingControlProp(DfLoadingControlProp loadingControlProp) {
        this._loadingControlProp = loadingControlProp;
    }
}
