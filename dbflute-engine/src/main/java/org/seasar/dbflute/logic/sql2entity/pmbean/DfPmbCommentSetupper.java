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
package org.seasar.dbflute.logic.sql2entity.pmbean;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.logic.generate.packagepath.DfPackagePathHandler;
import org.seasar.dbflute.properties.DfBasicProperties;
import org.seasar.dbflute.properties.DfClassificationProperties;
import org.seasar.dbflute.properties.DfDocumentProperties;
import org.seasar.dbflute.properties.DfLittleAdjustmentProperties;
import org.seasar.dbflute.properties.DfOutsideSqlProperties;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 */
public class DfPmbCommentSetupper {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log instance. */
    private static final Log _log = LogFactory.getLog(DfPmbCommentSetupper.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Collection<DfPmbMetaData> _pmbMetaDataList;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfPmbCommentSetupper(Collection<DfPmbMetaData> pmbMetaDataList) {
        _pmbMetaDataList = pmbMetaDataList;
    }

    // ===================================================================================
    //                                                                              Set up
    //                                                                              ======
    public void setupExtendedClassDescription() {
        if (_pmbMetaDataList.isEmpty()) {
            return;
        }
        if (getBasicProperties().isFriendsS2JDBC()) {
            return;
        }
        final String fileExt = getBasicProperties().getLanguageDependency().getLanguageGrammar()
                .getClassFileExtension();
        for (DfPmbMetaData pmbMetaData : _pmbMetaDataList) {
            final String basePath = derivePmbFileBasePath(pmbMetaData);
            final String classFilePath = basePath + "/" + pmbMetaData.getExtendedClassName() + "." + fileExt;
            final File pmbeanFile = new File(classFilePath);
            reflectExtendedClassDescription(pmbMetaData, pmbeanFile);
        }
    }

    protected String derivePmbFileBasePath(DfPmbMetaData pmbMetaData) {
        final String outputDirectory = pmbMetaData.getSql2EntityOutputDirectory();
        final String pmbPackage = getOutsideSqlProperties().getExtendedParameterBeanPackage();
        final DfPackagePathHandler packagePathHandler = new DfPackagePathHandler(getBasicProperties());
        packagePathHandler.setFileSeparatorSlash(true);
        return outputDirectory + "/" + packagePathHandler.getPackageAsPath(pmbPackage);
    }

    protected void reflectExtendedClassDescription(DfPmbMetaData pmbMetaData, File pmbeanFile) {
        final String encoding = getBasicProperties().getSourceFileEncoding();
        final String lineSep = getBasicProperties().getSourceCodeLineSeparator();
        final String beginMark = getBasicProperties().getExtendedClassDescriptionBeginMark();
        final String endMark = getBasicProperties().getExtendedClassDescriptionEndMark();
        String lineString = null;
        final StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(pmbeanFile), encoding));
            boolean targetArea = false;
            boolean done = false;
            while (true) {
                lineString = br.readLine();
                if (lineString == null) {
                    if (targetArea) {
                        String msg = "The end mark of extended class description was not found:";
                        msg = msg + " pmbeanFile=" + pmbeanFile;
                        throw new IllegalStateException(msg);
                    }
                    break;
                }
                if (targetArea) {
                    if (lineString.contains(endMark)) {
                        targetArea = false;
                    } else {
                        continue;
                    }
                }
                sb.append(lineString).append(lineSep);
                if (!done && lineString.contains(beginMark)) {
                    targetArea = true;
                    final String indent = lineString.substring(0, lineString.indexOf(beginMark));
                    sb.append(buildExtendedClassDescription(pmbMetaData, indent, lineSep));
                    done = true;
                }
            }
        } catch (UnsupportedEncodingException e) {
            String msg = "The encoding is unsupported: encoding=" + encoding;
            throw new IllegalStateException(msg, e);
        } catch (FileNotFoundException e) {
            String msg = "The file of extended parameter-bean was not found: pmbeanFile=" + pmbeanFile;
            throw new IllegalStateException(msg, e);
        } catch (IOException e) {
            String msg = "BufferedReader.readLine() threw the exception: current line=" + lineString;
            throw new IllegalStateException(msg, e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ignored) {
                    _log.warn(ignored.getMessage());
                }
            }
        }

        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pmbeanFile), encoding));
            bw.write(sb.toString());
            bw.flush();
        } catch (UnsupportedEncodingException e) {
            String msg = "The encoding is unsupported: encoding=" + encoding;
            throw new IllegalStateException(msg, e);
        } catch (FileNotFoundException e) {
            String msg = "The file of base behavior was not found: pmbeanFile=" + pmbeanFile;
            throw new IllegalStateException(msg, e);
        } catch (IOException e) {
            String msg = "BufferedWriter.write() threw the exception: pmbeanFile=" + pmbeanFile;
            throw new IllegalStateException(msg, e);
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException ignored) {
                    _log.warn(ignored.getMessage());
                }
            }
        }
    }

    protected String buildExtendedClassDescription(DfPmbMetaData pmbMetaData, String indent, String lineSep) {
        final DfDocumentProperties docprop = getDocumentProperties();
        final String pmbBizName = pmbMetaData.getBusinessName();
        final StringBuilder sb = new StringBuilder();
        if (pmbMetaData.isTypedParameterBean()) {
            final String bhvClassName = pmbMetaData.getBehaviorClassName();
            final String bhvQueryPath = pmbMetaData.getBehaviorQueryPath();
            final String sqlTitle = docprop.resolveTextForJavaDoc(pmbMetaData.getSqlTitle(), indent);
            sb.append(indent).append("The typed parameter-bean of ").append(pmbBizName).append(". ");
            final String typedDisp = pmbMetaData.buildTypedDisp();
            if (Srl.is_NotNull_and_NotTrimmedEmpty(typedDisp)) {
                sb.append("<span style=\"color: #AD4747\">").append(typedDisp).append("</span>");
            }
            sb.append("<br />").append(lineSep);
            sb.append(indent).append("This is related to \"<span style=\"color: #AD4747\">");
            sb.append(bhvQueryPath).append("</span>\" on ").append(bhvClassName);
            if (Srl.is_NotNull_and_NotTrimmedEmpty(sqlTitle)) {
                sb.append(", <br />").append(lineSep).append(indent);
                sb.append("described as \"").append(sqlTitle).append("\"");
            }
            sb.append(".");
        } else if (pmbMetaData.isRelatedToProcedure()) {
            final String procedureName = pmbMetaData.getProcedureName();
            sb.append(indent).append("The typed parameter-bean of ").append(pmbBizName);
            sb.append(". <br />").append(lineSep);
            sb.append(indent).append("This is related to \"<span style=\"color: #AD4747\">");
            sb.append(procedureName).append("</span>\".");
        } else {
            if (pmbMetaData.isRelatedToBehaviorQuery()) {
                sb.append(indent).append("The simple parameter-bean of ").append(pmbBizName);
                sb.append(". <br />").append(lineSep);
                final String bhvClassName = pmbMetaData.getBehaviorClassName();
                final String bhvQueryPath = pmbMetaData.getBehaviorQueryPath();
                sb.append(indent).append("This is defined at \"<span style=\"color: #AD4747\">");
                sb.append(bhvQueryPath).append("</span>\" on ").append(bhvClassName).append(".");
            } else {
                sb.append(indent).append("The simple parameter-bean of ").append(pmbBizName).append(".");
            }
        }
        sb.append(" <br />"); // basically unnecessary but against Eclipse default code formatter problem
        sb.append(lineSep);
        return sb.toString();
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

    protected DfOutsideSqlProperties getOutsideSqlProperties() {
        return getProperties().getOutsideSqlProperties();
    }

    protected DfDocumentProperties getDocumentProperties() {
        return getProperties().getDocumentProperties();
    }

    protected DfClassificationProperties getClassificationProperties() {
        return getProperties().getClassificationProperties();
    }

    protected DfLittleAdjustmentProperties getLittleAdjustmentProperties() {
        return getProperties().getLittleAdjustmentProperties();
    }
}
