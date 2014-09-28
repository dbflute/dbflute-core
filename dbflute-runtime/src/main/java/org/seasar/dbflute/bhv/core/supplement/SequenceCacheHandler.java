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
package org.seasar.dbflute.bhv.core.supplement;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.seasar.dbflute.DBDef;
import org.seasar.dbflute.XLog;
import org.seasar.dbflute.exception.SequenceCacheIllegalStateException;
import org.seasar.dbflute.exception.SequenceCacheSizeNotDividedIncrementSizeException;
import org.seasar.dbflute.resource.DBFluteSystem;
import org.seasar.dbflute.resource.ResourceContext;
import org.seasar.dbflute.util.Srl;

/**
 * The handler of sequence cache.
 * @author jflute
 * @since 0.9.6.4 (2010/01/15 Friday)
 */
public class SequenceCacheHandler {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The map of sequence cache keyed by unique strings of the sequence. (synchronized manually as transaction) */
    protected final Map<String, SequenceCache> _sequenceCacheMap = newConcurrentHashMap();

    /** The lock object to synchronize this sequence cache for transaction. (NotNull) */
    protected final Object _sequenceCacheLock = new Object();

    protected SequenceCacheKeyGenerator _sequenceCacheKeyGenerator;
    protected boolean _internalDebug;

    // ===================================================================================
    //                                                                            Handling
    //                                                                            ========
    /**
     * @param tableName The name of table. (NotNull)
     * @param sequenceName The name of sequence. (NotNull)
     * @param dataSource The data source for a database connection. (NotNull)
     * @param resultType The type of sequence result. (NotNull)
     * @param cacheSize The size of sequence cache. (NullAllowed: if null, returns null)
     * @param incrementSize The size of increment of sequence. (NullAllowed, f null, batch way is invalid) 
     * @return The object for sequence cache. (NullAllowed) 
     */
    public SequenceCache findSequenceCache(String tableName, String sequenceName, DataSource dataSource,
            Class<?> resultType, Integer cacheSize, Integer incrementSize) {
        if (cacheSize == null || cacheSize <= 1) { // if it is not cache valid size
            return null;
        }
        final String key = generateKey(tableName, sequenceName, dataSource);
        SequenceCache sequenceCache = getSequenceCache(key);
        if (sequenceCache != null) {
            return sequenceCache;
        }
        synchronized (_sequenceCacheLock) {
            sequenceCache = getSequenceCache(key);
            if (sequenceCache != null) {
                // previous thread might have initialized
                // or reading might failed by same-time writing
                return sequenceCache;
            }
            if (isLogEnabled()) {
                log("...Initializing sequence cache: " + sequenceName + ":cache(" + cacheSize + ")");
            }
            sequenceCache = createSequenceCache(sequenceName, dataSource, resultType, cacheSize, incrementSize);
            _sequenceCacheMap.put(key, sequenceCache);
        }
        if (sequenceCache == null) {
            String msg = "createSequenceCache() should not return null:";
            msg = msg + " sequenceName=" + sequenceName + " dataSource=" + dataSource;
            throw new SequenceCacheIllegalStateException(msg);
        }
        return sequenceCache;
    }

    protected SequenceCache getSequenceCache(String key) {
        return _sequenceCacheMap.get(key);
    }

    protected SequenceCache createSequenceCache(String sequenceName, DataSource dataSource, Class<?> resultType,
            Integer cacheSize, Integer incrementSize) {
        final SequenceCache cache = new SequenceCache(resultType, new BigDecimal(cacheSize), incrementSize);
        cache.setInternalDebug(_internalDebug);
        return cache;
    }

    protected String generateKey(String tableName, String sequenceName, DataSource dataSource) {
        if (_sequenceCacheKeyGenerator != null) {
            return _sequenceCacheKeyGenerator.generateKey(tableName, sequenceName, dataSource);
        }
        return tableName + "." + sequenceName; // as default
    }

