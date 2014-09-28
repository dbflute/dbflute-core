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
 *
 * And the following license definition is for Apache Torque.
 * DBFlute modified this source code and redistribute as same license 'Apache'.
 * /- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 *
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Turbine" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Turbine", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 * 
 * - - - - - - - - - -/
 */
package org.apache.torque.engine.database.transform;

/* ====================================================================
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Turbine" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Turbine", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Stack;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.torque.engine.database.model.AppData;
import org.apache.torque.engine.database.model.Column;
import org.apache.torque.engine.database.model.Database;
import org.apache.torque.engine.database.model.ForeignKey;
import org.apache.torque.engine.database.model.Index;
import org.apache.torque.engine.database.model.Table;
import org.apache.torque.engine.database.model.UnifiedSchema;
import org.apache.torque.engine.database.model.Unique;
import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A Class that is used to parse an input XML schema file and creates an AppData java structure.
 * @author modified by jflute (originated in Apache Torque)
 */
public class XmlToAppData extends DefaultHandler {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static SAXParserFactory _saxFactory;
    static {
        _saxFactory = SAXParserFactory.newInstance();
        _saxFactory.setValidating(true);
    }

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final AppData _appData;
    protected final XmlReadingFilter _readingFilter;
    protected Database _currentDB;
    protected Table _currentTable;
    protected Column _currentColumn;
    protected ForeignKey _currentFK;
    protected Index _currentIndex;
    protected Unique _currentUnique;
    protected String _currentPackage;
    protected String _currentXmlFile;
    protected boolean _firstPass;

    /** this is the stack to store parsing data */
    protected final Stack<ParseStackElement> _parsingStack = new Stack<ParseStackElement>();

    /** remember all files we have already parsed to detect looping. */
    protected Vector<String> _alreadyReadFiles;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * Creates a new instance for the specified database type.
     * @param databaseType The type of database for the application.
     * @param readingFilter The filter of object by name when reading XML. (NullAllowed)
     */
    public XmlToAppData(String databaseType, XmlReadingFilter readingFilter) {
        _appData = new AppData(databaseType);
        _readingFilter = readingFilter;
        _firstPass = true;
    }

    public static interface XmlReadingFilter {
        boolean isTableExcept(UnifiedSchema unifiedSchema, String tableName);

        boolean isColumnExcept(UnifiedSchema unifiedSchema, String tableName, String columnName);

        boolean isSequenceExcept(UnifiedSchema unifiedSchema, String sequenceName);

        boolean isProcedureExcept(UnifiedSchema unifiedSchema, String procedureName);
    }

