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
package org.seasar.dbflute.logic.sql2entity.bqp;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.logic.generate.language.DfLanguageDependency;
import org.seasar.dbflute.logic.generate.language.pkgstyle.DfLanguageClassPackage;
import org.seasar.dbflute.logic.sql2entity.analyzer.DfOutsideSqlFile;
import org.seasar.dbflute.properties.DfBasicProperties;
import org.seasar.dbflute.properties.DfOutsideSqlProperties;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 */
public class DfBqpOutsideSqlFile {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfOutsideSqlFile _outsideSqlFile;
    protected boolean _analyzed;
    protected boolean _bqp;
    protected String _filePath;
    protected String _subDirectoryPath;
    protected String _entityName;
    protected String _behaviorName;
    protected String _behaviorQueryPath;
    protected boolean _sqlAp;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfBqpOutsideSqlFile(DfOutsideSqlFile outsideSqlFile) {
        _outsideSqlFile = outsideSqlFile;
    }

    protected void analyze() {
        if (!_analyzed) {
            _analyzed = true;
            doAnalyze();
        }
    }

    protected void doAnalyze() {
        final String exbhvMark;
        {
            final String exbhvPackage;
            if (isApplicationBehaviorProject()) {
                exbhvPackage = getLibraryBehaviorPackage();
            } else {
                exbhvPackage = getBasicProperties().getExtendedBehaviorPackage();
            }
            final String exbhvName = Srl.substringLastRear(exbhvPackage, ".");
            final DfLanguageDependency lang = getBasicProperties().getLanguageDependency();
            final DfLanguageClassPackage pkg = lang.getLanguageClassPackage();
            final String sqlPackage;
            if (getOutsideSqlProperties().isSqlPackageValid()) {
                final String pureSqlPackage = getOutsideSqlProperties().getSqlPackage();
                if (pureSqlPackage.endsWith(exbhvName)) { // contains 'exbhv'
                    sqlPackage = Srl.substringLastFront(pureSqlPackage, ".");
                } else {
                    sqlPackage = pureSqlPackage;
                }
            } else {
                sqlPackage = Srl.substringLastFront(exbhvPackage, ".");
            }
            exbhvMark = pkg.buildExtendedBehaviorPackageMark(sqlPackage, exbhvName);
        }

        // both types are target
        final String bhvSuffix = "Bhv";
        final String bhvApSuffix = getApplicationBehaviorAdditionalSuffix();
        final Pattern bqpPattern = Pattern.compile(".+" + exbhvMark + ".+" + bhvSuffix + "_.+.sql$");
        final Pattern bqpApPattern = Pattern.compile(".+" + exbhvMark + ".+" + bhvApSuffix + "_.+.sql$");

        final String path = getSlashPath(_outsideSqlFile.getPhysicalFile());
        final Matcher matcher = bqpPattern.matcher(path);
        final Matcher matcherAp = bqpApPattern.matcher(path);
        final String foundSuffix;
        if (matcher.matches()) {
            foundSuffix = bhvSuffix;
        } else if (matcherAp.matches()) {
            foundSuffix = bhvApSuffix;
        } else {
            _bqp = false;
            return;
        }
        _bqp = true;
        String simpleFileName = path.substring(path.lastIndexOf(exbhvMark) + exbhvMark.length());
        if (simpleFileName.contains("/")) {
            _subDirectoryPath = simpleFileName.substring(0, simpleFileName.lastIndexOf("/"));
            simpleFileName = simpleFileName.substring(simpleFileName.lastIndexOf("/") + "/".length());
        }
        final int behaviorNameMarkIndex = simpleFileName.indexOf(foundSuffix + "_");
        final int behaviorNameEndIndex = behaviorNameMarkIndex + foundSuffix.length();
        final int behaviorQueryPathStartIndex = behaviorNameMarkIndex + (foundSuffix + "_").length();
        final int behaviorQueryPathEndIndex = simpleFileName.lastIndexOf(".sql");
        _entityName = simpleFileName.substring(0, behaviorNameMarkIndex);
        _behaviorName = simpleFileName.substring(0, behaviorNameEndIndex);
        _behaviorQueryPath = simpleFileName.substring(behaviorQueryPathStartIndex, behaviorQueryPathEndIndex);
        _filePath = path;
        if (_outsideSqlFile.isSqlAp()) {
            _sqlAp = true;
        }
    }

    // ===================================================================================
    //                                                                            Analyzed
    //                                                                            ========
    public boolean isBqp() {
        analyze();
        return _bqp;
    }

    public String getFilePath() {
        analyze();
        return _filePath;
    }

    public String getSubDirectoryPath() {
        analyze();
        return _subDirectoryPath;
    }

    public String getEntityName() {
        analyze();
        return _entityName;
    }

    public String getBehaviorName() {
        analyze();
        return _behaviorName;
    }

    public String getBehaviorQueryPath() {
        analyze();
        return _behaviorQueryPath;
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    public String replaceString(String text, String fromText, String toText) {
        return Srl.replace(text, fromText, toText);
    }

    public String getSlashPath(File file) {
        return replaceString(file.getPath(), getFileSeparator(), "/");
    }

    public String getFileSeparator() {
        return File.separator;
    }

    public String ln() {
        return "\n";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    protected DfBuildProperties getProperties() {
        return DfBuildProperties.getInstance();
    }

    protected DfBasicProperties getBasicProperties() {
        return getProperties().getBasicProperties();
    }

    protected DfOutsideSqlProperties getOutsideSqlProperties() {
        return getProperties().getOutsideSqlProperties();
    }

    protected boolean isApplicationBehaviorProject() {
        return getBasicProperties().isApplicationBehaviorProject();
    }

    protected String getLibraryBehaviorPackage() {
        return getBasicProperties().getLibraryBehaviorPackage();
    }

    protected String getApplicationBehaviorAdditionalSuffix() {
        return getBasicProperties().getApplicationBehaviorAdditionalSuffix();
    }

    protected String getBhvApResolvedProjectPrefix() {
        return getBasicProperties().getBhvApResolvedProjectPrefix();
    }

    protected String getBhvApResolvedBehaviorSuffix() {
        return getBasicProperties().getBhvApResolvedBehaviorSuffix();
    }
}