    // ===================================================================================
    //                                                                      Union Sequence
    //                                                                      ==============
    /**
     * Filter the SQL for next value. <br />
     * This method uses ResourceContext.
     * @param cacheSize The cache size of sequence. (NotNull, CacheValidSize)
     * @param incrementSize The increment size of sequence. (NotNull, NotMinus, NotZero)
     * @param nextValSql The SQL for next value. (NotNull, NotTrimmedEmpty)
     * @return The filtered SQL. (NotNull, NotTrimmedEmpty)
     */
    public String filterNextValSql(Integer cacheSize, Integer incrementSize, String nextValSql) {
        assertFilterArgumentValid(cacheSize, incrementSize, nextValSql);
        assertCacheSizeCanBeDividedByIncrementSize(cacheSize, incrementSize, nextValSql);
        final Integer divided = cacheSize / incrementSize;
        final Integer unionCount = divided - 1;
        final StringBuilder sb = new StringBuilder();
        if (unionCount > 0) { // "batch" way
            if (ResourceContext.isCurrentDBDef(DBDef.Oracle)) { // patch
                sb.append(buildNextValSqlOnOracle(nextValSql, divided, unionCount));
            } else if (ResourceContext.isCurrentDBDef(DBDef.DB2)) { // patch
                sb.append(buildNextValSqlOnDB2(nextValSql, divided, unionCount));
            } else { // basically PostgreSQL and H2
                sb.append(buildNextValSqlUsingUnionAll(nextValSql, divided, unionCount));
            }
        } else { // "increment" way
            sb.append(nextValSql);
        }
        return sb.toString();
    }

    protected String buildNextValSqlOnOracle(String nextValSql, Integer divided, Integer unionCount) {
        final StringBuilder sb = new StringBuilder();
        sb.append(Srl.replace(nextValSql, "from dual", ln() + "  from ("));
        final Integer maxDualCountInOneJoin = 10;
        int allRecordCount = 0;
        boolean reached = false;
        for (int joinIndex = 0; !reached; joinIndex++) {
            if (joinIndex >= 1) {
                sb.append(ln()).append("    cross join (");
            }
            int dualCountInOneJoin = 0;
            sb.append("select * from dual");
            ++dualCountInOneJoin;
            final String indent = (joinIndex >= 1 ? "                " : "        ");
            int calculatedRecordCount = 0;

            // always more one loop
            for (int unionIndex = 0; unionIndex < (maxDualCountInOneJoin - 1); unionIndex++) {
                sb.append(ln()).append(indent).append(" union all");
                sb.append(ln()).append(indent).append("select * from dual");
                ++dualCountInOneJoin;
                if (allRecordCount == 0) {
                    calculatedRecordCount = dualCountInOneJoin;
                } else {
                    // cross-joined record count
                    calculatedRecordCount = (allRecordCount * dualCountInOneJoin);
                }
                if (calculatedRecordCount >= divided) {
                    reached = true;
                    break;
                }
            }
            allRecordCount = calculatedRecordCount;
            sb.append(") join_").append(joinIndex + 1);
        }
        sb.append(ln()).append(" where rownum <= ").append(divided);
        return sb.toString();
    }

    // for override code (so basically unused)
    protected final String buildNextValSqlOnOracleUsingConnectBy(String nextValSql, Integer divided, Integer unionCount) {
        final StringBuilder sb = new StringBuilder();
        final String viewSql = "select level from dual connect by level <= " + unionCount;
        sb.append(Srl.replace(nextValSql, "from dual", "from (" + viewSql + ")"));
        return sb.toString();
    }

    protected String buildNextValSqlOnDB2(String nextValSql, Integer divided, Integer unionCount) {
        final StringBuilder sb = new StringBuilder();
        final String viewSql = "values (1) union all select N + 1 from NUM where n <= " + unionCount;
        sb.append("with NUM (N) as (").append(viewSql).append(")");
        sb.append(ln()).append(Srl.replace(nextValSql, "values", "select")).append(" from NUM");
        return sb.toString();
    }

