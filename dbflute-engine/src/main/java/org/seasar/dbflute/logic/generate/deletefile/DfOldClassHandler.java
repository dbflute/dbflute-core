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
package org.seasar.dbflute.logic.generate.deletefile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.engine.database.model.Table;
import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.friends.velocity.DfGenerator;
import org.seasar.dbflute.logic.generate.language.pkgstyle.DfLanguageClassPackage;
import org.seasar.dbflute.logic.generate.packagepath.DfPackagePathHandler;
import org.seasar.dbflute.logic.sql2entity.pmbean.DfPmbMetaData;
import org.seasar.dbflute.properties.DfBasicProperties;
import org.seasar.dbflute.properties.DfLittleAdjustmentProperties;
import org.seasar.dbflute.properties.DfOutsideSqlProperties;
import org.seasar.dbflute.properties.DfSimpleDtoProperties;

/**
 * @author jflute
 * @since 0.7.8 (2008/08/23 Saturday)
 */
public class DfOldClassHandler {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log instance. */
    private static final Log _log = LogFactory.getLog(DfOldClassHandler.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected DfGenerator _generator;
    protected DfLanguageClassPackage _generatedClassPkg;
    protected List<Table> _tableList;
    protected Map<String, Map<String, Table>> _cmentityLocationMap;
    protected Map<String, Map<String, DfPmbMetaData>> _pmbLocationMap;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfOldClassHandler(DfGenerator generator, DfLanguageClassPackage generatedClassPkg, List<Table> tableList) {
        _generator = generator;
        _generatedClassPkg = generatedClassPkg;
        _tableList = tableList;
    }

    // ===================================================================================
    //                                                                     Old Table Class
    //                                                                     ===============
    public void deleteOldTableClass() {
        info("public void deleteOldTableClass() {");
        deleteOldTableClass_for_BaseBehavior();
        deleteOldTableClass_for_ReferrerLoader();
        deleteOldTableClass_for_BaseDao();
        deleteOldTableClass_for_BaseEntity();
        deleteOldTableClass_for_ImmutableBaseEntity();
        deleteOldTableClass_for_DBMeta();
        deleteOldTableClass_for_BaseConditionBean();
        deleteOldTableClass_for_AbstractBaseConditionQuery();
        deleteOldTableClass_for_BaseConditionQuery();
        deleteOldTableClass_for_NestSelectSetupper();
        deleteOldTableClass_for_ExtendedConditionBean();
        deleteOldTableClass_for_ExtendedConditionQuery();
        deleteOldTableClass_for_ExtendedConditionInlineQuery();
        deleteOldTableClass_for_ExtendedBehavior();
        deleteOldTableClass_for_ExtendedDao();
        deleteOldTableClass_for_ExtendedEntity();
        deleteOldTableClass_for_ImmutableExtendedEntity();
        info("}");
    }

    // -----------------------------------------------------
    //                                            Base Class
    //                                            ----------
    protected List<String> _deletedOldTableBaseBehaviorList;

    public void deleteOldTableClass_for_BaseBehavior() {
        final NotDeleteTCNSetupper setupper = new NotDeleteTCNSetupper() {
            public String setup(Table table) {
                if (!table.hasBehavior()) {
                    return null; // delete the class though existing table
                }
                if (getBasicProperties().isApplicationBehaviorProject()) {
                    return table.getBaseBehaviorApClassName();
                } else {
                    return table.getBaseBehaviorClassName();
                }
            }
        };
        final String packagePath = getBaseBehaviorPackage();
        final String classPrefix = getProjectPrefix() + getBasePrefix();
        final String classSuffix;
        if (getBasicProperties().isApplicationBehaviorProject()) {
            final String additionalSuffix = getBasicProperties().getApplicationBehaviorAdditionalSuffix();
            classSuffix = "Bhv" + additionalSuffix;
        } else {
            classSuffix = "Bhv";
        }
        final DfOldTableClassDeletor deletor = createTCD(packagePath, classPrefix, classSuffix, setupper);
        _deletedOldTableBaseBehaviorList = deletor.deleteOldTableClass();
        showDeleteOldTableFile(_deletedOldTableBaseBehaviorList);
    }

    protected String getBaseBehaviorPackage() {
        return getBasicProperties().getBaseBehaviorPackage();
    }

    public void deleteOldTableClass_for_ReferrerLoader() {
        final NotDeleteTCNSetupper setupper = new NotDeleteTCNSetupper() {
            public String setup(Table table) {
                if (!table.hasReferrerLoader()) {
                    return null; // delete the class though existing table
                }
                return table.getReferrerLoaderClassName();
            }
        };
        final String packagePath = getReferrerLoaderPackage();
        final String classPrefix = getProjectPrefix() + "LoaderOf";
        final DfOldTableClassDeletor deletor = createTCD(packagePath, classPrefix, null, setupper);
        showDeleteOldTableFile(deletor.deleteOldTableClass());
    }

    protected String getReferrerLoaderPackage() {
        return getBasicProperties().getReferrerLoaderPackage();
    }

    protected List<String> _deletedOldTableBaseDaoList;

    public void deleteOldTableClass_for_BaseDao() {
        final NotDeleteTCNSetupper setupper = new NotDeleteTCNSetupper() {
            public String setup(Table table) {
                if (!table.hasBehavior()) {
                    return null; // delete the class though existing table
                }
                return table.getBaseDaoClassName();
            }
        };
        final String packagePath = getBaseDaoPackage();
        final String classPrefix = getProjectPrefix() + getBasePrefix();
        final DfOldTableClassDeletor deletor = createTCD(packagePath, classPrefix, "Dao", setupper);
        _deletedOldTableBaseDaoList = deletor.deleteOldTableClass();
        showDeleteOldTableFile(_deletedOldTableBaseDaoList);
    }

    protected String getBaseDaoPackage() {
        return getBasicProperties().getBaseDaoPackage();
    }

    protected List<String> _deletedOldTableBaseEntityList;

    public void deleteOldTableClass_for_BaseEntity() {
        final String packagePath = getBaseEntityPackage();
        final String classPrefix = getProjectPrefix() + getBasePrefix() + getEntityDBablePrefix();
        final NotDeleteTCNSetupper[] setuppers = getBaseEntitySetuppers();
        final DfOldTableClassDeletor deletor = createTCD(packagePath, classPrefix, null, setuppers);
        _deletedOldTableBaseEntityList = deletor.deleteOldTableClass();
        showDeleteOldTableFile(_deletedOldTableBaseEntityList);
    }

    protected List<String> _deletedOldTableImmutableBaseEntityList;

    public void deleteOldTableClass_for_ImmutableBaseEntity() {
        if (!isMakeImmutableEntity()) {
            return;
        }
        final String packagePath = getBaseEntityPackage();
        final String classPrefix = getProjectPrefix() + getBasePrefix();
        final NotDeleteTCNSetupper[] setuppers = getBaseEntitySetuppers();
        final DfOldTableClassDeletor deletor = createTCD(packagePath, classPrefix, null, setuppers);
        _deletedOldTableImmutableBaseEntityList = deletor.deleteOldTableClass();
        showDeleteOldTableFile(_deletedOldTableImmutableBaseEntityList);
    }

    protected NotDeleteTCNSetupper[] getBaseEntitySetuppers() {
        List<NotDeleteTCNSetupper> setupperList = new ArrayList<NotDeleteTCNSetupper>();
        setupperList.add(new NotDeleteTCNSetupper() {
            public String setup(Table table) {
                return table.getBaseEntityClassName();
            }
        });
        if (isMakeImmutableEntity()) {
            setupperList.add(new NotDeleteTCNSetupper() {
                public String setup(Table table) {
                    return table.getImmutableBaseEntityClassName();
                }
            });
        }
        return setupperList.toArray(new NotDeleteTCNSetupper[] {});
    }

    protected String getBaseEntityPackage() {
        return getBasicProperties().getBaseEntityPackage();
    }

    public void deleteOldTableClass_for_DBMeta() {
        final NotDeleteTCNSetupper setupper = new NotDeleteTCNSetupper() {
            public String setup(Table table) {
                return table.getDBMetaClassName();
            }
        };
        final String packagePath = getDBMetaPackage();
        final String classPrefix = getProjectPrefix();
        final DfOldTableClassDeletor deletor = createTCD(packagePath, classPrefix, "Dbm", setupper);
        showDeleteOldTableFile(deletor.deleteOldTableClass());
    }

    protected String getDBMetaPackage() {
        return getBasicProperties().getDBMetaPackage();
    }

    public void deleteOldTableClass_for_BaseConditionBean() {
        final NotDeleteTCNSetupper setupper = new NotDeleteTCNSetupper() {
            public String setup(Table table) {
                return table.getBaseConditionBeanClassName();
            }
        };
        final String packagePath = getConditionBeanPackage() + ".bs";
        final String classPrefix = getProjectPrefix() + getBasePrefix();
        final DfOldTableClassDeletor deletor = createTCD(packagePath, classPrefix, "CB", setupper);
        showDeleteOldTableFile(deletor.deleteOldTableClass());
    }

    protected String getConditionBeanPackage() {
        return getBasicProperties().getConditionBeanPackage();
    }

    public void deleteOldTableClass_for_AbstractBaseConditionQuery() {
        final NotDeleteTCNSetupper setupper = new NotDeleteTCNSetupper() {
            public String setup(Table table) {
                return table.getAbstractBaseConditionQueryClassName();
            }
        };
        final String packagePath = getConditionBeanPackage() + ".cq.bs";
        final String classPrefix = getProjectPrefix() + "Abstract" + getBasePrefix();
        final DfOldTableClassDeletor deletor = createTCD(packagePath, classPrefix, "CQ", setupper);
        showDeleteOldTableFile(deletor.deleteOldTableClass());
    }

    public void deleteOldTableClass_for_BaseConditionQuery() {
        final NotDeleteTCNSetupper setupper = new NotDeleteTCNSetupper() {
            public String setup(Table table) {
                return table.getBaseConditionQueryClassName();
            }
        };
        final String packagePath = getConditionBeanPackage() + ".cq.bs";
        final String classPrefix = getProjectPrefix() + getBasePrefix();
        final DfOldTableClassDeletor deletor = createTCD(packagePath, classPrefix, "CQ", setupper);
        showDeleteOldTableFile(deletor.deleteOldTableClass());
    }

    public void deleteOldTableClass_for_NestSelectSetupper() {
        final NotDeleteTCNSetupper setupper = new NotDeleteTCNSetupper() {
            public String setup(Table table) {
                return table.hasNestSelectSetupper() ? table.getNestSelectSetupperClassName() : null;
            }
        };
        final String packagePath = getConditionBeanPackage() + ".nss";
        final String classPrefix = getProjectPrefix();
        final DfOldTableClassDeletor deletor = createTCD(packagePath, classPrefix, "Nss", setupper);
        showDeleteOldTableFile(deletor.deleteOldTableClass());
    }

    // -----------------------------------------------------
    //                                        Extended Class
    //                                        --------------
    public void deleteOldTableClass_for_ExtendedConditionBean() {
        final NotDeleteTCNSetupper setupper = new NotDeleteTCNSetupper() {
            public String setup(Table table) {
                return table.getExtendedConditionBeanClassName();
            }
        };
        final String packagePath = getConditionBeanPackage();
        final String classPrefix = getProjectPrefix();
        final DfOldTableClassDeletor deletor = createTCD(packagePath, classPrefix, "CB", setupper);
        showDeleteOldTableFile(deletor.deleteOldTableClass());
    }

    public void deleteOldTableClass_for_ExtendedConditionQuery() {
        final NotDeleteTCNSetupper setupper = new NotDeleteTCNSetupper() {
            public String setup(Table table) {
                return table.getExtendedConditionQueryClassName();
            }
        };
        final String packagePath = getConditionBeanPackage() + ".cq";
        final String classPrefix = getProjectPrefix();
        final DfOldTableClassDeletor deletor = createTCD(packagePath, classPrefix, "CQ", setupper);
        showDeleteOldTableFile(deletor.deleteOldTableClass());
    }

    public void deleteOldTableClass_for_ExtendedConditionInlineQuery() {
        final NotDeleteTCNSetupper setupper = new NotDeleteTCNSetupper() {
            public String setup(Table table) {
                return table.hasConditionInlineQuery() ? table.getExtendedConditionInlineQueryClassName() : null;
            }
        };
        final String packagePath = getConditionBeanPackage() + ".cq.ciq";
        final String classPrefix = getProjectPrefix();
        final DfOldTableClassDeletor deletor = createTCD(packagePath, classPrefix, "CIQ", setupper);
        showDeleteOldTableFile(deletor.deleteOldTableClass());
    }

    public void deleteOldTableClass_for_ExtendedBehavior() {
        if (_deletedOldTableBaseBehaviorList == null || _deletedOldTableBaseBehaviorList.isEmpty()) {
            return;
        }
        final String outputPath = _generator.getOutputPath();
        final String packagePath = getExtendedBehaviorPackage();
        final DfPackagePathHandler packagePathHandler = createPackagePathHandler();
        final String dirPath = outputPath + "/" + packagePathHandler.getPackageAsPath(packagePath);
        for (String baseClassName : _deletedOldTableBaseBehaviorList) {
            final int prefixLength = getProjectPrefix().length() + getBasePrefix().length();
            final String extendedClassName = getProjectPrefix() + baseClassName.substring(prefixLength);
            final File file = new File(dirPath + "/" + extendedClassName + "." + getClassFileExtension());
            if (file.exists()) {
                file.delete();
                info("    delete('" + extendedClassName + "');");
            }
        }
    }

    protected String getExtendedBehaviorPackage() {
        return getBasicProperties().getExtendedBehaviorPackage();
    }

    public void deleteOldTableClass_for_ExtendedDao() {
        if (_deletedOldTableBaseDaoList == null || _deletedOldTableBaseDaoList.isEmpty()) {
            return;
        }
        final String outputPath = _generator.getOutputPath();
        final String packagePath = getExtendedDaoPackage();
        final DfPackagePathHandler packagePathHandler = createPackagePathHandler();
        final String dirPath = outputPath + "/" + packagePathHandler.getPackageAsPath(packagePath);
        for (String baseClassName : _deletedOldTableBaseDaoList) {
            final int prefixLength = getProjectPrefix().length() + getBasePrefix().length();
            final String extendedClassName = getProjectPrefix() + baseClassName.substring(prefixLength);
            final File file = new File(dirPath + "/" + extendedClassName + "." + getClassFileExtension());
            if (file.exists()) {
                file.delete();
                info("    delete('" + extendedClassName + "');");
            }
        }
    }

    protected String getExtendedDaoPackage() {
        return getBasicProperties().getExtendedDaoPackage();
    }

    public void deleteOldTableClass_for_ExtendedEntity() {
        if (_deletedOldTableBaseEntityList == null || _deletedOldTableBaseEntityList.isEmpty()) {
            return;
        }
        final String outputPath = _generator.getOutputPath();
        final String packagePath = getExtendedEntityPackage();
        final DfPackagePathHandler packagePathHandler = createPackagePathHandler();
        final String dirPath = outputPath + "/" + packagePathHandler.getPackageAsPath(packagePath);
        final String projectPrefix = getProjectPrefix();
        final String entityDBablePrefix = getEntityDBablePrefix();
        for (String baseClassName : _deletedOldTableBaseEntityList) {
            final String prefix = projectPrefix + getBasePrefix() + entityDBablePrefix;
            final int prefixLen = prefix.length();
            final String extendedClassName = projectPrefix + entityDBablePrefix + baseClassName.substring(prefixLen);
            final File file = new File(dirPath + "/" + extendedClassName + "." + getClassFileExtension());
            if (file.exists()) {
                file.delete();
                info("    delete('" + extendedClassName + "');");
            }
        }
    }

    public void deleteOldTableClass_for_ImmutableExtendedEntity() {
        if (!isMakeImmutableEntity()) {
            return;
        }
        if (_deletedOldTableImmutableBaseEntityList == null || _deletedOldTableImmutableBaseEntityList.isEmpty()) {
            return;
        }
        final String outputPath = _generator.getOutputPath();
        final String packagePath = getExtendedEntityPackage();
        final DfPackagePathHandler packagePathHandler = createPackagePathHandler();
        final String dirPath = outputPath + "/" + packagePathHandler.getPackageAsPath(packagePath);
        final String projectPrefix = getProjectPrefix();
        for (String baseClassName : _deletedOldTableImmutableBaseEntityList) {
            final String prefix = projectPrefix + getBasePrefix();
            final int prefixLen = prefix.length();
            final String extendedClassName = projectPrefix + baseClassName.substring(prefixLen);
            final File file = new File(dirPath + "/" + extendedClassName + "." + getClassFileExtension());
            if (file.exists()) {
                file.delete();
                info("    delete('" + extendedClassName + "');");
            }
        }
    }

    protected String getExtendedEntityPackage() {
        return getBasicProperties().getExtendedEntityPackage();
    }

    // -----------------------------------------------------
    //                                         Common Helper
    //                                         -------------
    protected void showDeleteOldTableFile(List<String> deletedClassNameList) {
        for (Object className : deletedClassNameList) {
            info("    delete('" + className + "');");
        }
    }

    protected void showDeleteOldTableFile(Map<String, List<String>> deletedClassNameListMap) {
        Set<Entry<String, List<String>>> entrySet = deletedClassNameListMap.entrySet();
        for (Entry<String, List<String>> entry : entrySet) {
            final List<String> deletedClassNameList = entry.getValue();
            for (Object className : deletedClassNameList) {
                info("    delete('" + className + "');");
            }
        }
    }

    protected DfOldTableClassDeletor createTCD(String packagePath, String classPrefix, String classSuffix,
            NotDeleteTCNSetupper... setuppers) { // createOldTableClassDeletor()
        final DfOldTableClassDeletor deletor = new DfOldTableClassDeletor(_generator.getOutputPath(),
                createPackagePathHandler());
        deletor.addPackagePath(packagePath);
        deletor.setClassPrefix(classPrefix);
        deletor.setClassSuffix(classSuffix);
        deletor.setClassExtension(getClassFileExtension());
        deletor.setNotDeleteClassNameSet(createNotDeleteTCNSet(setuppers));
        return deletor;
    }

    protected static interface NotDeleteTCNSetupper { // NotDeleteTableClassNameSetupper
        public String setup(Table table);
    }

    protected Set<String> createNotDeleteTCNSet(NotDeleteTCNSetupper... setuppers) {
        final Set<String> notDeleteClassNameSet = new HashSet<String>();
        for (NotDeleteTCNSetupper setupper : setuppers) {
            final List<Table> tableList = getTableList();
            for (Table table : tableList) {
                final String tableName = setupper.setup(table);
                if (tableName != null) {
                    notDeleteClassNameSet.add(tableName);
                }
            }
        }
        return notDeleteClassNameSet;
    }

    // ===================================================================================
    //                                                                 Old Customize Class
    //                                                                 ===================
    public void deleteOldCustomizeClass() {
        info("public void deleteOldCustomizeClass() {");
        deleteOldCustomizeClass_for_BaseCustomizeEntity();
        deleteOldCustomizeClass_for_ImmutableBaseCustomizeEntity();
        deleteOldCustomizeClass_for_DBMeta();
        deleteOldCustomizeClass_for_BaseCursor();
        deleteOldCustomizeClass_for_BaseCursorHandler();
        deleteOldCustomizeClass_for_BaseParameterBean();
        deleteOldCustomizeClass_for_ExtendedCustomizeEntity();
        deleteOldCustomizeClass_for_ImmutableExtendedCustomizeEntity();
        deleteOldCustomizeClass_for_ExtendedCursor();
        deleteOldCustomizeClass_for_ExtendedCursorHandler();
        deleteOldCustomizeClass_for_ExtendedParameterBean();
        info("}");
    }

    // -----------------------------------------------------
    //                                            Base Class
    //                                            ----------
    protected Map<String, List<String>> _deletedOldCustomizeBaseEntityListMap;

    public void deleteOldCustomizeClass_for_BaseCustomizeEntity() {
        if (_cmentityLocationMap == null) {
            return;
        }
        final String customizePackageName = _generatedClassPkg.getCustomizeEntitySimplePackage();
        final String packagePath = getBaseEntityPackage() + "." + customizePackageName;
        _deletedOldCustomizeBaseEntityListMap = new LinkedHashMap<String, List<String>>();
        final String classPrefix = getProjectPrefix() + getBasePrefix() + getEntityDBablePrefix();
        for (Entry<String, Map<String, Table>> entry : _cmentityLocationMap.entrySet()) {
            final String outputDirectory = entry.getKey();
            // *no need to use because tableList already exists
            //final Map<String, Table> elementMap = entry.getValue();
            final NotDeleteTCNSetupper[] setuppers = getCustomizeBaseEntitySetuppers(outputDirectory);
            final DfOldTableClassDeletor deletor = createCCD(outputDirectory, packagePath, classPrefix, null, setuppers);
            final List<String> deletedList = deletor.deleteOldTableClass();
            _deletedOldCustomizeBaseEntityListMap.put(outputDirectory, deletedList);
        }
        showDeleteOldTableFile(_deletedOldCustomizeBaseEntityListMap);
    }

    protected Map<String, List<String>> _deletedOldImmutableCustomizeBaseEntityListMap;

    public void deleteOldCustomizeClass_for_ImmutableBaseCustomizeEntity() {
        if (_cmentityLocationMap == null) {
            return;
        }
        final String customizePackageName = _generatedClassPkg.getCustomizeEntitySimplePackage();
        final String packagePath = getBaseEntityPackage() + "." + customizePackageName;
        _deletedOldImmutableCustomizeBaseEntityListMap = new LinkedHashMap<String, List<String>>();
        final String classPrefix = getProjectPrefix() + getBasePrefix();
        for (Entry<String, Map<String, Table>> entry : _cmentityLocationMap.entrySet()) {
            final String outputDirectory = entry.getKey();
            // *no need to use because tableList already exists
            //final Map<String, Table> elementMap = entry.getValue();
            final NotDeleteTCNSetupper[] setuppers = getCustomizeBaseEntitySetuppers(outputDirectory);
            final DfOldTableClassDeletor deletor = createCCD(outputDirectory, packagePath, classPrefix, null, setuppers);
            final List<String> deletedList = deletor.deleteOldTableClass();
            _deletedOldImmutableCustomizeBaseEntityListMap.put(outputDirectory, deletedList);
        }
        showDeleteOldTableFile(_deletedOldImmutableCustomizeBaseEntityListMap);
    }

    protected NotDeleteTCNSetupper[] getCustomizeBaseEntitySetuppers(final String outputDirectory) {
        final List<NotDeleteTCNSetupper> setupperList = new ArrayList<NotDeleteTCNSetupper>();
        setupperList.add(new NotDeleteTCNSetupper() {
            public String setup(Table table) {
                if (isSameAsSql2EntityOutputDirectory(table, outputDirectory)) {
                    return table.getBaseEntityClassName();
                }
                return null;
            }
        });
        if (isMakeImmutableEntity()) {
            setupperList.add(new NotDeleteTCNSetupper() {
                public String setup(Table table) {
                    if (isSameAsSql2EntityOutputDirectory(table, outputDirectory)) {
                        return table.getImmutableBaseEntityClassName();
                    }
                    return null;
                }
            });
        }
        return setupperList.toArray(new NotDeleteTCNSetupper[] {});
    }

    public void deleteOldCustomizeClass_for_DBMeta() { // has no extended class
        if (_cmentityLocationMap == null) {
            return;
        }
        final String customizePackageName = _generatedClassPkg.getCustomizeEntitySimplePackage();
        final String dbmetaSimplePackageName = _generatedClassPkg.getDBMetaSimplePackage();
        final String packagePath = getBaseEntityPackage() + "." + customizePackageName + "." + dbmetaSimplePackageName;
        final String classPrefix = getProjectPrefix();
        final String classSuffix = "Dbm";
        final Map<String, List<String>> deletedListMap = new LinkedHashMap<String, List<String>>(); // only for logging
        for (Entry<String, Map<String, Table>> entry : _cmentityLocationMap.entrySet()) {
            final String outputDirectory = entry.getKey();
            // *no need to use because tableList already exists
            //final Map<String, Table> elementMap = entry.getValue();
            final NotDeleteTCNSetupper setupper = new NotDeleteTCNSetupper() {
                public String setup(Table table) {
                    if (isSameAsSql2EntityOutputDirectory(table, outputDirectory)) {
                        return table.getDBMetaClassName();
                    }
                    return null;
                }
            };
            final DfOldTableClassDeletor deletor = createCCD(outputDirectory, packagePath, classPrefix, classSuffix,
                    setupper);
            final List<String> deletedList = deletor.deleteOldTableClass();
            deletedListMap.put(outputDirectory, deletedList);
        }
        showDeleteOldTableFile(deletedListMap);
    }

    protected Map<String, List<String>> _deletedOldCustomizeBaseCursorListMap;

    public void deleteOldCustomizeClass_for_BaseCursor() {
        if (_cmentityLocationMap == null) {
            return;
        }
        final String cursorPackageName = _generatedClassPkg.getCursorSimplePackage();
        final String packagePath = getBaseBehaviorPackage() + "." + cursorPackageName;
        final String oldStylePackagePath = getBaseDaoPackage() + "." + cursorPackageName;
        final String classPrefix = getProjectPrefix() + getBasePrefix();
        final String classSuffix = "Cursor";
        _deletedOldCustomizeBaseCursorListMap = new LinkedHashMap<String, List<String>>();
        for (Entry<String, Map<String, Table>> entry : _cmentityLocationMap.entrySet()) {
            final String outputDirectory = entry.getKey();
            // *no need to use because tableList already exists
            //final Map<String, Table> elementMap = entry.getValue();
            final NotDeleteTCNSetupper setupper = new NotDeleteTCNSetupper() {
                public String setup(Table table) {
                    if (isSameAsSql2EntityOutputDirectory(table, outputDirectory)) {
                        return table.getBaseEntityClassName() + classSuffix;
                    }
                    return null;
                }
            };
            final DfOldTableClassDeletor deletor = createCCD(outputDirectory, packagePath, classPrefix, classSuffix,
                    setupper);
            if (oldStylePackagePath != null) { // e.g. cursor
                deletor.addPackagePath(oldStylePackagePath);
            }
            final List<String> deletedList = deletor.deleteOldTableClass();
            _deletedOldCustomizeBaseCursorListMap.put(outputDirectory, deletedList);
        }
        showDeleteOldTableFile(_deletedOldCustomizeBaseCursorListMap);
    }

    protected Map<String, List<String>> _deletedOldCustomizeBaseCursorHandlerListMap;

    public void deleteOldCustomizeClass_for_BaseCursorHandler() {
        if (_cmentityLocationMap == null) {
            return;
        }
        final String cursorPackageName = _generatedClassPkg.getCursorSimplePackage();
        final String packagePath = getBaseBehaviorPackage() + "." + cursorPackageName;
        final String oldStylePackagePath = getBaseDaoPackage() + "." + cursorPackageName;
        final String classPrefix = getProjectPrefix() + getBasePrefix();
        final String classSuffix = "CursorHandler";
        _deletedOldCustomizeBaseCursorHandlerListMap = new LinkedHashMap<String, List<String>>();
        for (Entry<String, Map<String, Table>> entry : _cmentityLocationMap.entrySet()) {
            final String outputDirectory = entry.getKey();
            // *no need to use because tableList already exists
            //final Map<String, Table> elementMap = entry.getValue();
            final NotDeleteTCNSetupper setupperWrapper = new NotDeleteTCNSetupper() {
                public String setup(Table table) {
                    if (isSameAsSql2EntityOutputDirectory(table, outputDirectory)) {
                        return table.getBaseEntityClassName() + classSuffix;
                    }
                    return null;
                }
            };
            final DfOldTableClassDeletor deletor = createCCD(outputDirectory, packagePath, classPrefix, classSuffix,
                    setupperWrapper);
            if (oldStylePackagePath != null) { // e.g. cursor
                deletor.addPackagePath(oldStylePackagePath);
            }
            final List<String> deletedList = deletor.deleteOldTableClass();
            _deletedOldCustomizeBaseCursorHandlerListMap.put(outputDirectory, deletedList);
        }
        showDeleteOldTableFile(_deletedOldCustomizeBaseCursorHandlerListMap);
    }

    protected boolean isSameAsSql2EntityOutputDirectory(Table table, String outputDirectory) {
        return table.getSql2EntityOutputDirectory().equals(outputDirectory);
    }

    protected Map<String, List<String>> _deletedOldCustomizeBaseParameterBeanListMap;

    public void deleteOldCustomizeClass_for_BaseParameterBean() {
        if (_pmbLocationMap == null) {
            return;
        }
        final String parameterBeanPackageName = _generatedClassPkg.getParameterBeanSimplePackage();
        final String packagePath = getBaseBehaviorPackage() + "." + parameterBeanPackageName;
        final String oldStylePackagePath = getBaseDaoPackage() + "." + parameterBeanPackageName;
        final String classPrefix = getProjectPrefix() + getBasePrefix();
        _deletedOldCustomizeBaseParameterBeanListMap = new LinkedHashMap<String, List<String>>();
        for (Entry<String, Map<String, DfPmbMetaData>> entry : _pmbLocationMap.entrySet()) { // loop per location
            final String outputDirectory = entry.getKey();
            final Map<String, DfPmbMetaData> elementMap = entry.getValue();
            final Set<String> notDeleteClassNameSet = new HashSet<String>();
            for (String pmbName : elementMap.keySet()) {
                notDeleteClassNameSet.add(getProjectPrefix() + getBasePrefix() + pmbName);
            }
            final DfOldTableClassDeletor deletor = createCCD(outputDirectory, packagePath, classPrefix, null,
                    notDeleteClassNameSet);
            deletor.addPackagePath(oldStylePackagePath); // for Old Style Package
            final List<String> deletedList = deletor.deleteOldTableClass();
            _deletedOldCustomizeBaseParameterBeanListMap.put(outputDirectory, deletedList);
        }
        _deletedOldCustomizeBaseParameterBeanListMap.entrySet();
        showDeleteOldTableFile(_deletedOldCustomizeBaseParameterBeanListMap);
    }

    // -----------------------------------------------------
    //                                        Extended Class
    //                                        --------------
    public void deleteOldCustomizeClass_for_ExtendedCustomizeEntity() {
        if (!existsDeletedBaseClass(_deletedOldCustomizeBaseEntityListMap)) {
            return;
        }
        final String customizePackageName = _generatedClassPkg.getCustomizeEntitySimplePackage();
        final String packagePath = getExtendedEntityPackage() + "." + customizePackageName;
        deleteCustomizeExtendedClass(_deletedOldCustomizeBaseEntityListMap, packagePath);
    }

    public void deleteOldCustomizeClass_for_ImmutableExtendedCustomizeEntity() {
        if (!existsDeletedBaseClass(_deletedOldImmutableCustomizeBaseEntityListMap)) {
            return;
        }
        final String customizePackageName = _generatedClassPkg.getCustomizeEntitySimplePackage();
        final String packagePath = getExtendedEntityPackage() + "." + customizePackageName;
        deleteCustomizeExtendedClass(_deletedOldImmutableCustomizeBaseEntityListMap, packagePath);
    }

    public void deleteOldCustomizeClass_for_ExtendedCursor() {
        if (!existsDeletedBaseClass(_deletedOldCustomizeBaseCursorListMap)) {
            return;
        }
        final String cursorPackageName = _generatedClassPkg.getCursorSimplePackage();
        final String packagePath = getExtendedBehaviorPackage() + "." + cursorPackageName;
        final String oldStylePackagePath = getExtendedDaoPackage() + "." + cursorPackageName;
        deleteCustomizeExtendedClass(_deletedOldCustomizeBaseCursorListMap, packagePath, oldStylePackagePath);
    }

    public void deleteOldCustomizeClass_for_ExtendedCursorHandler() {
        if (!existsDeletedBaseClass(_deletedOldCustomizeBaseCursorHandlerListMap)) {
            return;
        }
        final String cursorPackageName = _generatedClassPkg.getCursorSimplePackage();
        final String packagePath = getExtendedBehaviorPackage() + "." + cursorPackageName;
        final String oldStylePackagePath = getExtendedDaoPackage() + "." + cursorPackageName;
        deleteCustomizeExtendedClass(_deletedOldCustomizeBaseCursorHandlerListMap, packagePath, oldStylePackagePath);
    }

    public void deleteOldCustomizeClass_for_ExtendedParameterBean() {
        if (!existsDeletedBaseClass(_deletedOldCustomizeBaseParameterBeanListMap)) {
            return;
        }
        final String parameterBeanPackageName = _generatedClassPkg.getParameterBeanSimplePackage();
        final String packagePath = getExtendedBehaviorPackage() + "." + parameterBeanPackageName;
        final String oldStylePackagePath = getExtendedDaoPackage() + "." + parameterBeanPackageName;
        deleteCustomizeExtendedClass(_deletedOldCustomizeBaseParameterBeanListMap, packagePath, oldStylePackagePath);
    }

    protected boolean existsDeletedBaseClass(Map<String, List<String>> deletedBaseListMap) {
        return deletedBaseListMap != null && !deletedBaseListMap.isEmpty();
    }

    // -----------------------------------------------------
    //                                         Common Helper
    //                                         -------------
    protected DfOldTableClassDeletor createCCD(String outputDirectory, String packagePath, String classPrefix,
            String classSuffix, NotDeleteTCNSetupper... setuppers) { // createOldCustomizeClassDeletor()
        return createCCD(outputDirectory, packagePath, classPrefix, classSuffix, createNotDeleteTCNSet(setuppers));
    }

    protected DfOldTableClassDeletor createCCD(String outputDirectory, String packagePath, String classPrefix,
            String classSuffix, Set<String> notDeleteClassNameSet) { // createOldCustomizeClassDeletor()
        final DfPackagePathHandler packagePathHandler = new DfPackagePathHandler(getBasicProperties());
        final DfOldTableClassDeletor deletor = new DfOldTableClassDeletor(outputDirectory, packagePathHandler);
        deletor.addPackagePath(packagePath);
        deletor.setClassPrefix(classPrefix);
        deletor.setClassSuffix(classSuffix);
        deletor.setClassExtension(getClassFileExtension());
        deletor.setNotDeleteClassNameSet(notDeleteClassNameSet);
        return deletor;
    }

    protected void deleteCustomizeExtendedClass(Map<String, List<String>> deletedBaseListMap, String... packagePathList) {
        final Set<Entry<String, List<String>>> entrySet = deletedBaseListMap.entrySet();
        for (Entry<String, List<String>> entry : entrySet) {
            final String outputDirectory = entry.getKey();
            final List<String> deletedList = entry.getValue();
            deleteCustomizeExtendedClass(outputDirectory, deletedList, packagePathList);
        }
    }

    protected void deleteCustomizeExtendedClass(String outputDirectory, List<String> deletedBaseList,
            String... packagePathList) {
        final DfPackagePathHandler packagePathHandler = createPackagePathHandler();
        for (String packagePath : packagePathList) {
            final String dirPath = outputDirectory + "/" + packagePathHandler.getPackageAsPath(packagePath);
            for (String baseClassName : deletedBaseList) {
                final int prefixLength = getProjectPrefix().length() + getBasePrefix().length();
                final String extendedClassName = getProjectPrefix() + baseClassName.substring(prefixLength);
                final File file = new File(dirPath + "/" + extendedClassName + "." + getClassFileExtension());
                if (file.exists()) {
                    file.delete();
                    info("    delete('" + extendedClassName + "');");
                }
            }
        }
    }

    // ===================================================================================
    //                                                                 Old SimpleDTO Class
    //                                                                 ===================
    public void deleteOldSimpleDtoTableClass() {
        if (getSimpleDtoProperties().hasSimpleDtoDefinition()) {
            info("public void deleteOldSimpleDtoTableClass() {");
            deleteOldTableClass_for_SimpleDtoBaseDto();
            deleteOldTableClass_for_SimpleDtoExtendedDto();
            info("}");
        }
    }

    public void deleteOldSimpleDtoMapperTableClass() {
        if (getSimpleDtoProperties().hasSimpleDtoDefinition()) {
            if (getSimpleDtoProperties().isUseDtoMapper()) {
                info("public void deleteOldSimpleDtoMapperTableClass() {");
                deleteOldTableClass_for_SimpleDtoBaseMapper();
                deleteOldTableClass_for_SimpleDtoExtendedMapper();
                info("}");
            }
        }
    }

    public void deleteOldSimpleDtoCustomizeClass() {
        if (getSimpleDtoProperties().hasSimpleDtoDefinition()) {
            info("public void deleteOldSimpleDtoCustomizeClass() {");
            deleteOldCustomizeClass_for_SimpleDtoBaseEntity();
            deleteOldCustomizeClass_for_SimpleDtoExtendedDto();
            info("}");
        }
    }

    public void deleteOldSimpleDtoMapperCustomizeClass() {
        if (getSimpleDtoProperties().hasSimpleDtoDefinition()) {
            if (getSimpleDtoProperties().isUseDtoMapper()) {
                info("public void deleteOldSimpleDtoMapperTableClass() {");
                deleteOldCustomizeClass_for_SimpleDtoBaseMapper();
                deleteOldCustomizeClass_for_SimpleDtoExtendedMapper();
                info("}");
            }
        }
    }

    // -----------------------------------------------------
    //                                           Table Class
    //                                           -----------
    protected List<String> _deletedOldTableSimpleDtoBaseDtoList;

    public void deleteOldTableClass_for_SimpleDtoBaseDto() {
        final String packagePath = getSimpleDtoBaseDtoPackage();
        _deletedOldTableSimpleDtoBaseDtoList = doDeleteOldTableClass_for_SimpleDtoBaseDto(packagePath);
    }

    public List<String> doDeleteOldTableClass_for_SimpleDtoBaseDto(String packagePath) {
        final NotDeleteTCNSetupper setupper = new NotDeleteTCNSetupper() {
            public String setup(Table table) {
                return table.getSimpleDtoBaseDtoClassName();
            }
        };
        final String classPrefix = getSimpleDtoBaseDtoPrefix();
        final String classSuffix = getSimpleDtoBaseDtoSuffix();
        final DfOldTableClassDeletor deletor = createTCD(packagePath, classPrefix, classSuffix, setupper);
        final List<String> deletedOldTableSimpleDtoBaseDtoList = deletor.deleteOldTableClass();
        showDeleteOldTableFile(deletedOldTableSimpleDtoBaseDtoList);
        return deletedOldTableSimpleDtoBaseDtoList;
    }

    protected String getSimpleDtoBaseDtoPackage() {
        return getSimpleDtoProperties().getBaseDtoPackage();
    }

    protected String getSimpleDtoBaseDtoPrefix() {
        return getSimpleDtoProperties().getBaseDtoPrefix();
    }

    protected String getSimpleDtoBaseDtoSuffix() {
        return getSimpleDtoProperties().getBaseDtoSuffix();
    }

    public void deleteOldTableClass_for_SimpleDtoExtendedDto() {
        if (_deletedOldTableSimpleDtoBaseDtoList == null || _deletedOldTableSimpleDtoBaseDtoList.isEmpty()) {
            return;
        }
        final String packagePath = getSimpleDtoExtendedDtoPackage();
        doDeleteOldTableClass_for_SimpleDtoExtendedDto(packagePath, _deletedOldTableSimpleDtoBaseDtoList);
    }

    public void doDeleteOldTableClass_for_SimpleDtoExtendedDto(String packagePath, List<String> deletedList) {
        final String outputPath = _generator.getOutputPath();
        final DfPackagePathHandler packagePathHandler = createPackagePathHandler();
        final String dirPath = outputPath + "/" + packagePathHandler.getPackageAsPath(packagePath);
        for (String baseDtoClassName : deletedList) {
            final String extendedDtoClassName = deriveSimpleDtoExtendedDtoClassName(baseDtoClassName);
            final File file = new File(dirPath + "/" + extendedDtoClassName + "." + getClassFileExtension());
            if (file.exists()) {
                file.delete();
                info("    delete('" + extendedDtoClassName + "');");
            }
        }
    }

    protected String deriveSimpleDtoExtendedDtoClassName(String baseDtoClassName) {
        return getSimpleDtoProperties().deriveExtendedDtoClassName(baseDtoClassName);
    }

    protected String getSimpleDtoExtendedDtoPackage() {
        return getSimpleDtoProperties().getExtendedDtoPackage();
    }

    protected List<String> _deletedOldTableSimpleDtoBaseMapperList;

    public void deleteOldTableClass_for_SimpleDtoBaseMapper() {
        final String packagePath = getSimpleDtoBaseMapperPackage();
        _deletedOldTableSimpleDtoBaseMapperList = doDeleteOldTableClass_for_SimpleDtoBaseMapper(packagePath);
    }

    public List<String> doDeleteOldTableClass_for_SimpleDtoBaseMapper(String packagePath) {
        final NotDeleteTCNSetupper setupper = new NotDeleteTCNSetupper() {
            public String setup(Table table) {
                return table.getSimpleDtoBaseMapperClassName();
            }
        };
        final String classPrefix = getSimpleDtoBaseDtoPrefix();
        final String classSuffix = getSimpleDtoMapperSuffix();
        final DfOldTableClassDeletor deletor = createTCD(packagePath, classPrefix, classSuffix, setupper);
        final List<String> deletedOldTableSimpleDtoBaseMapperList = deletor.deleteOldTableClass();
        showDeleteOldTableFile(deletedOldTableSimpleDtoBaseMapperList);
        return deletedOldTableSimpleDtoBaseMapperList;
    }

    public void deleteOldTableClass_for_SimpleDtoExtendedMapper() {
        if (_deletedOldTableSimpleDtoBaseMapperList == null || _deletedOldTableSimpleDtoBaseMapperList.isEmpty()) {
            return;
        }
        final String packagePath = getSimpleDtoExtendedMapperPackage();
        doDeleteOldTableClass_for_SimpleDtoExtendedMapper(packagePath, _deletedOldTableSimpleDtoBaseMapperList);
    }

    public void doDeleteOldTableClass_for_SimpleDtoExtendedMapper(String packagePath, List<String> deletedList) {
        final String outputPath = _generator.getOutputPath();
        final DfPackagePathHandler packagePathHandler = createPackagePathHandler();
        final String dirPath = outputPath + "/" + packagePathHandler.getPackageAsPath(packagePath);
        for (String baseMapperClassName : deletedList) {
            final String extendedMapperClassName = deriveSimpleDtoExtendedMapperClassName(baseMapperClassName);
            final File file = new File(dirPath + "/" + extendedMapperClassName + "." + getClassFileExtension());
            if (file.exists()) {
                file.delete();
                info("    delete('" + extendedMapperClassName + "');");
            }
        }
    }

    protected String deriveSimpleDtoExtendedMapperClassName(String baseMapperClassName) {
        return getSimpleDtoProperties().deriveExtendedMapperClassName(baseMapperClassName);
    }

    protected String getSimpleDtoBaseMapperPackage() {
        return getSimpleDtoProperties().getBaseMapperPackage();
    }

    protected String getSimpleDtoExtendedMapperPackage() {
        return getSimpleDtoProperties().getExtendedMapperPackage();
    }

    protected String getSimpleDtoMapperSuffix() {
        return getSimpleDtoProperties().getMapperSuffix();
    }

    // -----------------------------------------------------
    //                                             Customize
    //                                             ---------
    protected List<String> _deletedOldCustomizeSimpleDtoBaseDtoList;

    public void deleteOldCustomizeClass_for_SimpleDtoBaseEntity() {
        if (!getSimpleDtoProperties().hasSimpleDtoDefinition()) {
            return;
        }
        final String customizePackageName = _generatedClassPkg.getCustomizeEntitySimplePackage();
        final String packagePath = getSimpleDtoBaseDtoPackage() + "." + customizePackageName;
        _deletedOldCustomizeSimpleDtoBaseDtoList = doDeleteOldTableClass_for_SimpleDtoBaseDto(packagePath);
    }

    public void deleteOldCustomizeClass_for_SimpleDtoExtendedDto() {
        if (_deletedOldCustomizeSimpleDtoBaseDtoList == null || _deletedOldCustomizeSimpleDtoBaseDtoList.isEmpty()) {
            return;
        }
        final String customizePackageName = _generatedClassPkg.getCustomizeEntitySimplePackage();
        final String packagePath = getSimpleDtoExtendedDtoPackage() + "." + customizePackageName;
        doDeleteOldTableClass_for_SimpleDtoExtendedDto(packagePath, _deletedOldCustomizeSimpleDtoBaseDtoList);
    }

    protected List<String> _deletedOldCustomizeSimpleDtoBaseMapperList;

    public void deleteOldCustomizeClass_for_SimpleDtoBaseMapper() {
        if (!getSimpleDtoProperties().hasSimpleDtoDefinition()) {
            return;
        }
        final String customizePackageName = _generatedClassPkg.getCustomizeEntitySimplePackage();
        final String packagePath = getSimpleDtoBaseMapperPackage() + "." + customizePackageName;
        _deletedOldCustomizeSimpleDtoBaseMapperList = doDeleteOldTableClass_for_SimpleDtoBaseMapper(packagePath);
    }

    public void deleteOldCustomizeClass_for_SimpleDtoExtendedMapper() {
        if (_deletedOldCustomizeSimpleDtoBaseMapperList == null
                || _deletedOldCustomizeSimpleDtoBaseMapperList.isEmpty()) {
            return;
        }
        final String customizePackageName = _generatedClassPkg.getCustomizeEntitySimplePackage();
        final String packagePath = getSimpleDtoExtendedMapperPackage() + "." + customizePackageName;
        doDeleteOldTableClass_for_SimpleDtoExtendedMapper(packagePath, _deletedOldCustomizeSimpleDtoBaseMapperList);
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    protected DfPackagePathHandler createPackagePathHandler() {
        return new DfPackagePathHandler(getBasicProperties());
    }

    protected String getProjectPrefix() {
        return getBasicProperties().getProjectPrefix();
    }

    protected String getBasePrefix() {
        return getBasicProperties().getBasePrefix();
    }

    protected String getEntityDBablePrefix() {
        return getLittleAdjustmentProperties().getEntityDBablePrefix();
    }

    protected boolean isMakeImmutableEntity() {
        return getLittleAdjustmentProperties().isMakeImmutableEntity();
    }

    protected String getClassFileExtension() {
        return getBasicProperties().getClassFileExtension();
    }

    protected List<Table> getTableList() {
        return _tableList;
    }

    // -----------------------------------------------------
    //                                               Logging
    //                                               -------
    public void info(String msg) {
        _log.info(msg);
    }

    public void debug(String msg) {
        _log.debug(msg);
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfBuildProperties getProperties() {
        return DfBuildProperties.getInstance();
    }

    protected DfBasicProperties getBasicProperties() {
        return DfBuildProperties.getInstance().getBasicProperties();
    }

    protected DfLittleAdjustmentProperties getLittleAdjustmentProperties() {
        return DfBuildProperties.getInstance().getLittleAdjustmentProperties();
    }

    protected DfOutsideSqlProperties getOutsideSqlProperties() {
        return DfBuildProperties.getInstance().getOutsideSqlProperties();
    }

    protected DfSimpleDtoProperties getSimpleDtoProperties() {
        return DfBuildProperties.getInstance().getSimpleDtoProperties();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void setCustomizeTableList(List<Table> customizeTableList) {
        final Map<String, Map<String, Table>> cmentityLocationMap = new LinkedHashMap<String, Map<String, Table>>();
        for (Table table : customizeTableList) {
            final String outputDirectory = table.getSql2EntityOutputDirectory();
            Map<String, Table> elementMap = cmentityLocationMap.get(outputDirectory);
            if (elementMap == null) {
                elementMap = new LinkedHashMap<String, Table>();
                cmentityLocationMap.put(outputDirectory, elementMap);
            }
            elementMap.put(table.getTableDbName(), table);
        }
        if (cmentityLocationMap.isEmpty()) { // no outsideSql (all classes for outsideSql are old)
            final String mainDir = getOutsideSqlProperties().getSql2EntityOutputDirectory(); // main directory only
            cmentityLocationMap.put(mainDir, new HashMap<String, Table>());
        }
        _cmentityLocationMap = cmentityLocationMap;
    }

    public void setPmbMetaDataMap(Map<String, DfPmbMetaData> pmbMetaDataMap) {
        final Map<String, Map<String, DfPmbMetaData>> pmbLocationMap = new LinkedHashMap<String, Map<String, DfPmbMetaData>>();
        final Set<Entry<String, DfPmbMetaData>> entrySet = pmbMetaDataMap.entrySet();
        for (Entry<String, DfPmbMetaData> entry : entrySet) {
            final String pmbName = entry.getKey();
            final DfPmbMetaData pmbMetaData = entry.getValue();
            final String outputDirectory = pmbMetaData.getSql2EntityOutputDirectory();
            Map<String, DfPmbMetaData> elementMap = pmbLocationMap.get(outputDirectory);
            if (elementMap == null) {
                elementMap = new LinkedHashMap<String, DfPmbMetaData>();
                pmbLocationMap.put(outputDirectory, elementMap);
            }
            elementMap.put(pmbName, pmbMetaData);
        }
        if (pmbLocationMap.isEmpty()) { // no outsideSql (all classes for outsideSql are old)
            final String mainDir = getOutsideSqlProperties().getSql2EntityOutputDirectory(); // main directory only
            pmbLocationMap.put(mainDir, new HashMap<String, DfPmbMetaData>());
        }
        _pmbLocationMap = pmbLocationMap;
    }
}
