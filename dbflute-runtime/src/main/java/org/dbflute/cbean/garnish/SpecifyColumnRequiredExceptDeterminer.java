/*
 * Copyright 2014-2018 the original author or authors.
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
package org.dbflute.cbean.garnish;

import org.dbflute.cbean.ConditionBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The determiner to except check of specify-column required. <br>
 * Used by DBFluteConfig so don't rename and move easily.
 * @author jflute
 * @since 1.1.7 (2018/03/14 Wednesday)
 */
@FunctionalInterface
public interface SpecifyColumnRequiredExceptDeterminer {

    /**
     * @param cb The currently executed condition-bean. (NotNull)
     * @return The determination, true or false.
     */
    boolean isExcept(ConditionBean cb);

    /**
     * for easy settings to multiple databases (you can set default determiner)
     * <pre>
     * SpecifyColumnRequiredExceptDeterminer.Bowgun.unlock();
     * SpecifyColumnRequiredExceptDeterminer.Bowgun.setDefaultDeterminer(...); // and locked
     * </pre>
     * @author jflute
     */
    class Bowgun {

        private static final Logger _log = LoggerFactory.getLogger(SpecifyColumnRequiredExceptDeterminer.Bowgun.class);

        protected static final SpecifyColumnRequiredExceptDeterminer _emptyDeterminer = cb -> false;
        protected static SpecifyColumnRequiredExceptDeterminer _defaultDeterminer;
        protected static boolean _locked = true;

        public static SpecifyColumnRequiredExceptDeterminer getDefaultDeterminer() { // not null
            return _defaultDeterminer != null ? _defaultDeterminer : _emptyDeterminer;
        }

        public static void setDefaultDeterminer(SpecifyColumnRequiredExceptDeterminer defaultDeterminer) {
            assertUnlocked();
            if (_log.isInfoEnabled()) {
                _log.info("...Setting default of specifyColumnRequiredExceptDeterminer: " + defaultDeterminer);
            }
            _defaultDeterminer = defaultDeterminer;
            lock();
        }

        public static boolean isLocked() {
            return _locked;
        }

        public static void lock() {
            if (_locked) {
                return;
            }
            if (_log.isInfoEnabled()) {
                _log.info("...Locking the determiner bowgun for specifyColumnRequiredExceptDeterminer!");
            }
            _locked = true;
        }

        public static void unlock() {
            if (!_locked) {
                return;
            }
            if (_log.isInfoEnabled()) {
                _log.info("...Unlocking the determiner bowgun for specifyColumnRequiredExceptDeterminer!");
            }
            _locked = false;
        }

        protected static void assertUnlocked() {
            if (!isLocked()) {
                return;
            }
            throw new IllegalStateException("The determiner bowgun for specifyColumnRequiredExceptDeterminer is locked.");
        }
    }
}