    protected String buildNextValSqlUsingUnionAll(String nextValSql, Integer divided, Integer unionCount) {
        final StringBuilder sb = new StringBuilder();
        sb.append(nextValSql);
        for (int i = 0; i < unionCount; i++) {
            sb.append(ln()).append(" union all ");
            sb.append(ln()).append(nextValSql);
        }
        sb.append(ln()).append(" order by 1 asc");
        return sb.toString();
    }

    protected void assertFilterArgumentValid(Integer cacheSize, Integer incrementSize, String nextValSql) {
        if (cacheSize == null || cacheSize <= 1) {
            String msg = "The argument 'cacheSize' should be cache valid size: " + cacheSize;
            throw new SequenceCacheIllegalStateException(msg);
        }
        if (incrementSize == null || incrementSize <= 0) {
            String msg = "The argument 'incrementSize' should be plus size: " + incrementSize;
            throw new SequenceCacheIllegalStateException(msg);
        }
        if (nextValSql == null || nextValSql.trim().length() == 0) {
            String msg = "The argument 'nextValSql' should be valid: " + nextValSql;
            throw new SequenceCacheIllegalStateException(msg);
        }
    }

    protected void assertCacheSizeCanBeDividedByIncrementSize(Integer cacheSize, Integer incrementSize,
            String nextValSql) {
        final Integer extraValue = cacheSize % incrementSize;
        if (extraValue != 0) {
            throwSequenceCacheSizeNotDividedIncrementSizeException(cacheSize, incrementSize, nextValSql);
        }
    }

    protected void throwSequenceCacheSizeNotDividedIncrementSizeException(Integer cacheSize, Integer incrementSize,
            String nextValSql) {
        String msg = "Look! Read the message below." + ln();
        msg = msg + "/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *" + ln();
        msg = msg + "The cache size cannot be divided by increment size!" + ln();
        msg = msg + ln();
        msg = msg + "[Advice]" + ln();
        msg = msg + "Please confirm sequence increment size and dfcache size setting." + ln();
        msg = msg + "  For example:" + ln();
        msg = msg + "    (x) - cacheSize = 50, incrementSize = 3" + ln();
        msg = msg + "    (x) - cacheSize = 50, incrementSize = 27" + ln();
        msg = msg + "    (o) - cacheSize = 50, incrementSize = 1" + ln();
        msg = msg + "    (o) - cacheSize = 50, incrementSize = 50" + ln();
        msg = msg + "    (o) - cacheSize = 50, incrementSize = 2" + ln();
        msg = msg + ln();
        msg = msg + "[Cache Size]" + ln() + cacheSize + ln();
        msg = msg + ln();
        msg = msg + "[Increment Size]" + ln() + incrementSize + ln();
        msg = msg + ln();
        msg = msg + "[SQL for Next Value]" + ln() + nextValSql + ln();
        msg = msg + "* * * * * * * * * */";
        throw new SequenceCacheSizeNotDividedIncrementSizeException(msg);
    }

    // ===================================================================================
    //                                                                                 Log
    //                                                                                 ===
    protected void log(String msg) {
        XLog.log(msg);
    }

    protected boolean isLogEnabled() {
        return XLog.isLogEnabled();
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected <KEY, VALUE> ConcurrentHashMap<KEY, VALUE> newConcurrentHashMap() {
        return new ConcurrentHashMap<KEY, VALUE>();
    }

    protected String ln() {
        return DBFluteSystem.getBasicLn();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void setSequenceCacheKeyGenerator(SequenceCacheKeyGenerator sequenceCacheKeyGenerator) {
        _sequenceCacheKeyGenerator = sequenceCacheKeyGenerator;
    }

    public void setInternalDebug(boolean internalDebug) {
        _internalDebug = internalDebug;
    }
}
