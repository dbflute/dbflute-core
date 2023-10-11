/*
 * Copyright 2014-2023 the original author or authors.
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
package org.dbflute.properties.assistant.base.dispatch;

import java.io.File;
import java.util.Map;

import org.dbflute.exception.DfIllegalPropertySettingException;
import org.dbflute.unit.EngineTestCase;
import org.dbflute.util.DfCollectionUtil;

/**
 * @author jflute
 */
public class DfDispatchVariableResolverTest extends EngineTestCase {

    // ===================================================================================
    //                                                           resolveDispatchVariable()
    //                                                           =========================
    public void test_resolveDispatchVariable_default_basic() {
        // ## Arrange ##
        DfDispatchVariableResolver resolver = createBasicResolver();

        // ## Act ##
        // ## Assert ##
        assertEquals("sea", resolver.resolveDispatchVariable("url", "$$env:DBFLUTE_UT_TMP$$ | sea"));
        assertEquals("land", resolver.resolveDispatchVariable("url", "$$env:DBFLUTE_UT_TMP$$ | df:dfprop/sea.dfprop | land"));
        assertException(IllegalStateException.class, () -> {
            resolver.resolveDispatchVariable("url", "$$env:DBFLUTE_UT_TMP$$ | df:dfprop/sea.dfprop");
        });
    }

    public void test_resolveDispatchVariable_default_part() {
        // ## Arrange ##
        DfDispatchVariableResolver resolver = createBasicResolver();

        // ## Act ##
        // ## Assert ##
        assertEquals("sea", resolver.resolveDispatchVariable("url", "mystic$$env:DBFLUTE_UT_TMP$$oneman | sea"));
        assertEquals("land", resolver.resolveDispatchVariable("url", "mytic$$env:DBFLUTE_UT_TMP$$oneman | df:dfprop/sea.dfprop | land"));
        assertException(IllegalStateException.class, () -> {
            resolver.resolveDispatchVariable("url", "mystic$$env:DBFLUTE_UT_TMP$$over | df:dfprop/sea.dfprop");
        });
    }

    public void test_resolveDispatchVariable_env_basic() {
        // ## Arrange ##
        DfDispatchVariableResolver resolver = createEnvResolver("maihama");

        // ## Act ##
        // ## Assert ##
        assertEquals("maihama", resolver.resolveDispatchVariable("url", "$$env:DBFLUTE_UT_TMP$$ | sea"));
        assertEquals("maihama", resolver.resolveDispatchVariable("url", "$$env:DBFLUTE_UT_TMP$$ | df:dfprop/sea.dfprop | land"));
        assertEquals("maihama", resolver.resolveDispatchVariable("url", "$$env:DBFLUTE_UT_TMP$$ | df:dfprop/sea.dfprop"));
    }

    public void test_resolveDispatchVariable_env_part() {
        // ## Arrange ##
        DfDispatchVariableResolver resolver = createEnvResolver("maihama");

        // ## Act ##
        // ## Assert ##
        assertEquals("mysticmaihamaoneman", resolver.resolveDispatchVariable("url", "mystic$$env:DBFLUTE_UT_TMP$$oneman | sea"));
        assertEquals("mystic maihama oneman", resolver.resolveDispatchVariable("url", "mystic $$env:DBFLUTE_UT_TMP$$ oneman | sea"));
        assertEquals("mysticmaihamaoneman",
                resolver.resolveDispatchVariable("url", "mystic$$env:DBFLUTE_UT_TMP$$oneman | df:dfprop/sea.dfprop | land"));
        assertEquals("mysticmaihamaoneman",
                resolver.resolveDispatchVariable("url", "mystic$$env:DBFLUTE_UT_TMP$$oneman | df:dfprop/sea.dfprop"));
    }

    public void test_resolveDispatchVariable_outsideFile() {
        // ## Arrange ##
        DfDispatchVariableResolver resolver = new DfDispatchVariableResolver() {
            @Override
            protected boolean existsOutsideFile(File dispatchFile) {
                return true;
            }

            @Override
            protected String readOutsideFileFirstLine(File dispatchFile, String defaultValue) {
                return "land";
            }
        };

        // ## Act ##
        // ## Assert ##
        assertEquals("land", resolver.resolveDispatchVariable("url", "df:dfprop/sea.dfprop"));
        assertEquals("land", resolver.resolveDispatchVariable("url", "$$env:DBFLUTE_UT_TMP$$ | df:dfprop/sea.dfprop"));
    }

    // ===================================================================================
    //                                                         handleEnvironmentVariable()
    //                                                         ===========================
    public void test_handleEnvironmentVariable_basic() {
        // ## Arrange ##
        DfDispatchVariableResolver resolver = createBasicResolver();

        // ## Act ##
        // ## Assert ##
        assertNull(resolver.handleEnvironmentVariable("url", "jdbc:mysql://localhost:3306/maihamadb"));
    }

    public void test_handleEnvironmentVariable_env_basic() {
        // ## Arrange ##
        DfDispatchVariableResolver resolver = createEnvResolver("sea");

        // ## Act ##
        DfEnvironmentVariableInfo info = resolver.handleEnvironmentVariable("url", "$$env:DBFLUTE_UT_TMP$$");

        // ## Assert ##
        assertEquals("sea", info.getEnvValue());
    }

    public void test_handleEnvironmentVariable_env_default() {
        // ## Arrange ##
        DfDispatchVariableResolver resolver = createBasicResolver();

        // ## Act ##
        DfEnvironmentVariableInfo info = resolver.handleEnvironmentVariable("url", "$$env:DBFLUTE_UT_TMP$$ | sea");

        // ## Assert ##
        assertEquals("sea", info.getEnvValue());
    }

    public void test_handleEnvironmentVariable_env_notFound() {
        // ## Arrange ##
        DfDispatchVariableResolver resolver = createBasicResolver();

        // ## Act ##
        // ## Assert ##
        assertException(DfIllegalPropertySettingException.class, () -> resolver.handleEnvironmentVariable("url", "$$env:DBFLUTE_UT_TMP$$"));
    }

    public void test_handleEnvironmentVariable_env_part() {
        // ## Arrange ##
        DfDispatchVariableResolver resolver = new DfDispatchVariableResolver() {
            @Override
            protected Map<String, String> extractEnvironmentMap() {
                return DfCollectionUtil.newHashMap("DBFLUTE_UT_TMP", "sea");
            }
        };

        // ## Act ##
        DfEnvironmentVariableInfo info = resolver.handleEnvironmentVariable("url", "mystic$$env:DBFLUTE_UT_TMP$$oneman");

        // ## Assert ##
        assertEquals("mysticseaoneman", info.getEnvValue());
    }

    public void test_handleEnvironmentVariable_env_with_outsideFile_default() {
        // ## Arrange ##
        DfDispatchVariableResolver resolver = createBasicResolver();

        // ## Act ##
        String plainValue = "$$env:DBFLUTE_UT_TMP$$ | df:dfprop/sea.dfprop | land";
        DfEnvironmentVariableInfo info = resolver.handleEnvironmentVariable("url", plainValue);

        // ## Assert ##
        assertEquals("df:dfprop/sea.dfprop | land", info.getEnvValue());
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    private DfDispatchVariableResolver createBasicResolver() {
        return new DfDispatchVariableResolver();
    }

    private DfDispatchVariableResolver createEnvResolver(String envValue) {
        return new DfDispatchVariableResolver() {
            @Override
            protected Map<String, String> extractEnvironmentMap() {
                return DfCollectionUtil.newHashMap("DBFLUTE_UT_TMP", envValue);
            }
        };
    }
}
