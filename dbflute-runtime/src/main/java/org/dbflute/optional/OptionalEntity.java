/*
 * Copyright 2014-2014 the original author or authors.
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
package org.dbflute.optional;

import org.dbflute.exception.EntityAlreadyDeletedException;
import org.dbflute.exception.NonSetupSelectRelationAccessException;
import org.dbflute.helper.message.ExceptionMessageBuilder;

/**
 * The entity as optional object, which has entity instance in it. <br />
 * You can handle null value by this methods without direct null handling.
 * <pre>
 * MemberCB cb = new MemberCB();
 * cb.query().set...
 * OptionalEntity&lt;Member&gt; entity = memberBhv.selectEntity(cb);
 *
 * <span style="color: #3F7E5E">// if the data always exists as your business rule</span>
 * entity.<span style="color: #DD4747">required</span>(member -&gt; {
 *     ...
 * });
 * Member member = entity.<span style="color: #DD4747">get()</span>;
 *
 * <span style="color: #3F7E5E">// if it might be no data, isPresent(), orElseNull(), ...</span>
 * entity.<span style="color: #DD4747">ifPresent</span>(member -&gt; {
 *     ...
 * });
 * if (entity.<span style="color: #DD4747">isPresent()</span>) {
 *     Member member = entity.<span style="color: #DD4747">get()</span>;
 * } else {
 *     ...
 * }
 * </pre>
 * @param <ENTITY> The type of entity.
 * @author jflute
 * @since 1.0.5F (2014/05/05 Monday)
 */
public class OptionalEntity<ENTITY> extends BaseOptional<ENTITY> {

    // TODO jflute fix optional javadoc
    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final OptionalEntity<Object> EMPTY_INSTANCE;
    static {
        EMPTY_INSTANCE = new OptionalEntity<Object>(null, new OptionalThingExceptionThrower() {
            public void throwNotFoundException() {
                String msg = "The empty optional so the value is null.";
                throw new EntityAlreadyDeletedException(msg);
            }
        });
    }
    protected static final OptionalThingExceptionThrower NOWAY_THROWER = new OptionalThingExceptionThrower() {
        public void throwNotFoundException() {
            throw new EntityAlreadyDeletedException("no way");
        }
    };

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * @param entity The wrapped instance of entity. (NullAllowed)
     * @param thrower The exception thrower when illegal access. (NotNull)
     */
    public OptionalEntity(ENTITY entity, OptionalThingExceptionThrower thrower) { // basically called by DBFlute
        super(entity, thrower);
    }

    /**
     * @return The fixed instance as empty. (NotNull)
     */
    @SuppressWarnings("unchecked")
    public static <EMPTY> OptionalEntity<EMPTY> empty() {
        return (OptionalEntity<EMPTY>) EMPTY_INSTANCE;
    }

    /**
     * @param entity The wrapped entity for the optional object. (NotNull)
     * @return The new-created instance as existing optional object. (NotNull)
     */
    public static <ENTITY> OptionalEntity<ENTITY> of(ENTITY entity) {
        if (entity == null) {
            String msg = "The argument 'entity' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        return new OptionalEntity<ENTITY>(entity, NOWAY_THROWER);
    }

    /**
     * @param entity The wrapped instance or entity. (NullAllowed)
     * @param thrower The exception thrower when illegal access. (NotNull)
     * @return The new-created instance as existing or empty optional object. (NotNull)
     */
    public static <ENTITY> OptionalEntity<ENTITY> ofNullable(ENTITY entity, OptionalThingExceptionThrower thrower) {
        if (entity != null) {
            return of(entity);
        } else {
            return new OptionalEntity<ENTITY>(entity, thrower);
        }
    }

    /**
     * @param entity The base entity of the relation for exception message. (NotNull)
     * @param relation The property name of the relation for exception message. (NotNull)
     * @return The new-created instance as existing or empty optional object. (NotNull)
     */
    public static <EMPTY> OptionalEntity<EMPTY> relationEmpty(final Object entity, final String relation) {
        if (entity == null) {
            String msg = "The argument 'entity' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        if (relation == null) {
            String msg = "The argument 'relation' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        return new OptionalEntity<EMPTY>(null, new OptionalThingExceptionThrower() {
            public void throwNotFoundException() {
                throwNonSetupSelectRelationAccessException(entity, relation);
            }
        });
    }

