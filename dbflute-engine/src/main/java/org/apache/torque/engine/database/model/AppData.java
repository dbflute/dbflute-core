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
package org.apache.torque.engine.database.model;

import org.apache.torque.engine.database.transform.DTDResolver;
import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.properties.DfBasicProperties;
import org.seasar.dbflute.properties.facade.DfDatabaseTypeFacadeProp;
import org.seasar.dbflute.util.Srl;
import org.xml.sax.Attributes;

/**
 * The schema data for your application.
 * @author modified by jflute (originated in Apache Torque)
 */
public class AppData {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // DBFlute treats all tables that contains other schema's as one database object.
    // Other schema's tables is handled by DBFlute original function AdditionalSchema.
    /**
     * The database for this application.
     */
    private Database _database;

    /**
     * The type for our databases.
     */
    private String _databaseType;

    /**
     * Name of the database. Only one database definition
     * is allowed in one XML descriptor.
     */
    private String _name;

    // flag to complete initialization only once.
    private boolean _isInitialized;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * Creates a new instance for the specified database type.
     * @param databaseType The default type for any databases added to this application model. file, including trailing slash.
     */
    public AppData(String databaseType) {
        this._databaseType = databaseType;
    }

    public static AppData createAsEmpty() { // e.g. ReplaceSchema's generation
        final DfBasicProperties basicProp = DfBuildProperties.getInstance().getBasicProperties();
        final DfDatabaseTypeFacadeProp facadeProp = basicProp.getDatabaseTypeFacadeProp();
        final String databaseType = facadeProp.getTargetDatabase();
        final AppData appData = new AppData(databaseType);
        final Database database = new Database();
        database.setAppData(appData);
        appData.addDatabase(database);
        return appData;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    /**
     * Set the name of the database.
     * @param name of the database.
     */
    public void setName(String name) {
        this._name = name;
    }

    /**
     * Get the name of the database.
     * @return String name
     */
    public String getName() {
        return _name;
    }

    /**
     * Get the short name of the database (without the '-schema' postfix).
     * @return String name
     */
    public String getShortName() {
        return Srl.replace(_name, "-schema", "");
    }

    /**
     * Get database object for application. <br />
     * @return The instance of the database. (NullAllowed: if null, not loaded yet)
     */
    public Database getDatabase() {
        doFinalInitialization();
        return _database;
    }

    /**
     * An utility method to add a new database from an xml attribute.
     * @param attrib the xml attributes
     * @return the database
     */
    public Database addDatabase(Attributes attrib) {
        final Database db = new Database();
        db.loadFromXML(attrib);
        addDatabase(db);
        return db;
    }

    /**
     * Add a database to the list and sets the AppData property to this AppData
     * @param db the database to add
     */
    public void addDatabase(Database db) { // called only once
        if (_database != null) {
            String msg = "Already added a database. DBFlute uses only one database objects.";
            throw new IllegalStateException(msg);
        }
        db.setAppData(this);
        if (db.getName() == null) {
            db.setName("default"); // Torque.getDefaultDB());
        }
        if (db.getDatabaseType() == null) {
            db.setDatabaseType(_databaseType);
        }
        _database = db;
    }

    /**
     * Initialize detail points after loading as final process.
     */
    private void doFinalInitialization() {
        if (!_isInitialized) {
            _database.doFinalInitialization();
            _isInitialized = true;
        }
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    /**
     * Creates a string representation of this AppData.
     * The representation is given in xml format.
     * @return representation in xml format
     */
    public String toString() { // basically no maintenance
        final StringBuilder result = new StringBuilder();

        result.append("<?xml version=\"1.0\"?>\n");
        result.append("<!DOCTYPE database SYSTEM \"" + DTDResolver.WEB_SITE_DTD + "\">\n");
        result.append("<!-- Autogenerated by SQLToXMLSchema! -->\n");
        result.append(_database);
        return result.toString();
    }
}
