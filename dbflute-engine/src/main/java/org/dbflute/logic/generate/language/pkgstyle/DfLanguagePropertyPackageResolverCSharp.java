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
package org.dbflute.logic.generate.language.pkgstyle;

/**
 * @author jflute
 * @since 1.0.5F (2014/05/04 Sunday)
 */
public class DfLanguagePropertyPackageResolverCSharp extends DfLanguagePropertyPackageResolver {

    protected String processLanguageType(String typeName, boolean exceptUtil) {
        final String listType = processListType(typeName, exceptUtil, "System.Collections.Generic", "IList");
        if (listType != null) {
            return listType;
        }
        final String mapType = processMapType(typeName, exceptUtil, "System.Collections.Generic", "IDictionary");
        if (mapType != null) {
            return mapType;
        }
        return null;
    }
}