    protected static void throwNonSetupSelectRelationAccessException(Object entity, String relation) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("NON-setupSelect relation was accessed.");
        br.addItem("Advice");
        br.addElement("Confirm your access to the relation.");
        br.addElement("Call setupSelect or fix your access.");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    MemberCB cb = new MemberCB()");
        br.addElement("    cb.setupSelect_MemberStatus();");
        br.addElement("    List<Member> memberList = memberBhv.selectList(cb);");
        br.addElement("    for (Member member : memberList) {");
        br.addElement("        ... = member.getMemberSecurityAsOne().required(...); // *NG");
        br.addElement("    }");
        br.addElement("  (o): (fix access mistake)");
        br.addElement("    MemberCB cb = new MemberCB()");
        br.addElement("    cb.setupSelect_MemberStatus();");
        br.addElement("    List<Member> memberList = memberBhv.selectList(cb);");
        br.addElement("    for (Member member : memberList) {");
        br.addElement("        ... = member.getMemberStatus().required(...); // OK");
        br.addElement("    }");
        br.addElement("  (o): (fix setupSelect mistake)");
        br.addElement("    MemberCB cb = new MemberCB()");
        br.addElement("    cb.setupSelect_MemberSecurityAsOne(); // OK");
        br.addElement("    List<Member> memberList = memberBhv.selectList(cb);");
        br.addElement("    for (Member member : memberList) {");
        br.addElement("        ... = member.getMemberSecurityAsOne().required(...);");
        br.addElement("    }");
        br.addItem("Your Relation");
        br.addElement(entity.getClass().getSimpleName() + "." + relation);
        final String msg = br.buildExceptionMessage();
        throw new NonSetupSelectRelationAccessException(msg);
    }

    // ===================================================================================
    //                                                                     Object Handling
    //                                                                     ===============
    /**
     * Get the entity or exception if null.
     * <pre>
     * MemberCB cb = new MemberCB();
     * cb.query().set...
     * OptionalEntity&lt;Member&gt; entity = memberBhv.selectEntity(cb);
     *
     * <span style="color: #3F7E5E">// if the data always exists as your business rule</span>
     * Member member = entity.<span style="color: #DD4747">get()</span>;
     *
     * <span style="color: #3F7E5E">// if it might be no data, isPresent(), orElse(), ...</span>
     * if (entity.<span style="color: #DD4747">isPresent()</span>) {
     *     Member member = entity.<span style="color: #DD4747">get()</span>;
     * } else {
     *     ...
     * }
     * </pre>
     * @return The entity instance wrapped in this optional object. (NotNull)
     * @exception EntityAlreadyDeletedException When the entity instance wrapped in this optional object is null, which means entity has already been deleted (point is not found).
     */
    public ENTITY get() {
        return directlyGet();
    }

    /**
     * Handle the wrapped entity if it is present. <br />
     * You should call this if null entity handling is unnecessary (do nothing if null). <br />
     * If exception is preferred when null entity, use required().
     * <pre>
     * MemberCB cb = new MemberCB();
     * cb.query().set...
     * OptionalEntity&lt;Member&gt; entity = memberBhv.selectEntity(cb);
     * entity.<span style="color: #CC4747">ifPresent</span>(member -&gt; {
     *     <span style="color: #3F7E5E">// called if value exists, not called if not present</span>
     *     ... = member.getMemberName();
     * }).<span style="color: #994747">orElse</span>(() -&gt; {
     *     <span style="color: #3F7E5E">// called if value does not exist</span>
     * });
     * </pre>
     * @param entityLambda The callback interface to consume the optional entity. (NotNull)
     * @return The handler of after process when if not present. (NotNull)
     */
    public OptionalThingIfPresentAfter ifPresent(OptionalThingConsumer<ENTITY> entityLambda) {
        assertEntityLambdaNotNull(entityLambda);
        return callbackIfPresent(entityLambda);
    }

    /**
     * Is the entity instance present? (existing?)
     * <pre>
     * MemberCB cb = new MemberCB();
     * cb.query().set...
     * OptionalEntity&lt;Member&gt; entity = memberBhv.selectEntity(cb);
     * if (entity.<span style="color: #DD4747">isPresent()</span>) { <span style="color: #3F7E5E">// true if the entity exists</span>
     *     Member member = entity.get();
     * } else {
     *     ...
     * }
     * </pre>
     * @return The determination, true or false.
     */
    public boolean isPresent() {
        return exists();
    }

    /**
     * Filter the entity by the predicate.
     * <pre>
     * MemberCB cb = new MemberCB();
     * cb.query().set...
     * OptionalEntity&lt;Member&gt; entity = memberBhv.selectEntity(cb);
     * OptionalEntity&lt;Member&gt; filtered = entity.<span style="color: #DD4747">filter</span>(member -&gt; {
     *     <span style="color: #3F7E5E">// called if value exists, not called if not present</span>
     *     return member.getMemberId() % 2 == 0;
     * });
     * </pre>
     * @param entityLambda The callback to predicate whether the entity is remained. (NotNull)
     * @return The filtered optional entity, might be empty. (NotNull)
     */
    public OptionalEntity<ENTITY> filter(OptionalThingPredicate<ENTITY> entityLambda) {
        assertEntityLambdaNotNull(entityLambda);
        return (OptionalEntity<ENTITY>) callbackFilter(entityLambda);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected <ARG> OptionalThing<ARG> createOptionalFilteredObject(ARG obj) {
        return new OptionalThing<ARG>(obj, _thrower);
    }

    /**
     * Apply the mapping of entity to result object.
     * <pre>
     * MemberCB cb = new MemberCB();
     * cb.query().set...
     * OptionalEntity&lt;Member&gt; entity = memberBhv.selectEntity(cb);
     * OptionalObject&lt;MemberWebBean&gt; bean = entity.<span style="color: #DD4747">map</span>(member -&gt; {
     *     <span style="color: #3F7E5E">// called if value exists, not called if not present</span>
     *     return new MemberWebBean(member);
     * });
     * </pre>
     * @param entityLambda The callback interface to apply. (NotNull)
     * @return The optional object as mapped result. (NotNull, EmptyOptionalAllowed: if not present or callback returns null)
     */
    @SuppressWarnings("unchecked")
    public <RESULT> OptionalThing<RESULT> map(OptionalThingFunction<? super ENTITY, ? extends RESULT> entityLambda) {
        assertEntityLambdaNotNull(entityLambda);
        return (OptionalThing<RESULT>) callbackMapping(entityLambda); // downcast allowed because factory is overridden
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected <ARG> OptionalThing<ARG> createOptionalMappedObject(ARG obj) {
        return new OptionalThing<ARG>(obj, _thrower);
    }

    // TODO jflute make Optional.flatMap()
    // almost no needed
    ///**
    // * Apply the flat-mapping of entity to result object.
    // * <pre>
    // * MemberCB cb = new MemberCB();
    // * cb.query().set...
    // * OptionalEntity&lt;Member&gt; entity = memberBhv.selectEntity(cb);
    // * OptionalObject&lt;MemberWebBean&gt; bean = entity.<span style="color: #DD4747">map</span>(member -&gt; {
    // *     <span style="color: #3F7E5E">// called if value exists, not called if not present</span>
    // *     if (member.getMemberId() % 2 == 0) {
    // *         return OptionalObject.of(new MemberWebBean(member));
    // *     } else {
    // *         return OptionalObject.empty();
    // *     }
    // * });
    // * </pre>
    // * @param mapper The callback interface to apply. (NotNull)
    // * @return The optional object as mapped result. (NotNull, EmptyOptionalAllowed: if not present or callback returns null)
    // */
    //public <RESULT> OptionalObject<RESULT> flatMap(OptionalObjectFunction<? super ENTITY, OptionalObject<RESULT>> mapper) {
    //    return callbackFlatMapping(mapper);
    //}

    // unsupported because of absolutely no needed, and making orElseNull() stand out
    //public ENTITY orElse(...) {
    //    return ...;
    //}
    //public ENTITY orElseGet(...) {
    //    return ...;
    //}
    // TODO jflute make Optional.orElseThrow(Supplier)

    // ===================================================================================
    //                                                                           Extension
    //                                                                           =========
    /**
     * Handle the entity in the optional object or exception if not present.
     * <pre>
     * memberBhv.selectEntity(cb -&gt; {
     *     cb.setupSelect_MemberStatus();
     *     cb.query().setMemberId_Equal(1);
     * }).<span style="color: #DD4747">alwaysPresent</span>(member -&gt; {
     *     <span style="color: #3F7E5E">// called if value exists, or exception if not present</span>
     *     ... = member.getMemberName();
     * });
     * </pre>
     * @param entityLambda The callback interface to consume the optional value. (NotNull)
     * @exception EntityAlreadyDeletedException When the entity instance wrapped in this optional object is null, which means entity has already been deleted (point is not found).
     */
    public void alwaysPresent(OptionalThingConsumer<ENTITY> entityLambda) {
        assertEntityLambdaNotNull(entityLambda);
        callbackAlwaysPresent(entityLambda);
    }

    /**
     * Get the entity instance or null if not present.
     * <pre>
     * MemberCB cb = new MemberCB();
     * cb.query().set...
     * OptionalEntity&lt;Member&gt; entity = memberBhv.selectEntity(cb);
     * Member member = entity.<span style="color: #DD4747">orElseNull()</span> <span style="color: #3F7E5E">// returns null if not present</span>
     * </pre>
     * @return The object instance wrapped in this optional object or null. (NullAllowed: if not present)
     */
    public ENTITY orElseNull() {
        return directlyGetOrElse(null);
    }

    /**
     * Handle the entity in the optional object or exception if not present.
     * <pre>
     * MemberCB cb = new MemberCB();
     * cb.query().set...
     * OptionalEntity&lt;Member&gt; entity = memberBhv.selectEntity(cb);
     * entity.<span style="color: #DD4747">required</span>(member -&gt; {
     *     <span style="color: #3F7E5E">// called if value exists, or exception if not present</span>
     *     ... = member.getMemberName();
     * });
     * </pre>
     * @param entityLambda The callback interface to consume the optional value. (NotNull)
     * @exception EntityAlreadyDeletedException When the entity instance wrapped in this optional object is null, which means entity has already been deleted (point is not found).
     * @deprecated use alwaysPresent()
     */
    public void required(OptionalThingConsumer<ENTITY> entityLambda) {
        callbackAlwaysPresent(entityLambda);
    }

    // ===================================================================================
    //                                                                       Assert Helper
    //                                                                       =============
    protected void assertEntityLambdaNotNull(Object entityLambda) {
        if (entityLambda == null) {
            throw new IllegalArgumentException("The argument 'entityLambda' should not be null.");
        }
    }
}
