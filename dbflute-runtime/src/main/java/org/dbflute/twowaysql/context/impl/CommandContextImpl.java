/*
 * Copyright 2014-2019 the original author or authors.
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
package org.dbflute.twowaysql.context.impl;

import java.util.List;

import org.dbflute.helper.StringKeyMap;
import org.dbflute.twowaysql.context.CommandContext;
import org.dbflute.util.DfCollectionUtil;

/**
 * @author modified by jflute (originated in S2Dao)
 */
public class CommandContextImpl implements CommandContext {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The arguments. it should be allowed null value. */
    protected final StringKeyMap<Object> _args = StringKeyMap.createAsCaseInsensitive();

    /** The types of argument. it should be allowed null value. */
    protected final StringKeyMap<Class<?>> _argTypes = StringKeyMap.createAsCaseInsensitive();

    protected final StringBuilder _sqlSb = new StringBuilder(100);
    protected final List<Object> _bindVariables = DfCollectionUtil.newArrayList();
    protected final List<Class<?>> _bindVariableTypes = DfCollectionUtil.newArrayList();
    protected final CommandContext _parent;

    protected boolean _enabled;
    protected boolean _beginChild;
    protected boolean _alreadySkippedConnector;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * Constructor for root context.
     */
    protected CommandContextImpl() {
        _parent = null;
        _enabled = true; // immutable since here
    }

    /**
     * Constructor for child context.
     * @param parent The parent context. (NotNull)
     */
    protected CommandContextImpl(CommandContext parent) {
        _parent = parent;
        _enabled = false; // changing depends on child elements
    }

    // -----------------------------------------------------
    //                                               Factory
    //                                               -------
    /**
     * Create the implementation of command context as root.
     * @return The implementation of command context as root. (NotNull)
     */
    public static CommandContextImpl createCommandContextImplAsRoot() { // basically for creator
        return new CommandContextImpl();
    }

    /**
     * Create the implementation of command context as BEGIN child.
     * @param parent The parent context. (NotNull)
     * @return The implementation of command context as BEGIN child. (NotNull)
     */
    public static CommandContextImpl createCommandContextImplAsBeginChild(CommandContext parent) {
        return new CommandContextImpl(parent).asBeginChild();
    }

    protected CommandContextImpl asBeginChild() {
        _beginChild = true;
        return this;
    }

    // ===================================================================================
    //                                                                    Context Handling
    //                                                                    ================
    public Object getArg(String name) {
        if (_args.containsKey(name)) {
            return _args.get(name);
        } else if (_parent != null) {
            return _parent.getArg(name);
        } else {
            if (_args.size() == 1) {
                String firstKey = _args.keySet().iterator().next();
                return _args.get(firstKey);
            }
            return null;
        }
    }

    public Class<?> getArgType(String name) {
        if (_argTypes.containsKey(name)) {
            return (Class<?>) _argTypes.get(name);
        } else if (_parent != null) {
            return _parent.getArgType(name);
        } else {
            if (_argTypes.size() == 1) {
                String firstKey = _argTypes.keySet().iterator().next();
                return _argTypes.get(firstKey);
            }
            return null;
        }
    }

    public void addArg(String name, Object arg, Class<?> argType) {
        _args.put(name, arg);
        _argTypes.put(name, argType);
    }

    public String getSql() {
        return _sqlSb.toString();
    }

    public Object[] getBindVariables() {
        return _bindVariables.toArray(new Object[_bindVariables.size()]);
    }

    public Class<?>[] getBindVariableTypes() {
        return (Class<?>[]) _bindVariableTypes.toArray(new Class[_bindVariableTypes.size()]);
    }

    public CommandContext addSql(String sql) {
        _sqlSb.append(sql);
        return this;
    }

    public CommandContext addSql(String sql, Object bindVariable, Class<?> bindVariableType) {
        _sqlSb.append(sql);
        _bindVariables.add(bindVariable);
        _bindVariableTypes.add(bindVariableType);
        return this;
    }

    public CommandContext addSql(String sql, Object[] bindVariables, Class<?>[] bindVariableTypes) {
        _sqlSb.append(sql);
        for (int i = 0; i < bindVariables.length; ++i) {
            _bindVariables.add(bindVariables[i]);
            _bindVariableTypes.add(bindVariableTypes[i]);
        }
        return this;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append(_sqlSb).append(", ");
        sb.append(_enabled).append(", ");
        sb.append(_beginChild).append(", ");
        sb.append(_alreadySkippedConnector).append(", ");
        sb.append("parent=").append(_parent);
        sb.append("}@").append(Integer.toHexString(hashCode()));
        return sb.toString();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public boolean isEnabled() {
        return _enabled;
    }

    public void setEnabled(boolean enabled) {
        _enabled = enabled;
    }

    public boolean isBeginChild() {
        return _beginChild;
    }

    public boolean isAlreadySkippedConnector() {
        return _alreadySkippedConnector;
    }

    public void setAlreadySkippedConnector(boolean alreadySkippedConnector) {
        _alreadySkippedConnector = alreadySkippedConnector;
    }
}