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
package org.dbflute.logic.generate.language.framework;

/**
 * @author jflute
 */
public interface DfLanguageFramework {

    /**
     * @return The name-space of Seasar dbflute.dicon. (NotNull)
     */
    String getDBFluteDiconNamespace();

    /**
     * @return The file name of Seasar dbflute.dicon. (NotNull)
     */
    String getDBFluteDiconFileName();

    /**
     * @return The resource name of Seasar j2ee.dicon. (NotNull)
     */
    String getJ2eeDiconResourceName();

    /**
     * @return The name-space of Lasta Di dbflute.xml. (NotNull)
     */
    String getDBFluteDiXmlNamespace();

    /**
     * @return The file name of Lasta Di dbflute.xml. (NotNull)
     */
    String getDBFluteDiXmlFileName();

    /**
     * @return The resource name of Lasta Di j2ee.xml. (NotNull)
     */
    String getRdbDiXmlResourceName();

    /**
     * @return Does it make S2Dao's interface?
     */
    boolean isMakeDaoInterface();
}