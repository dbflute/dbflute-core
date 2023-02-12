package org.dbflute.logic.replaceschema.loaddata.delimiter.secretary;

import org.dbflute.unit.EngineTestCase;

/**
 * @author jflute
 * @since 1.2.7 (2023/02/06 Monday at roppongi japanese)
 */
public class DfDelimiterDataTableDbNameExtractorTest extends EngineTestCase {

    public void test_extractTableDbName_basic() {
        // orthodox
        assertEquals("HANGAR_MYSTIC", createExtractor("./HANGAR_MYSTIC.tsv").extractTableDbName());
        assertEquals("HANGAR_MYSTIC", createExtractor("./sea/HANGAR_MYSTIC.tsv").extractTableDbName());
        assertEquals("hangar_mystic", createExtractor("./sea/hangar_mystic.tsv").extractTableDbName());
        assertEquals("MYSTIC", createExtractor("./sea/MYSTIC.tsv").extractTableDbName());

        // no slash
        assertEquals("HANGAR_MYSTIC", createExtractor("HANGAR_MYSTIC.tsv").extractTableDbName());
        assertEquals("MYSTIC", createExtractor("MYSTIC.tsv").extractTableDbName());

        // no extension
        assertException(StringIndexOutOfBoundsException.class, () -> { // unexpected but no problem
            createExtractor("HANGAR_MYSTIC").extractTableDbName();
        });

        // hyphenated (first one is delimiter)
        assertEquals("HANGAR_MYSTIC", createExtractor("./sea-HANGAR_MYSTIC.tsv").extractTableDbName());
        assertEquals("lostriver-HANGAR_MYSTIC", createExtractor("./sea-lostriver-HANGAR_MYSTIC.tsv").extractTableDbName());
    }

    protected DfDelimiterDataTableDbNameExtractor createExtractor(String filePath) {
        return new DfDelimiterDataTableDbNameExtractor(filePath);
    }
}
