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
package org.seasar.dbflute.twowaysql;

import org.seasar.dbflute.exception.CommentTerminatorNotFoundException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.resource.DBFluteSystem;

/**
 * @author modified by jflute (originated in S2Dao)
 */
public class SqlTokenizer {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final int SQL = 1;
    public static final int COMMENT = 2;
    public static final int ELSE = 3;
    public static final int BIND_VARIABLE = 4;
    public static final int EOF = 99;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _sql;
    protected int _position = 0;
    protected String _token;
    protected int _tokenType = SQL;
    protected int _nextTokenType = SQL;
    protected int _bindVariableNum = 0;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public SqlTokenizer(String sql) {
        this._sql = sql;
    }

    // ===================================================================================
    //                                                                                Next
    //                                                                                ====
    public int next() {
        if (_position >= _sql.length()) {
            _token = null;
            _tokenType = EOF;
            _nextTokenType = EOF;
            return _tokenType;
        }
        switch (_nextTokenType) {
        case SQL:
            parseSql();
            break;
        case COMMENT:
            parseComment();
            break;
        case ELSE:
            parseElse();
            break;
        case BIND_VARIABLE:
            parseBindVariable();
            break;
        default:
            parseEof();
            break;
        }
        return _tokenType;
    }

    // ===================================================================================
    //                                                                           Parse SQL
    //                                                                           =========
    protected void parseSql() {
        int commentStartPos = _sql.indexOf("/*", _position);
        int commentStartPos2 = _sql.indexOf("#*", _position);
        if (0 < commentStartPos2 && commentStartPos2 < commentStartPos) {
            commentStartPos = commentStartPos2;
        }
        int bindVariableStartPos = _sql.indexOf("?", _position);
        int elseCommentStartPos = -1;
        int elseCommentLength = -1;
        int elseCommentSearchCurrentPosition = _position;
        while (true) { // searching nearest next ELSE comment
            final int lineCommentStartPos = _sql.indexOf("--", elseCommentSearchCurrentPosition);
            if (lineCommentStartPos < 0) {
                break;
            }
            if (calculateNextStartPos(commentStartPos, bindVariableStartPos, -1) < lineCommentStartPos) {
                break;
            }
            int skipPos = skipWhitespace(lineCommentStartPos + 2);
            if (skipPos + 4 < _sql.length() && "ELSE".equals(_sql.substring(skipPos, skipPos + 4))) {
                elseCommentStartPos = lineCommentStartPos;
                elseCommentLength = skipPos + 4 - lineCommentStartPos;
                break;
            }
            elseCommentSearchCurrentPosition = skipPos;
        }
        int nextStartPos = calculateNextStartPos(commentStartPos, bindVariableStartPos, elseCommentStartPos);
        if (nextStartPos < 0) {
            _token = _sql.substring(_position);
            _nextTokenType = EOF;
            _position = _sql.length();
            _tokenType = SQL;
        } else {
            _token = _sql.substring(_position, nextStartPos);
            _tokenType = SQL;
            boolean needNext = nextStartPos == _position;
            if (nextStartPos == commentStartPos) {
                _nextTokenType = COMMENT;
                _position = commentStartPos + 2;
            } else if (nextStartPos == elseCommentStartPos) {
                _nextTokenType = ELSE;
                _position = elseCommentStartPos + elseCommentLength;
            } else if (nextStartPos == bindVariableStartPos) {
                _nextTokenType = BIND_VARIABLE;
                _position = bindVariableStartPos;
            }
            if (needNext) {
                next();
            }
        }
    }

    protected int calculateNextStartPos(int commentStartPos, int bindVariableStartPos, int elseCommentStartPos) {
        int nextStartPos = -1;
        if (commentStartPos >= 0) {
            nextStartPos = commentStartPos;
        }
        if (bindVariableStartPos >= 0 && (nextStartPos < 0 || bindVariableStartPos < nextStartPos)) {
            nextStartPos = bindVariableStartPos;
        }
        if (elseCommentStartPos >= 0 && (nextStartPos < 0 || elseCommentStartPos < nextStartPos)) {
            nextStartPos = elseCommentStartPos;
        }
        return nextStartPos;
    }

    protected String nextBindVariableName() {
        return "$" + ++_bindVariableNum;
    }

    // ===================================================================================
    //                                                                       Parse Comment
    //                                                                       =============
    protected void parseComment() {
        int commentEndPos = _sql.indexOf("*/", _position);
        int commentEndPos2 = _sql.indexOf("*#", _position);
        if (0 < commentEndPos2 && commentEndPos2 < commentEndPos) {
            commentEndPos = commentEndPos2;
        }
        if (commentEndPos < 0) {
            throwCommentTerminatorNotFoundException(_sql.substring(_position));
        }
        _token = _sql.substring(_position, commentEndPos);
        _nextTokenType = SQL;
        _position = commentEndPos + 2;
        _tokenType = COMMENT;
    }

