/*
 * Copyright 2014-2021 the original author or authors.
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
package org.dbflute.properties.initializer;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.torque.engine.database.model.Column;
import org.apache.torque.engine.database.model.Database;
import org.apache.torque.engine.database.model.ForeignKey;
import org.apache.torque.engine.database.model.Table;
import org.apache.torque.engine.database.model.UnifiedSchema;
import org.dbflute.DfBuildProperties;
import org.dbflute.exception.DfIllegalPropertySettingException;
import org.dbflute.exception.DfPropertySettingColumnNotFoundException;
import org.dbflute.exception.DfPropertySettingTableNotFoundException;
import org.dbflute.helper.StringSet;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.properties.DfAdditionalForeignKeyProperties;
import org.dbflute.properties.DfLittleAdjustmentProperties;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The initializer of additional foreign key.
 * @author jflute
 */
public class DfAdditionalForeignKeyInitializer {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The logger instance for this class. (NotNull) */
    private static final Logger _log = LoggerFactory.getLogger(DfAdditionalForeignKeyInitializer.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected Database _database;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfAdditionalForeignKeyInitializer(Database database) {
        _database = database;
    }

    // ===================================================================================
    //                                                                AdditionalForeignKey
    //                                                                ====================
    public void initializeAdditionalForeignKey() {
        _log.info("/=======================================");
        _log.info("...Initializing additional foreign keys.");
        final Map<String, Map<String, String>> additionalForeignKeyMap = getAdditionalForeignKeyMap();
        for (Entry<String, Map<String, String>> entry : additionalForeignKeyMap.entrySet()) {
            final String foreignKeyName = entry.getKey();
            final String foreignTableName = getForeignTableName(foreignKeyName);
            assertForeignTable(foreignKeyName, foreignTableName);
            final List<String> foreignColumnNameList = getForeignColumnNameList(foreignKeyName, foreignTableName);
            assertForeignTableColumn(foreignKeyName, foreignTableName, foreignColumnNameList);
            final String localTableName = getLocalTableName(foreignKeyName);

            _log.info(foreignKeyName);
            final DfAdditionalForeignKeyOption option = createFKOption(foreignKeyName);
            assertAdditionalForeignKeyOption(foreignKeyName, option);
            if (localTableName.equals("$$ALL$$") || localTableName.equals("*")) { // "*" is for compatibility
                processAllTableFK(foreignKeyName, foreignTableName, foreignColumnNameList, option);
            } else {
                processOneTableFK(foreignKeyName, localTableName, foreignTableName, foreignColumnNameList, option);
            }
        }
        _log.info("==========/");
    }

    protected DfAdditionalForeignKeyOption createFKOption(final String foreignKeyName) {
        final DfAdditionalForeignKeyOption option = new DfAdditionalForeignKeyOption();
        option.setFixedCondition(getFixedCondition(foreignKeyName));
        option.setFixedSuffix(getFixedSuffix(foreignKeyName));
        option.setFixedInline(isFixedInline(foreignKeyName));
        option.setFixedReferrer(isFixedReferrer(foreignKeyName));
        option.setFixedOnlyJoin(isFixedOnlyJoin(foreignKeyName));
        option.setComment(getComment(foreignKeyName));
        option.setSuppressJoin(isSuppressJoin(foreignKeyName));
        option.setSuppressSubQuery(isSuppressSubQuery(foreignKeyName));
        option.setDeprecated(getDeprecated(foreignKeyName));
        return option;
    }

    protected void assertAdditionalForeignKeyOption(String foreignKeyName, DfAdditionalForeignKeyOption option) {
        if (option.isFixedOnlyJoin()) {
            if (option.getFixedCondition() == null) {
                String msg = "fixedCondition is required when fixedOnlyJoin: " + foreignKeyName;
                throw new DfIllegalPropertySettingException(msg);
            }
            if (option.isFixedReferrer()) {
                String msg = "Cannot use fixedReferrer when fixedOnlyJoin: " + foreignKeyName;
                throw new DfIllegalPropertySettingException(msg);
            }
        }
    }

    protected void processAllTableFK(String foreignKeyName, String foreignTableName, List<String> foreignColumnNameList,
            DfAdditionalForeignKeyOption option) {
        if (option.isFixedOnlyJoin()) {
            String msg = "Cannot use fixedOnlyJoin when all-table FK: " + foreignKeyName;
            throw new DfIllegalPropertySettingException(msg);
        }

        // for check about same-column self reference
        final Table foreignTable = getTable(foreignTableName);
        final StringSet foreignColumnSet = StringSet.createAsFlexible();
        foreignColumnSet.addAll(foreignColumnNameList);

        for (Table table : getTableList()) {
            final String localTableName = table.getTableDbName();
            final List<String> localColumnNameList =
                    getLocalColumnNameList(table, foreignKeyName, foreignTableName, foreignColumnNameList, localTableName, option, true,
                            false);
            if (!table.containsColumn(localColumnNameList)) {
                continue;
            }

            // check same-column self reference
            final StringSet localColumnSet = StringSet.createAsFlexible();
            localColumnSet.addAll(localColumnNameList);
            final boolean selfReference = table.getTableDbName().equals(foreignTable.getTableDbName());
            if (selfReference && localColumnSet.equalsUnderCharOption(foreignColumnSet)) {
                continue;
            }

            // check same foreign key existence
            final String fixedSuffix = option.getFixedSuffix();
            final ForeignKey existingFK =
                    table.findExistingForeignKey(foreignTableName, localColumnNameList, foreignColumnNameList, fixedSuffix);
            if (existingFK != null) {
                _log.info("The foreign key has already set up: " + foreignKeyName + "(" + fixedSuffix + ")");
                reflectOptionToExistingFKIfNeeds(foreignKeyName, option, existingFK);
                continue;
            }

            final String currentForeignKeyName = foreignKeyName + "_" + toConstraintPart(localTableName);
            setupForeignKeyToTable(currentForeignKeyName, foreignTableName, foreignColumnNameList, table, localColumnNameList, option);
            showResult(foreignTableName, foreignColumnNameList, table, localColumnNameList, option);
        }
    }

    protected String toConstraintPart(String tableDbName) {
        return Srl.replace(tableDbName, ".", "_");
    }

    protected void processOneTableFK(String foreignKeyName, String localTableName, String foreignTableName,
            List<String> foreignColumnNameList, DfAdditionalForeignKeyOption option) {
        assertLocalTable(foreignKeyName, localTableName);
        final Table table = getTable(localTableName);
        final boolean searchFromExistingFK = false;
        final boolean errorIfNotFound = true;
        final List<String> localColumnNameList =
                getLocalColumnNameList(table, foreignKeyName, foreignTableName, foreignColumnNameList, localTableName, option,
                        searchFromExistingFK, errorIfNotFound);
        assertLocalTableColumn(foreignKeyName, localTableName, localColumnNameList, option);

        // check same foreign key existence
        final String fixedSuffix = getFixedSuffix(foreignKeyName);
        final ForeignKey existingFK =
                table.findExistingForeignKey(foreignTableName, localColumnNameList, foreignColumnNameList, fixedSuffix);
        if (existingFK != null) {
            _log.info("The foreign key has already set up: " + foreignKeyName + "(" + fixedSuffix + ")");
            reflectOptionToExistingFKIfNeeds(foreignKeyName, option, existingFK);
            return;
        }

        setupForeignKeyToTable(foreignKeyName, foreignTableName, foreignColumnNameList, table, localColumnNameList, option);
        showResult(foreignTableName, foreignColumnNameList, table, localColumnNameList, option);
    }

    protected void setupForeignKeyToTable(String foreignKeyName, String foreignTableName, List<String> foreignColumnNameList, Table table,
            List<String> localColumnNameList, DfAdditionalForeignKeyOption option) {
        // set up foreign key instance
        final ForeignKey fk =
                createAdditionalForeignKey(foreignKeyName, foreignTableName, localColumnNameList, foreignColumnNameList, option);
        reflectOptionToExistingFKIfNeeds(foreignKeyName, option, fk);
        table.addForeignKey(fk);

        // set up referrer instance
        final Table foreignTable = getTable(foreignTableName);
        final boolean canBeReferrer = foreignTable.addReferrer(fk);
        if (canBeReferrer) {
            for (String foreignColumnName : foreignColumnNameList) {
                final Column foreignColumn = foreignTable.getColumn(foreignColumnName);
                foreignColumn.addReferrer(fk);
            }
        } else {
            _log.info("  *Referrer setting was not allowed in this case");
        }

        // set up implicit reverse foreign key if fixed condition is valid
        // (and if a same-structured FK does not exist)
        // because biz-one-to-one needs reverse foreign key for ConditionBean's Specify
        // ...
        // ...
        // Sorry, I forgot the detail of the reason...
        // ...
        // ...
        // (2014/09/18)
        // actually, no problem for generation if suppressed
        // so suppressed (deprecated) as default since 1.1
        final DfLittleAdjustmentProperties prop = getLittleAdjustmentProperties();
        if (prop.isCompatibleBizOneToOneImplicitReverseFkAllowed()) { // basically false since 1.1
            if (fk.hasFixedCondition() && !isSuppressImplicitReverseFK(foreignKeyName)) {
                // to suppress biz-many-to-one-like biz-one-to-one
                // at any rate, if fixedReferrer, basically means BizOneToOne so unnecessary
                // but compatible just in case
                if (!fk.isFixedReferrer()) {
                    processImplicitReverseForeignKey(fk, table, foreignTable, localColumnNameList, foreignColumnNameList, option);
                }
            }
        }
    }

    protected void reflectOptionToExistingFKIfNeeds(String foreignKeyName, DfAdditionalForeignKeyOption option, final ForeignKey existingFK) {
        final String fixedSuffix = option.getFixedSuffix();
        if (option.isSuppressJoin()) {
            _log.info("...Refecting suppress join to the FK: " + foreignKeyName + "(" + fixedSuffix + ")");
            existingFK.setSuppressJoin(option.isSuppressJoin());
        }
        if (option.isSuppressSubQuery()) {
            _log.info("...Refecting suppress sub-query to the FK: " + foreignKeyName + "(" + fixedSuffix + ")");
            existingFK.setSuppressSubQuery(option.isSuppressSubQuery());
        }
        final String deprecated = option.getDeprecated();
        if (Srl.is_NotNull_and_NotTrimmedEmpty(deprecated)) {
            _log.info("...Refecting deprecated to the FK: " + foreignKeyName + "(" + fixedSuffix + ")");
            existingFK.setDeprecated(deprecated);
        }
    }

    protected void processImplicitReverseForeignKey(ForeignKey correspondingFk, Table table, Table foreignTable,
            List<String> localColumnNameList, List<String> foreignColumnNameList, DfAdditionalForeignKeyOption option) { // called only when a fixed condition exists
        // name is "FK_ + foreign + local" because it's reversed
        final String localTableName = table.getTableDbName();
        final String foreignTableName = foreignTable.getTableDbName();
        final String reverseName = buildReverseFKName(localTableName, foreignTableName);
        final String comment = "Implicit Reverse FK to " + correspondingFk.getName();
        final List<Column> primaryKey = table.getPrimaryKey();
        if (localColumnNameList.size() != primaryKey.size()) {
            return; // may be biz-many-to-one (not biz-one-to-one)
        }
        for (String localColumnName : localColumnNameList) {
            final Column localColumn = table.getColumn(localColumnName);
            if (!localColumn.isPrimaryKey()) { // check PK just in case
                return; // basically no way because a fixed condition exists
                // and FK to unique key is unsupported
            }
        }
        // here all local columns are elements of primary key
        if (foreignTable.existsForeignKey(localTableName, foreignColumnNameList, localColumnNameList)) {
            return; // same-structured FK already exists
        }
        String fixedSuffix = null;
        if (foreignTable.hasForeignTableContainsOne(table)) {
            final StringBuilder sb = new StringBuilder();
            sb.append("By");
            for (String foreignColumnName : foreignColumnNameList) {
                sb.append(foreignTable.getColumn(foreignColumnName).getJavaName());
            }
            fixedSuffix = sb.toString();
        }
        DfAdditionalForeignKeyOption implicitRes = new DfAdditionalForeignKeyOption();
        implicitRes.setFixedSuffix(fixedSuffix);
        implicitRes.setComment(comment);
        final ForeignKey fk =
                createAdditionalForeignKey(reverseName, localTableName, foreignColumnNameList, localColumnNameList, implicitRes);
        fk.setImplicitReverseForeignKey(true);
        foreignTable.addForeignKey(fk);
        final boolean canBeReferrer = table.addReferrer(fk);
        if (canBeReferrer) { // basically true because foreign columns are PK and no fixed condition
            for (String localColumnName : localColumnNameList) {
                final Column localColumn = table.getColumn(localColumnName);
                localColumn.addReferrer(fk);
            }
            _log.info("  *Reversed FK was also made implicitly");
        }
    }

    protected String buildReverseFKName(String localTableName, String foreignTableName) {
        return "FK_" + toConstraintPart(foreignTableName) + "_" + toConstraintPart(localTableName) + "_IMPLICIT";
    }

    protected ForeignKey createAdditionalForeignKey(String foreignKeyName, String foreignTableName, List<String> localColumnNameList,
            List<String> foreignColumnNameList, DfAdditionalForeignKeyOption option) {
        final ForeignKey fk = new ForeignKey();
        fk.setName(foreignKeyName);
        if (foreignTableName.contains(".")) {
            final String foreignSchema = Srl.substringLastFront(foreignTableName, ".");
            final String foreignTablePureName = Srl.substringLastRear(foreignTableName, ".");
            fk.setForeignSchema(UnifiedSchema.createAsDynamicSchema(foreignSchema));
            fk.setForeignTablePureName(foreignTablePureName);
        } else {
            fk.setForeignSchema(getDatabase().getDatabaseSchema()); // main schema
            fk.setForeignTablePureName(foreignTableName);
        }
        fk.addReference(localColumnNameList, foreignColumnNameList);
        fk.setAdditionalForeignKey(true);
        final String fixedCondition = option.getFixedCondition();
        if (Srl.is_NotNull_and_NotTrimmedEmpty(fixedCondition)) {
            fk.setFixedCondition(fixedCondition);
        }
        final String fixedSuffix = option.getFixedSuffix();
        if (Srl.is_NotNull_and_NotTrimmedEmpty(fixedSuffix)) {
            fk.setFixedSuffix(fixedSuffix);
        }
        fk.setFixedInline(option.isFixedInline());
        fk.setFixedReferrer(option.isFixedReferrer());
        fk.setFixedOnlyJoin(option.isFixedOnlyJoin());
        final String comment = option.getComment();
        if (Srl.is_NotNull_and_NotTrimmedEmpty(comment)) {
            fk.setComment(comment);
        }
        return fk;
    }

    protected void showResult(String foreignTableName, List<String> foreignColumnNameList, Table table, List<String> localColumnNameList,
            DfAdditionalForeignKeyOption option) {
        final String fixedCondition = option.getFixedCondition();
        String msg = "  Add foreign key " + table.getTableDbName() + "." + localColumnNameList;
        if (fixedCondition != null && fixedCondition.trim().length() > 0) {
            msg = msg + " to " + foreignTableName + "." + foreignColumnNameList;
            _log.info(msg);
            String withFixedCondition = "  with " + fixedCondition;
            _log.info(withFixedCondition);
        } else {
            msg = msg + " to " + foreignTableName + "." + foreignColumnNameList;
            _log.info(msg);
        }
    }

    protected List<String> getForeignColumnNameList(String foreignKeyName, final String foreignTableName) {
        List<String> foreignColumnNameList = getForeignColumnNameList(foreignKeyName);
        if (foreignColumnNameList != null && !foreignColumnNameList.isEmpty()) {
            return foreignColumnNameList;
        }
        foreignColumnNameList = DfCollectionUtil.newArrayList();
        final List<Column> foreignPrimaryKeyList = getTable(foreignTableName).getPrimaryKey();
        if (foreignPrimaryKeyList.isEmpty()) {
            final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
            br.addNotice("Not found primary key on the foreign table of additionalForeignKey.");
            br.addItem("Advice");
            br.addElement("Foreign table should have primary keys.");
            br.addItem("Additional FK");
            br.addElement(foreignKeyName);
            br.addItem("Foreign Table");
            br.addElement(foreignTableName);
            final String msg = br.buildExceptionMessage();
            throw new DfIllegalPropertySettingException(msg);
        }
        for (Column column : foreignPrimaryKeyList) {
            foreignColumnNameList.add(column.getName());
        }
        return foreignColumnNameList;
    }

    protected List<String> getLocalColumnNameList(Table table, String foreignKeyName, String foreignTableName,
            List<String> foreignColumnNameList, String localTableName, DfAdditionalForeignKeyOption option, boolean searchFromExistingFK,
            boolean errorIfNotFound) {
        List<String> localColumnNameList = getLocalColumnNameList(foreignKeyName);
        if (localColumnNameList != null && !localColumnNameList.isEmpty()) {
            return localColumnNameList;
        }

        // searching from existing foreign key
        if (searchFromExistingFK) {
            final ForeignKey existingFK = table.findExistingForeignKey(foreignTableName, foreignColumnNameList, option.getFixedSuffix());
            if (existingFK != null) {
                return existingFK.getLocalColumnNameList();
            }
        }

        // searching local columns by foreign columns (PK or UQ: PK when omitted)
        localColumnNameList = DfCollectionUtil.newArrayList();
        if (option.isFixedOnlyJoin()) { // no need to search
            return localColumnNameList;
        }
        for (String foreignColumnName : foreignColumnNameList) {
            final Column column = table.getColumn(foreignColumnName);
            if (column != null) {
                // no check about same-column self reference here
                localColumnNameList.add(column.getName());
                continue;
            }
            if (errorIfNotFound) {
                final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
                br.addNotice("Not found the local column by the foreign column of additionalForeignKey.");
                br.addItem("Advice");
                br.addElement("When localColumnName is omitted, the local table should have");
                br.addElement("the columns that are same as primary keys of foreign table.");
                br.addItem("Additional FK");
                br.addElement(foreignKeyName);
                br.addElement(option);
                br.addItem("Local Table");
                br.addElement(localTableName);
                br.addItem("Foreign Table");
                br.addElement(foreignTableName);
                br.addItem("Foreign Column");
                br.addElement(foreignColumnNameList);
                final String msg = br.buildExceptionMessage();
                throw new DfPropertySettingColumnNotFoundException(msg);
            } else {
                return DfCollectionUtil.newArrayList(); // means not found
            }
        }
        return localColumnNameList;
    }

    protected void assertForeignTable(String foreignKeyName, String foreignTableName) {
        if (getTable(foreignTableName) != null) {
            return;
        }
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found table by the foreignTableName of additionalForeignKey.");
        br.addItem("Advice");
        br.addElement("Make sure your additionalForeignKeyMap.dfprop");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("     ; FK_MEMBER_MEMBER_ADDRESS_VALID = map:{");
        br.addElement("         ; localTableName = ... ; foreignTableName = NOEXISTING_ADDRESS // *NG");
        br.addElement("         ; localColumnName = MEMBER_ID ; foreignColumnName = MEMBER_ID");
        br.addElement("         ...");
        br.addElement("     }");
        br.addElement("  (o):");
        br.addElement("     ; FK_MEMBER_MEMBER_ADDRESS_VALID = map:{");
        br.addElement("         ; localTableName = ... ; foreignTableName = MEMBER_ADDRESS     // OK");
        br.addElement("         ; localColumnName = MEMBER_ID ; foreignColumnName = MEMBER_ID");
        br.addElement("         ...");
        br.addElement("     }");
        br.addElement("");
        br.addElement("Or remove it if the table is deleted from your schema.");
        br.addItem("Additional FK");
        br.addElement(foreignKeyName);
        br.addItem("NotFound Table");
        br.addElement(foreignTableName);
        final String msg = br.buildExceptionMessage();
        throw new DfPropertySettingTableNotFoundException(msg);
    }

    protected void assertForeignTableColumn(String foreignKeyName, String foreignTableName, List<String> foreignColumnNameList) {
        if (getTable(foreignTableName).containsColumn(foreignColumnNameList)) {
            return;
        }
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found column by the foreignColumnName of additionalForeignKey.");
        br.addItem("Advice");
        br.addElement("Make sure your additionalForeignKeyMap.dfprop");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("     ; FK_MEMBER_MEMBER_ADDRESS_VALID = map:{");
        br.addElement("         ; localTableName = ... ; foreignTableName = ...");
        br.addElement("         ; localColumnName = MEMBER_ID ; foreignColumnName = NOEXISTING_ID // *NG");
        br.addElement("         ...");
        br.addElement("     }");
        br.addElement("  (o):");
        br.addElement("     ; FK_MEMBER_MEMBER_ADDRESS_VALID = map:{");
        br.addElement("         ; localTableName = ... ; foreignTableName = MEMBER_ADDRESS");
        br.addElement("         ; localColumnName = MEMBER_ID ; foreignColumnName = MEMBER_ID     // OK");
        br.addElement("         ...");
        br.addElement("     }");
        br.addItem("Additional FK");
        br.addElement(foreignKeyName);
        br.addItem("Foreign Table");
        br.addElement(foreignTableName);
        br.addItem("NotFound Column");
        br.addElement(foreignColumnNameList);
        final String msg = br.buildExceptionMessage();
        throw new DfPropertySettingColumnNotFoundException(msg);
    }

    protected void assertLocalTable(final String foreignKeyName, final String localTableName) {
        if (getTable(localTableName) != null) {
            return;
        }
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found table by the localTableName of additionalForeignKey.");
        br.addItem("Advice");
        br.addElement("Make sure your additionalForeignKeyMap.dfprop");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("     ; FK_MEMBER_MEMBER_ADDRESS_VALID = map:{");
        br.addElement("         ; localTableName = NOEXISTING_STATUS ; foreignTableName = ... // *NG");
        br.addElement("         ; localColumnName = MEMBER_ID ; foreignColumnName = MEMBER_ID");
        br.addElement("         ...");
        br.addElement("     }");
        br.addElement("  (o):");
        br.addElement("     ; FK_MEMBER_MEMBER_ADDRESS_VALID = map:{");
        br.addElement("         ; localTableName = MEMBER_STATUS ; foreignTableName = ...     // OK");
        br.addElement("         ; localColumnName = MEMBER_ID ; foreignColumnName = MEMBER_ID");
        br.addElement("         ...");
        br.addElement("     }");
        br.addElement("");
        br.addElement("Or remove it if the table is deleted from your schema.");
        br.addItem("Additional FK");
        br.addElement(foreignKeyName);
        br.addItem("NotFound Table");
        br.addElement(localTableName);
        final String msg = br.buildExceptionMessage();
        throw new DfPropertySettingTableNotFoundException(msg);
    }

    protected void assertLocalTableColumn(final String foreignKeyName, final String localTableName, List<String> localColumnNameList,
            DfAdditionalForeignKeyOption resource) {
        if (resource.isFixedOnlyJoin()) {
            if (!localColumnNameList.isEmpty()) {
                final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
                br.addNotice("The localColumn should be omitted when fixedOnlyJoin is true.");
                br.addItem("Additional FK");
                br.addElement(foreignKeyName);
                br.addItem("Local Table");
                br.addElement(localTableName);
                br.addItem("Column List");
                br.addElement(localColumnNameList);
                final String msg = br.buildExceptionMessage();
                throw new DfIllegalPropertySettingException(msg);
            }
        } else {
            if (!getTable(localTableName).containsColumn(localColumnNameList)) {
                final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
                br.addNotice("Not found column by the localColumnName of additionalForeignKey.");
                br.addElement("Make sure your additionalForeignKeyMap.dfprop");
                br.addElement("For example:");
                br.addElement("  (x):");
                br.addElement("     ; FK_MEMBER_MEMBER_ADDRESS_VALID = map:{");
                br.addElement("         ; localTableName = ... ; foreignTableName = ...");
                br.addElement("         ; localColumnName = NOEXISTING_ID ; foreignColumnName = MEMBER_ID // *NG");
                br.addElement("         ...");
                br.addElement("     }");
                br.addElement("  (o):");
                br.addElement("     ; FK_MEMBER_MEMBER_ADDRESS_VALID = map:{");
                br.addElement("         ; localTableName = ... ; foreignTableName = MEMBER_ADDRESS");
                br.addElement("         ; localColumnName = MEMBER_ID ; foreignColumnName = MEMBER_ID     // OK");
                br.addElement("         ...");
                br.addElement("     }");
                br.addItem("Additional FK");
                br.addElement(foreignKeyName);
                br.addItem("Local Table");
                br.addElement(localTableName);
                br.addItem("NotFound Column");
                br.addElement(localColumnNameList);
                final String msg = br.buildExceptionMessage();
                throw new DfPropertySettingColumnNotFoundException(msg);
            }
        }
    }

    protected String getLocalTableName(String foreignKeyName) {
        return getProperties().findLocalTableName(foreignKeyName);
    }

    protected String getForeignTableName(String foreignKeyName) {
        return getProperties().findForeignTableName(foreignKeyName);
    }

    protected List<String> getLocalColumnNameList(String foreignKeyName) {
        return getProperties().findLocalColumnNameList(foreignKeyName);
    }

    protected List<String> getForeignColumnNameList(String foreignKeyName) {
        return getProperties().findForeignColumnNameList(foreignKeyName);
    }

    protected String getFixedCondition(String foreignKeyName) {
        return getProperties().findFixedCondition(foreignKeyName);
    }

    protected String getFixedSuffix(String foreignKeyName) {
        return getProperties().findFixedSuffix(foreignKeyName);
    }

    protected boolean isFixedInline(String foreignKeyName) {
        return getProperties().isFixedInline(foreignKeyName);
    }

    protected boolean isFixedReferrer(String foreignKeyName) {
        return getProperties().isFixedReferrer(foreignKeyName);
    }

    protected boolean isFixedOnlyJoin(String foreignKeyName) {
        return getProperties().isFixedOnlyJoin(foreignKeyName);
    }

    protected String getComment(String foreignKeyName) {
        return getProperties().findComment(foreignKeyName);
    }

    protected boolean isSuppressJoin(String foreignKeyName) {
        return getProperties().isSuppressJoin(foreignKeyName);
    }

    protected boolean isSuppressSubQuery(String foreignKeyName) {
        return getProperties().isSuppressSubQuery(foreignKeyName);
    }

    protected String getDeprecated(String foreignKeyName) {
        return getProperties().findDeprecated(foreignKeyName);
    }

    protected boolean isSuppressImplicitReverseFK(String foreignKeyName) {
        return getProperties().isSuppressImplicitReverseFK(foreignKeyName);
    }

    protected Map<String, Map<String, String>> getAdditionalForeignKeyMap() {
        return getProperties().getAdditionalForeignKeyMap();
    }

    protected Table getTable(String tableName) {
        return getDatabase().getTable(tableName);
    }

    protected List<Table> getTableList() {
        return getDatabase().getTableList();
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfAdditionalForeignKeyProperties getProperties() {
        return DfBuildProperties.getInstance().getAdditionalForeignKeyProperties();
    }

    protected DfLittleAdjustmentProperties getLittleAdjustmentProperties() {
        return DfBuildProperties.getInstance().getLittleAdjustmentProperties();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    protected Database getDatabase() {
        return _database;
    }
}