    // ===================================================================================
    //                                                                               Parse
    //                                                                               =====
    /**
     * Parses a XML input file and returns a newly created and
     * populated AppData structure.
     * @param xmlFile The input file to parse.
     * @return AppData populated by <code>xmlFile</code>.
     * @throws IOException
     */
    public AppData parseFile(String xmlFile) throws IOException {
        // in case I am missing something, make it obvious
        if (!_firstPass) {
            throw new Error("No more double pass");
        }
        // check to see if we already have parsed the file
        if ((_alreadyReadFiles != null) && _alreadyReadFiles.contains(xmlFile)) {
            return _appData;
        } else if (_alreadyReadFiles == null) {
            _alreadyReadFiles = new Vector<String>(3, 1);
        }

        // remember the file to avoid looping
        _alreadyReadFiles.add(xmlFile);
        _currentXmlFile = xmlFile;

        final String encoding = getProejctSchemaXMLEncoding();
        BufferedReader br = null;
        try {
            // use InputStreamReader for setting an encoding for project schema XML
            // for example, Japanese table names and comments need this
            br = new BufferedReader(new InputStreamReader(new FileInputStream(xmlFile), encoding));
            final InputSource is = new InputSource(br);
            final SAXParser parser = _saxFactory.newSAXParser();
            parser.parse(is, this);
        } catch (ParserConfigurationException e) {
            handleException(xmlFile, encoding, e);
        } catch (SAXException e) {
            handleException(xmlFile, encoding, e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ignored) {
                }
            }
        }
        _firstPass = false;
        return _appData;
    }

    protected void handleException(String xmlFile, String encoding, Exception e) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to parse SchemaXML.");
        br.addItem("SchemaXML");
        br.addElement(xmlFile);
        br.addItem("Encoding");
        br.addElement(encoding);
        br.addItem("Exception");
        br.addElement(e.getClass().getName());
        br.addElement(e.getMessage());
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg, e);
    }

    // ===================================================================================
    //                                                                    Handler Override
    //                                                                    ================
    /**
     * EntityResolver implementation. Called by the XML parser
     * @param publicId The public identifier of the external entity
     * @param systemId The system identifier of the external entity
     * @return an InputSource for the database.dtd file
     * @see org.apache.torque.engine.database.transform.DTDResolver#resolveEntity(String, String)
     */
    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException {
        try {
            return new DTDResolver().resolveEntity(publicId, systemId);
        } catch (Exception e) {
            throw new SAXException(e);
        }
    }

    /**
     * Handles opening elements of the XML file.
     * @param uri URI.
     * @param localName The local name (without prefix), or the empty string if namespace processing is not being performed.
     * @param rawName The qualified name (with prefix), or the empty string if qualified names are not available.
     * @param attributes The specified or defaulted attributes
     */
    @Override
    public void startElement(String uri, String localName, String rawName, Attributes attributes) {
        try {
            if (rawName.equals("database")) { // basically only one on DBFlute
                _currentDB = _appData.addDatabase(attributes); // two or more calls throws Exception
            } else if (rawName.equals("table")) { // contains additional schema's tables
                clearCurrentElements();
                _currentTable = _currentDB.addTable(attributes, _readingFilter); // null allowed
            } else if (rawName.equals("sequenceGroup")) {
                clearCurrentElements();
                _currentDB.markSequenceGroup();
            } else if (rawName.equals("sequence")) { // may contain additional schema's sequences
                clearCurrentElements();
                _currentDB.addSequence(attributes, _readingFilter);
            } else if (rawName.equals("procedureGroup")) {
                clearCurrentElements();
                _currentDB.markProcedureGroup();
            } else if (rawName.equals("procedure")) { // may contain additional schema's procedures
                clearCurrentElements();
                _currentDB.addProcedure(attributes, _readingFilter);
            }
            if (_currentTable != null) { // check because the table may be filtered
                // handle table elements
                if (rawName.equals("column")) {
                    _currentColumn = _currentTable.addColumn(attributes, _readingFilter);
                } else if (rawName.equals("foreign-key")) {
                    // except foreign tables are adjusted later (at final initialization)
                    _currentFK = _currentTable.addForeignKey(attributes);
                } else if (rawName.equals("reference")) {
                    _currentFK.addReference(attributes);
                } else if (rawName.equals("unique")) {
                    _currentUnique = _currentTable.addUnique(attributes);
                } else if (rawName.equals("unique-column")) {
                    _currentUnique.addColumn(attributes);
                } else if (rawName.equals("index")) {
                    _currentIndex = _currentTable.addIndex(attributes);
                } else if (rawName.equals("index-column")) {
                    _currentIndex.addColumn(attributes);
                }
            }
        } catch (Exception e) {
            String msg = "Failed to analyze schema data of the XML:";
            msg = msg + " uri=" + uri + " localName=" + localName + " rawName=" + rawName;
            throw new IllegalStateException(msg, e);
        }
    }

    protected void clearCurrentElements() {
        _currentTable = null;
        _currentColumn = null;
        _currentFK = null;
        _currentIndex = null;
        _currentUnique = null;
    }

    /**
     * Handles closing elements of the xml file.
     * @param uri
     * @param localName The local name (without prefix), or the empty string if
     *         Namespace processing is not being performed.
     * @param rawName The qualified name (with prefix), or the empty string if
     *         qualified names are not available.
     */
    @Override
    public void endElement(String uri, String localName, String rawName) {
        // *commented out because of too many logging
        //if (log.isDebugEnabled()) {
        //    log.debug("endElement(" + uri + ", " + localName + ", " + rawName + ") called");
        //}
    }

    /**
     * When parsing multiple files that use nested <external-schema> tags we
     * need to use a stack to remember some values.
     */
    protected static class ParseStackElement {
        private String currentPackage;
        private String currentXmlFile;
        private boolean firstPass;

        /**
         * @param parser
         */
        public ParseStackElement(XmlToAppData parser) {
            // remember current state of parent object
            currentPackage = parser._currentPackage;
            currentXmlFile = parser._currentXmlFile;
            firstPass = parser._firstPass;

            // push the state onto the stack
            parser._parsingStack.push(this);
        }

        /**
         * Removes the top element from the stack and activates the stored state
         * @param parser
         */
        public static void popState(XmlToAppData parser) {
            if (!parser._parsingStack.isEmpty()) {
                ParseStackElement elem = (ParseStackElement) parser._parsingStack.pop();

                // activate stored state
                parser._currentPackage = elem.currentPackage;
                parser._currentXmlFile = elem.currentXmlFile;
                parser._firstPass = elem.firstPass;
            }
        }

        /**
         * Stores the current state on the top of the stack.
         * @param parser
         */
        public static void pushState(XmlToAppData parser) {
            new ParseStackElement(parser);
        }
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected String getProejctSchemaXMLEncoding() {
        return getProperties().getBasicProperties().getProejctSchemaXMLEncoding();
    }

    protected DfBuildProperties getProperties() {
        return DfBuildProperties.getInstance();
    }
}
