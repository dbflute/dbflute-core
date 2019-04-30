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
package org.dbflute.logic.manage.freegen.table.solr;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.logic.manage.freegen.DfFreeGenMapProp;
import org.dbflute.logic.manage.freegen.DfFreeGenMetaData;
import org.dbflute.logic.manage.freegen.DfFreeGenResource;
import org.dbflute.logic.manage.freegen.DfFreeGenTableLoader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author jflute
 */
public class DfSolrXmlTableLoader implements DfFreeGenTableLoader {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static SAXParserFactory _saxFactory;
    static {
        _saxFactory = SAXParserFactory.newInstance();
        _saxFactory.setValidating(true);
    }

    // ===================================================================================
    //                                                                          Load Table
    //                                                                          ==========
    // ; resourceMap = map:{
    //     ; resourceType = SOLR
    //     ; resourceFile = ../../.../schema.xml
    // }
    // ; outputMap = map:{
    //     ; templateFile = unused
    //     ; outputDirectory = ../src/main/java
    //     ; package = org.dbflute...
    //     ; className = unused
    // }
    // ; optionMap = map:{
    //     ; isContainsDynamicField = false
    // }
    // ; mappingMap = map:{
    //     ; type = map:{
    //         ; INTEGER = Integer
    //         ; VARCHAR = String
    //     }
    // }
    @Override
    public DfFreeGenMetaData loadTable(String requestName, DfFreeGenResource resource, DfFreeGenMapProp mapProp) {
        final Map<String, Object> tableMap = mapProp.getOptionMap();
        final Map<String, Map<String, String>> mappingMap = mapProp.getMappingMap();
        final String resourceFile = resource.getResourceFile();
        final String encoding = resource.hasEncoding() ? resource.getEncoding() : "UTF-8";
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(resourceFile), encoding));
            final SAXParser saxParser = _saxFactory.newSAXParser();
            final InputSource is = new InputSource(br);
            final DfSolrXmlParserHandler parserHandler = new DfSolrXmlParserHandler(tableMap, mappingMap);
            saxParser.parse(is, parserHandler);
            final String tableName = requestName; // basically unused, also for compatible
            return DfFreeGenMetaData.asOnlyOne(tableMap, tableName, parserHandler.getColumnList());
        } catch (IOException e) {
            String msg = "Failed to read the properties:";
            msg = msg + " requestName=" + requestName + " resourceFile=" + resourceFile;
            throw new IllegalStateException(msg, e);
        } catch (ParserConfigurationException e) {
            handleException(resourceFile, encoding, e);
            return null; // unreachable
        } catch (SAXException e) {
            handleException(resourceFile, encoding, e);
            return null; // unreachable
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ignored) {}
            }
        }
    }

    protected void handleException(String xmlFile, String encoding, Exception e) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to parse the XML file.");
        br.addItem("XML File");
        br.addElement(xmlFile);
        br.addItem("Encoding");
        br.addElement(encoding);
        br.addItem("Exception");
        br.addElement(e.getClass().getName());
        br.addElement(e.getMessage());
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg, e);
    }
}
