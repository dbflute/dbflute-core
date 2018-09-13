package org.dbflute.properties.assistant.base.dispatch;

import java.util.Map;

import org.dbflute.exception.DfIllegalPropertySettingException;
import org.dbflute.unit.EngineTestCase;
import org.dbflute.util.DfCollectionUtil;

/**
 * @author jflute
 */
public class DfDispatchVariableResolverTest extends EngineTestCase {

    public void test_handleEnvironmentVariable_basic() {
        // ## Arrange ##
        DfDispatchVariableResolver resolver = new DfDispatchVariableResolver();

        // ## Act ##
        // ## Assert ##
        assertNull(resolver.handleEnvironmentVariable("url", "jdbc:mysql://localhost:3306/maihamadb"));
    }

    public void test_handleEnvironmentVariable_env_basic() {
        // ## Arrange ##
        DfDispatchVariableResolver resolver = new DfDispatchVariableResolver() {
            @Override
            protected Map<String, String> extractEnvironmentMap() {
                return DfCollectionUtil.newHashMap("DBFLUTE_UT_TMP", "sea");
            }
        };

        // ## Act ##
        DfEnvironmentVariableInfo info = resolver.handleEnvironmentVariable("url", "$$env:DBFLUTE_UT_TMP$$");

        // ## Assert ##
        assertEquals("sea", info.getEnvValue());
    }

    public void test_handleEnvironmentVariable_env_default() {
        // ## Arrange ##
        DfDispatchVariableResolver resolver = new DfDispatchVariableResolver();

        // ## Act ##
        DfEnvironmentVariableInfo info = resolver.handleEnvironmentVariable("url", "$$env:DBFLUTE_UT_TMP$$ | sea");

        // ## Assert ##
        assertEquals("sea", info.getEnvValue());
    }

    public void test_handleEnvironmentVariable_env_notFound() {
        // ## Arrange ##
        DfDispatchVariableResolver resolver = new DfDispatchVariableResolver();

        // ## Act ##
        // ## Assert ##
        assertException(DfIllegalPropertySettingException.class, () -> resolver.handleEnvironmentVariable("url", "$$env:DBFLUTE_UT_TMP$$"));
    }
}
