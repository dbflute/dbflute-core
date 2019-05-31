package org.dbflute.logic.generate.refresh;

import java.io.IOException;
import java.util.Arrays;

import org.dbflute.unit.EngineTestCase;

/**
 * @author jflute
 */
public class DfRefreshResourceProcessTest extends EngineTestCase {

    public void test_buildContinuedIOExceptionMessage_basic() {
        // ## Arrange ##
        DfRefreshResourceProcess process = new DfRefreshResourceProcess(Arrays.asList("sea"), "http://dbflute.org");

        // ## Act ##
        // ## Assert ##
        // expects no exception and visual check
        process.handleRefreshIOException(new IOException());
        process.handleRefreshIOException(new IOException("mystic"));
    }
}
