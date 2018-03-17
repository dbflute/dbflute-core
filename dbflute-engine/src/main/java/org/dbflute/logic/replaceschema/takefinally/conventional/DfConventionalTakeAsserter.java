/*
 * Copyright 2014-2018 the original author or authors.
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
package org.dbflute.logic.replaceschema.takefinally.conventional;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Supplier;

import org.dbflute.exception.DfTakeFinallyAssertionFailureEmptyTableException;
import org.dbflute.exception.SQLFailureException;
import org.dbflute.helper.jdbc.context.DfSchemaSource;
import org.dbflute.helper.jdbc.facade.DfJdbcFacade;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.logic.jdbc.metadata.basic.DfTableExtractor;
import org.dbflute.logic.jdbc.metadata.info.DfTableMeta;
import org.dbflute.logic.replaceschema.process.DfAbstractRepsProcess;
import org.dbflute.properties.DfReplaceSchemaProperties;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 1.1.7 (2018/03/17 Saturday)
 */
public class DfConventionalTakeAsserter extends DfAbstractRepsProcess {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger _log = LoggerFactory.getLogger(DfConventionalTakeAsserter.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfSchemaSource _dataSource;
    protected final Supplier<String> _dispPropertiesProvider;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfConventionalTakeAsserter(DfSchemaSource dataSource, Supplier<String> dispPropertiesProvider) {
        _dataSource = dataSource;
        _dispPropertiesProvider = dispPropertiesProvider;
    }

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    public void assertConventionally() {
        final DfReplaceSchemaProperties prop = getReplaceSchemaProperties();
        if (prop.isConventionalEmptyTableFailure()) {
            _log.info("...Checking conventional empty tables");
            if (prop.isConventionalEmptyTableWorkableEnv()) {
                doAssertEmptyTable(prop);
            } else {
                _log.info(" => out of target environment so do nothing: currentEnv=" + prop.getRepsEnvType());
            }
        }
    }

    protected void doAssertEmptyTable(DfReplaceSchemaProperties prop) {
        final List<DfTableMeta> allTableList = extractTableList();
        final List<DfTableMeta> emptyTableList = DfCollectionUtil.newArrayList();
        for (DfTableMeta tableMeta : allTableList) {
            if (prop.isConventionalEmptyTableTarget(tableMeta.getTableDbName())) {
                if (determineEmptyTable(tableMeta)) {
                    emptyTableList.add(tableMeta);
                }
            }
        }
        if (!emptyTableList.isEmpty()) {
            throwTakeFinallyAssertionFailureEmptyTableException(emptyTableList);
        }
    }

    protected List<DfTableMeta> extractTableList() {
        Connection conn = null;
        try {
            conn = _dataSource.getConnection();
            final DatabaseMetaData metaData = conn.getMetaData();
            return new DfTableExtractor().getTableList(metaData, _dataSource.getSchema());
        } catch (SQLException e) {
            throw new SQLFailureException("Failed to extract table meta list: " + _dataSource, e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    protected boolean determineEmptyTable(DfTableMeta tableMeta) {
        final DfJdbcFacade facade = new DfJdbcFacade(_dataSource);
        final int countAll = facade.selectCountAll(tableMeta.getTableSqlName());
        return countAll == 0;
    }

    protected void throwTakeFinallyAssertionFailureEmptyTableException(final List<DfTableMeta> emptyTableList) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Found the empty table (no-data) after ReplaceSchema.");
        br.addItem("Advice");
        br.addElement("The tables should have at least one record");
        br.addElement("by conventionalTakeAssertMap settings in replaceSchemaMap.dfprop:");
        br.addElement("");
        br.addElement(Srl.indent(2, _dispPropertiesProvider.get()));
        br.addElement("");
        br.addElement("So prepare the data (e.g. tsv or xls) for ReplaceSchema.");
        br.addElement("");
        br.addElement("  playsql");
        br.addElement("    |-data");
        br.addElement("    |   |-common");
        br.addElement("    |   |   |-tsv");
        br.addElement("    |   |   |-xls");
        br.addElement("    |   |-ut");
        br.addElement("    |   |   |-tsv");
        br.addElement("    |   |   |-xls");
        br.addElement("    ...");
        br.addElement("");
        br.addElement("Or adjust dfprop settings, for example,");
        br.addElement("you can except the table if it cannot be help.");
        br.addElement("(Of course, do after you ask your friends developing together)");
        br.addItem("Empty Table");
        for (DfTableMeta tableMeta : emptyTableList) {
            br.addElement(tableMeta.getTableDispName());
        }
        final String msg = br.buildExceptionMessage();
        throw new DfTakeFinallyAssertionFailureEmptyTableException(msg);
    }
}
