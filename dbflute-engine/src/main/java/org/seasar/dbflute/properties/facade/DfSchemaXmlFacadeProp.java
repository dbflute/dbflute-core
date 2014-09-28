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
package org.seasar.dbflute.properties.facade;

import org.seasar.dbflute.properties.DfBasicProperties;

/**
 * @author jflute
 * @since 0.9.8.3 (2011/04/29 Friday)
 */
public class DfSchemaXmlFacadeProp {

    protected final DfBasicProperties _basicProp;

    public DfSchemaXmlFacadeProp(DfBasicProperties basicProp) {
        _basicProp = basicProp;
    }

    public String getProejctSchemaXMLEncoding() {
        return _basicProp.getProejctSchemaXMLEncoding();
    }

    public String getProejctSchemaXMLFile() {
        return _basicProp.getProejctSchemaXMLFile();
    }

    public String getProjectSchemaHistoryFile() {
        return _basicProp.getProjectSchemaHistoryFile();
    }
}