    protected void throwCommentTerminatorNotFoundException(String expression) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The comment end was NOT found!");
        br.addItem("Advice");
        br.addElement("Please confirm the SQL comment writing.");
        br.addElement("Any comments DOESN'T have a comment end.");
        br.addElement("For example:");
        br.addElement("  (x) -- /*pmb.xxxId3");
        br.addElement("  (o) -- /*pmb.xxxId*/3");
        br.addItem("Specified SQL");
        br.addElement(expression);
        br.addItem("Comment Expression");
        br.addElement(_sql);
        final String msg = br.buildExceptionMessage();
        throw new CommentTerminatorNotFoundException(msg);
    }

    // ===================================================================================
    //                                                                     Parse Parameter
    //                                                                     ===============
    protected void parseBindVariable() {
        _token = nextBindVariableName();
        _nextTokenType = SQL;
        _position += 1;
        _tokenType = BIND_VARIABLE;
    }

    protected void parseElse() {
        _token = null;
        _nextTokenType = SQL;
        _tokenType = ELSE;
    }

    protected void parseEof() {
        _token = null;
        _tokenType = EOF;
        _nextTokenType = EOF;
    }

    // ===================================================================================
    //                                                                      Skip Character
    //                                                                      ==============
    public String skipToken() {
        return skipToken(false);
    }

    public String skipToken(boolean testValue) {
        int index = _sql.length(); // last index as default

        final String dateLiteralPrefix = extractDateLiteralPrefix(testValue, _sql, _position);
        if (dateLiteralPrefix != null) {
            _position = _position + dateLiteralPrefix.length();
        }

        final char quote;
        {
            final char firstChar = (_position < _sql.length() ? _sql.charAt(_position) : '\0');
            quote = (firstChar == '(' ? ')' : firstChar);
        }
        final boolean quoting = quote == '\'' || quote == ')';

        for (int i = quoting ? _position + 1 : _position; i < _sql.length(); ++i) {
            final char c = _sql.charAt(i);
            if (isNotQuoteEndPoint(quoting, c)) {
                index = i;
                break;
            } else if (isBlockCommentBeginPoint(_sql, c, i)) {
                index = i;
                break;
            } else if (isLineCommentBeginPoint(_sql, c, i)) {
                index = i;
                break;
            } else if (quoting && isSingleQuoteEndPoint(_sql, quote, c, i)) {
                index = i + 1;
                break;
            } else if (quoting && isQuoteEndPoint(_sql, quote, c, i)) {
                index = i + 1;
                break;
            }
        }
        _token = _sql.substring(_position, index);
        if (dateLiteralPrefix != null) {
            _token = dateLiteralPrefix + _token;
        }
        _tokenType = SQL;
        _nextTokenType = SQL;
        _position = index;
        return _token;
    }

    protected String extractDateLiteralPrefix(boolean testValue, String currentSql, int position) {
        if (!testValue) {
            return null;
        }
        if (position >= currentSql.length()) {
            return null;
        }
        final char firstChar = currentSql.charAt(position);
        if (firstChar != 'd' && firstChar != 'D' && firstChar != 't' && firstChar != 'T') {
            return null;
        }
        final String rear;
        {
            final String tmpRear = currentSql.substring(position);
            final int maxlength = "timestamp '".length();
            if (tmpRear.length() > maxlength) {
                // get only the quantity needed for performance
                rear = tmpRear.substring(0, maxlength);
            } else {
                rear = tmpRear;
            }
        }
        final String lowerRear = rear.toLowerCase();
        String literalPrefix = null;
        if (lowerRear.startsWith("date '")) {
            literalPrefix = rear.substring(0, "date ".length());
        } else if (lowerRear.startsWith("date'")) {
            literalPrefix = rear.substring(0, "date".length());
        } else if (lowerRear.startsWith("timestamp '")) { // has max length
            literalPrefix = rear.substring(0, "timestamp ".length());
        } else if (lowerRear.startsWith("timestamp'")) {
            literalPrefix = rear.substring(0, "timestamp".length());
        }
        return literalPrefix;
    }

    protected boolean isNotQuoteEndPoint(boolean quoting, char c) {
        return !quoting && (Character.isWhitespace(c) || c == ',' || c == ')' || c == '(');
    }

    protected boolean isBlockCommentBeginPoint(String currentSql, char c, int i) {
        return c == '/' && isNextCharacter(currentSql, i, '*');
    }

    protected boolean isLineCommentBeginPoint(String currentSql, char c, int i) {
        return c == '-' && isNextCharacter(currentSql, i, '-');
    }

    protected boolean isSingleQuoteEndPoint(String currentSql, char quote, char c, int i) {
        final int sqlLen = currentSql.length();
        final boolean endSqlOrNotEscapeQuote = (i + 1 >= sqlLen || currentSql.charAt(i + 1) != '\'');
        return quote == '\'' && c == '\'' && endSqlOrNotEscapeQuote;
    }

    protected boolean isQuoteEndPoint(String currentSql, char quote, char c, int i) {
        return c == quote;
    }

    protected boolean isNextCharacter(String currentSql, int i, char targetChar) {
        return i + 1 < currentSql.length() && currentSql.charAt(i + 1) == targetChar;
    }

    public String skipWhitespace() {
        int index = skipWhitespace(_position);
        _token = _sql.substring(_position, index);
        _position = index;
        return _token;
    }

    protected int skipWhitespace(int position) {
        int index = _sql.length();
        for (int i = position; i < _sql.length(); ++i) {
            char c = _sql.charAt(i);
            if (!Character.isWhitespace(c)) {
                index = i;
                break;
            }
        }
        return index;
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String ln() {
        return DBFluteSystem.getBasicLn();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public int getPosition() {
        return _position;
    }

    public String getToken() {
        return _token;
    }

    public String getBefore() {
        return _sql.substring(0, _position);
    }

    public String getAfter() {
        return _sql.substring(_position);
    }

    public int getTokenType() {
        return _tokenType;
    }

    public int getNextTokenType() {
        return _nextTokenType;
    }
}
