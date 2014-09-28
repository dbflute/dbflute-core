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
package org.seasar.dbflute.logic.generate.language.pkgstyle;

/**
 * @author jflute
 * @since 1.0.5F (2014/05/04 Sunday)
 */
public class DfLanguagePropertyPackageResolverScala extends DfLanguagePropertyPackageResolver {

    protected final DfLanguagePropertyPackageResolverJava _resolverJava = new DfLanguagePropertyPackageResolverJava() {

        @Override
        protected String getListPackage() {
            return "scala.collection.immutable";
        };

        @Override
        protected String getMapPackage() {
            return "scala.collection.immutable";
        };

        @Override
        protected String getBigDecimalPackage() {
            return "scala.math";
        }

        protected String getJava8LocalDate() {
            return "java.time.LocalDate";
        }

        @Override
        protected String getJava8LocalDateTime() {
            return "java.time.LocalDateTime";
        }

        @Override
        protected String getJodaLocalDate() {
            return "org.joda.time.LocalDate";
        }

        @Override
        protected String getJodaLocalDateTime() {
            return "org.joda.time.LocalDateTime";
        }
    };

    protected String processLanguageType(String typeName, boolean exceptUtil) {
        return _resolverJava.processLanguageType(typeName, exceptUtil);
    }
}
