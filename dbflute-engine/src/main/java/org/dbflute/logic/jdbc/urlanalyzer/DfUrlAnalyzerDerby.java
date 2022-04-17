/*
 * Copyright 2014-2022 the original author or authors.
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
package org.dbflute.logic.jdbc.urlanalyzer;

import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 0.9.6.8 (2010/04/17 Saturday)
 */
public class DfUrlAnalyzerDerby extends DfUrlAnalyzerBase {

    public DfUrlAnalyzerDerby(String url) {
        super(url);
    }

    protected String doExtractCatalog() {
        final String pureUrl = Srl.substringFirstFront(_url, ";", "?", "&");
        final String catalog = Srl.substringLastRear(pureUrl, "/", ":");
        return !catalog.equals(pureUrl) ? catalog : null;
    }
}
