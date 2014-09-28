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
package org.seasar.dbflute.helper.token.file;

import java.io.IOException;
import java.sql.SQLException;

/**
 * The callback of file-making with writer.
 * <pre>
 * File tsvFile = ... <span style="color: #3F7E5E">// output file</span>
 * List&lt;String&gt; columnNameList = ... <span style="color: #3F7E5E">// columns for header</span>
 * FileToken fileToken = new FileToken();
 * fileToken.make(new FileOutputStream(tsvFile), new FileMakingCallback() {
 *     public void write(FileMakingRowWriter writer, FileMakingRowAcceptor acceptor) throws IOException, SQLException {
 *         for (Member member : ...) { <span style="color: #3F7E5E">// output data loop</span>
 *             List&lt;String&gt; valueList = ...; <span style="color: #3F7E5E">// convert the member to the row resource</span>
 *             writer.<span style="color: #AD4747">writeRow</span>(valueList); <span style="color: #3F7E5E">// Yes, you write!</span>
 *         }
 *     }
 * }, new FileMakingOption().delimitateByTab().encodeAsUTF8().headerInfo(columnNameList));
 * </pre>
 * @author jflute
 */
public interface FileMakingCallback {

    /**
     * Make (write) token file by row writer that accepts row resources.
     * @param writer The row writer of file-making. (NotNull)
     * @throws IOException When the IO handling fails in the row writing process.
     * @throws SQLException When the SQL handling fails in the row writing process.
     */
    void write(FileMakingRowWriter writer) throws IOException, SQLException;
}
