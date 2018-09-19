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
    public void test_resolveDispatchVariable_basic() {
        // ## Arrange ##
        DfDispatchVariableResolver resolver = new DfDispatchVariableResolver();

        // ## Act ##
        // ## Assert ##
        assertEquals("sea", resolver.resolveDispatchVariable("url", "$$env:DBFLUTE_UT_TMP$$ | sea"));
        assertEquals("land", resolver.resolveDispatchVariable("url", "$$env:DBFLUTE_UT_TMP$$ | df:dfprop/sea.dfprop | land"));
        assertException(IllegalStateException.class, () -> {
            resolver.resolveDispatchVariable("url", "$$env:DBFLUTE_UT_TMP$$ | df:dfprop/sea.dfprop");
        });
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

    public void test_handleEnvironmentVariable_env_with_outsideFile_default() {
        // ## Arrange ##
        DfDispatchVariableResolver resolver = new DfDispatchVariableResolver();

        // ## Act ##
        String plainValue = "$$env:DBFLUTE_UT_TMP$$ | df:dfprop/sea.dfprop | land";
        DfEnvironmentVariableInfo info = resolver.handleEnvironmentVariable("url", plainValue);

        // ## Assert ##
        assertEquals("df:dfprop/sea.dfprop | land", info.getEnvValue());
    }
}
