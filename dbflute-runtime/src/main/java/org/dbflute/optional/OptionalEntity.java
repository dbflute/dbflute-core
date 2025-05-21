/*
 * Copyright 2014-2025 the original author or authors.
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

import java.util.stream.Stream;

import org.dbflute.exception.EntityAlreadyDeletedException;
import org.dbflute.exception.NonSetupSelectRelationAccessException;
import org.dbflute.helper.function.IndependentProcessor;
import org.dbflute.helper.message.ExceptionMessageBuilder;

/**
 * The entity as optional object, which has entity instance in it. <br>
 * You can handle null value by this methods without direct null handling.
 * <pre>
 * <span style="color: #3F7E5E">// if the data always exists as your business rule</span>
 * <span style="color: #0000C0">memberBhv</span>.<span style="color: #994747">selectEntity</span>(<span style="color: #553000">cb</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
 *     <span style="color: #553000">cb</span>.setupSelect_MemberStatus();
 *     <span style="color: #553000">cb</span>.query().setMemberId_Equal(<span style="color: #2A00FF">1</span>);
 * }).<span style="color: #CC4747">alwaysPresent</span>(<span style="color: #553000">member</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
 *     <span style="color: #3F7E5E">// called if present, or exception</span>
 *     ... = <span style="color: #553000">member</span>.getMemberName();
 * });
 *
 * <span style="color: #3F7E5E">// if it might be no data, ...</span>
 * <span style="color: #0000C0">memberBhv</span>.<span style="color: #994747">selectEntity</span>(<span style="color: #553000">cb</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> <span style="color: #553000">cb</span>.acceptPK(<span style="color: #2A00FF">1</span>)).<span style="color: #CC4747">ifPresent</span>(<span style="color: #553000">member</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
 *     <span style="color: #3F7E5E">// called if present</span>
 *     ... = <span style="color: #553000">member</span>.getMemberName();
 * }).<span style="color: #994747">orElse</span>(() <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
 *     <span style="color: #3F7E5E">// called if not present</span>
 * });
 * </pre>
 * @param <ENTITY> The type of entity.
 * @author jflute
 * @since 1.0.5F (2014/05/05 Monday)
 */
public class OptionalEntity<ENTITY> extends BaseOptional<ENTITY> {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final long serialVersionUID = 1L; // basically for relation entity's optional

    protected static final OptionalEntity<Object> EMPTY_INSTANCE;
    static {
        EMPTY_INSTANCE = new OptionalEntity<Object>(null, new SerializableOptionalThingExceptionThrower() {

            private static final long serialVersionUID = 1L;

            public void throwNotFoundException() {
                String msg = "The empty optional so the value is null.";
                throw new EntityAlreadyDeletedException(msg);
            }
        });
    }
    protected static final OptionalThingExceptionThrower NOWAY_THROWER = new SerializableOptionalThingExceptionThrower() {

        private static final long serialVersionUID = 1L;

        @Override
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
     * @param <EMPTY> The type of empty optional entity.
     * @return The fixed instance as empty. (NotNull)
     */
    @SuppressWarnings("unchecked")
    public static <EMPTY> OptionalEntity<EMPTY> empty() {
        return (OptionalEntity<EMPTY>) EMPTY_INSTANCE;
    }

    /**
     * @param <ENTITY> The type of entity wrapped in the optional entity.
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
     * @param <ENTITY> The type of entity for the optional type.
     * @param entity The wrapped instance or entity. (NullAllowed)
     * @param noArgLambda The exception thrower when illegal access. (NotNull)
     * @return The new-created instance as existing or empty optional object. (NotNull)
     */
    public static <ENTITY> OptionalEntity<ENTITY> ofNullable(ENTITY entity, OptionalThingExceptionThrower noArgLambda) {
        if (entity != null) {
            return of(entity);
        } else {
            return new OptionalEntity<ENTITY>(entity, noArgLambda);
        }
    }

    // #giveup jflute want to rename relationalEmpty() but give up (2021/04/19)
    /**
     * @param <EMPTY> The type of empty optional entity.
     * @param entity The base entity of the relation for exception message. (NotNull)
     * @param relationship The property name of the relation for exception message. (NotNull)
     * @return The new-created instance as existing or empty optional object. (NotNull)
     */
    public static <EMPTY> OptionalEntity<EMPTY> relationEmpty(final Object entity, final String relationship) {
        if (entity == null) {
            String msg = "The argument 'entity' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        if (relationship == null) {
            String msg = "The argument 'relationship' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        return new OptionalEntity<EMPTY>(null, new OptionalThingExceptionThrower() {
            public void throwNotFoundException() {
                throwNonSetupSelectRelationAccessException(entity, relationship);
            }
        });
    }

    protected static void throwNonSetupSelectRelationAccessException(Object entity, String relationship) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Non-setupSelect relationship was accessed by your code.");
        br.addItem("Advice");
        br.addElement("Confirm your access to the relationship.");
        br.addElement("Call setupSelect or fix your access.");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    memberBhv.selectList(cb -> {");
        br.addElement("        cb.setupSelect_MemberStatus(); // status");
        br.addElement("    }).forEach(member -> {");
        br.addElement("        ... = member.getMemberSecurityAsOne().alwaysPresent(...); // *NG: security");
        br.addElement("    });");
        br.addElement("  (o): (fix access mistake)");
        br.addElement("    List<Member> memberList = memberBhv.selectList(cb -> {");
        br.addElement("        cb.setupSelect_MemberStatus();");
        br.addElement("    }).forEach(member -> {");
        br.addElement("        ... = member.getMemberStatus().alwaysPresent(...); // OK");
        br.addElement("    });");
        br.addElement("  (o): (fix setupSelect mistake)");
        br.addElement("    List<Member> memberList = memberBhv.selectList(cb -> {");
        br.addElement("        cb.setupSelect_MemberSecurityAsOne(); // OK");
        br.addElement("    }).forEach(member -> {");
        br.addElement("        ... = member.getMemberSecurityAsOne().alwaysPresent(...);");
        br.addElement("    });");
        br.addItem("Local Table");
        br.addElement(entity);
        br.addItem("Accessed Relationship");
        br.addElement(entity.getClass().getSimpleName() + "." + relationship);
        final String msg = br.buildExceptionMessage();
        throw new NonSetupSelectRelationAccessException(msg);
    }

    // ===================================================================================
    //                                                                   Standard Handling
    //                                                                   =================
    // -----------------------------------------------------
    //                                             ifPresent
    //                                             ---------
    /**
     * Handle the wrapped entity if it is present. <br>
     * You should call this if empty handling is unnecessary (do nothing if not present). <br>
     * If exception is preferred when null entity, use required().
     * <pre>
     * <span style="color: #0000C0">memberBhv</span>.<span style="color: #994747">selectEntity</span>(<span style="color: #553000">cb</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">cb</span>.setupSelect_MemberWithdrawal();
     *     <span style="color: #553000">cb</span>.query().setMemberId_Equal(<span style="color: #2A00FF">1</span>);
     * }).<span style="color: #CC4747">ifPresent</span>(<span style="color: #553000">member</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> { <span style="color: #3F7E5E">// called if value exists, or not called</span>
     *     ... = <span style="color: #553000">member</span>.getMemberName();
     *     <span style="color: #553000">member</span>.getMemberWithdrawal().<span style="color: #CC4747">ifPresent</span>(<span style="color: #553000">withdrawal</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> { <span style="color: #3F7E5E">// also relation</span>
     *         ... = <span style="color: #553000">withdrawal</span>.getWithdrawalDatetime();
     *     });
     * }).<span style="color: #994747">orElse</span>(() <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #3F7E5E">// called if no value</span>
     * });
     * </pre>
     * @param entityLambda The callback interface to consume the optional entity. (NotNull)
     * @return The handler of after process when if not present. (NotNull)
     */
    public OptionalThingIfPresentAfter ifPresent(OptionalThingConsumer<? super ENTITY> entityLambda) {
        assertEntityLambdaNotNull(entityLambda);
        return callbackIfPresent(entityLambda);
    }

    /**
     * Handle the wrapped thing if it is both present and empty. <br>
     * <span style="color: #AD4747; font-size: 120%">You have a choice, you can use this method or ifPresent().orElse().</span>
     * @param entityLambda The present-case callback interface to consume the optional thing. (NotNull)
     * @param noArgLambda The empty-case callback interface to handle or-else. (NotNull)
     */
    public void ifPresentOrElse(OptionalThingConsumer<? super ENTITY> entityLambda, IndependentProcessor noArgLambda) {
        assertEntityLambdaNotNull(entityLambda);
        assertNoArgLambdaNotNull(noArgLambda);
        callbackIfPresentOrElse(entityLambda, noArgLambda);
    }

    // -----------------------------------------------------
    //                                         isPresent/get
    //                                         -------------
    /**
     * Is the entity instance present? (existing?)
     * <pre>
     * OptionalEntity&lt;Member&gt; <span style="color: #553000">optMember</span> = <span style="color: #0000C0">memberBhv</span>.<span style="color: #994747">selectEntity</span>(cb <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     cb.query()...;
     * });
     * if (<span style="color: #553000">optMember</span>.<span style="color: #CC4747">isPresent()</span>) { <span style="color: #3F7E5E">// true if the entity exists</span>
     *     Member member = <span style="color: #553000">optMember</span>.get();
     * } else {
     *     ...
     * }
     * </pre>
     * @return The determination, true or false.
     */
    public boolean isPresent() {
        return determinePresent();
    }

    /**
     * Is the entity instance empty? (not existing?)
     * <pre>
     * OptionalEntity&lt;Member&gt; <span style="color: #553000">optMember</span> = <span style="color: #0000C0">memberBhv</span>.<span style="color: #994747">selectEntity</span>(cb <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     cb.query()...;
     * });
     * if (<span style="color: #553000">optMember</span>.<span style="color: #CC4747">isEmpty()</span>) { <span style="color: #3F7E5E">// true if the entity does not exist</span>
     *     ...
     * } else { // present here
     *     Member member = <span style="color: #553000">optMember</span>.get();
     * }
     * </pre>
     * @return The determination, true or false.
     */
    public boolean isEmpty() {
        return determineEmpty();
    }

    /**
     * Get the entity or (detail-message) exception if not present. <br>
     * <span style="color: #AD4747; font-size: 120%">However the alwaysPresent() or the orElseThrow() is recommended instead this.</span>
     * <pre>
     * OptionalEntity&lt;Member&gt; <span style="color: #553000">optMember</span> = <span style="color: #0000C0">memberBhv</span>.selectEntity(<span style="color: #553000">cb</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">cb</span>.setupSelect_MemberStatus();
     *     <span style="color: #553000">cb</span>.query().setMemberId_Equal(<span style="color: #2A00FF">1</span>);
     * });
     *
     * <span style="color: #3F7E5E">// if the data always exists as your business rule</span>
     * Member member = <span style="color: #553000">optMember</span>.<span style="color: #CC4747">get()</span>;
     *
     * <span style="color: #3F7E5E">// if it might be no data, isPresent(), orElse(), ...</span>
     * if (<span style="color: #553000">optMember</span>.<span style="color: #CC4747">isPresent()</span>) {
     *     Member member = <span style="color: #553000">optMember</span>.<span style="color: #CC4747">get()</span>;
     * } else {
     *     ...
     * }
     * </pre>
     * @return The entity instance wrapped in this optional object. (NotNull)
     * @throws EntityAlreadyDeletedException When the entity instance wrapped in this optional object is null, which means entity has already been deleted (point is not found).
     */
    public ENTITY get() {
        return directlyGet();
    }

    // -----------------------------------------------------
    //                                             or/orElse
    //                                             ---------
    /** {@inheritDoc} */
    public OptionalThing<ENTITY> or(OptionalThingSupplier<? extends OptionalThing<? extends ENTITY>> noArgLambda) {
        assertNoArgLambdaNotNull(noArgLambda);
        return callbackOr(noArgLambda);
    }

    /** {@inheritDoc} */
    public ENTITY orElse(ENTITY other) {
        return directlyGetOrElse(other);
    }

    /** {@inheritDoc} */
    public ENTITY orElseGet(OptionalThingSupplier<? extends ENTITY> noArgLambda) {
        assertNoArgLambdaNotNull(noArgLambda);
        return callbackGetOrElseGet(noArgLambda);
    }

    /**
     * Get the entity or (detail-message) exception if not present.
     * <pre>
     * OptionalEntity&lt;Member&gt; <span style="color: #553000">optMember</span> = <span style="color: #0000C0">memberBhv</span>.selectEntity(<span style="color: #553000">cb</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">cb</span>.setupSelect_MemberStatus();
     *     <span style="color: #553000">cb</span>.query().setMemberId_Equal(<span style="color: #2A00FF">1</span>);
     * });
     *
     * <span style="color: #3F7E5E">// if the data always exists as your business rule</span>
     * Member member = <span style="color: #553000">optMember</span>.<span style="color: #CC4747">orElseThrow()</span>;
     * </pre>
     * @return The entity instance wrapped in this optional object. (NotNull)
     * @throws EntityAlreadyDeletedException When the entity instance wrapped in this optional object is null, which means entity has already been deleted (point is not found).
     */
    public ENTITY orElseThrow() {
        return directlyGet();
    }

    /** {@inheritDoc} */
    public <CAUSE extends Throwable> ENTITY orElseThrow(OptionalThingSupplier<? extends CAUSE> noArgLambda) throws CAUSE {
        assertNoArgLambdaNotNull(noArgLambda);
        return callbackGetOrElseThrow(noArgLambda);
    }

    /** {@inheritDoc} */
    public <CAUSE extends Throwable, TRANSLATED extends Throwable> ENTITY orElseTranslatingThrow(
            OptionalThingFunction<CAUSE, TRANSLATED> causeLambda) throws TRANSLATED {
        assertCauseLambdaNotNull(causeLambda);
        return callbackGetOrElseTranslatingThrow(causeLambda);
    }

    // -----------------------------------------------------
    //                                            filter/map
    //                                            ----------
    /**
     * Filter the entity by the predicate.
     * <pre>
     * <span style="color: #0000C0">memberBhv</span>.<span style="color: #994747">selectEntity</span>(<span style="color: #553000">cb</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> <span style="color: #553000">cb</span>.acceptPK(<span style="color: #2A00FF">1</span>)).<span style="color: #CC4747">filter</span>(<span style="color: #553000">member</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #3F7E5E">// called if value exists, not called if not present</span>
     *     return <span style="color: #553000">member</span>.getMemberId() % 2 == 0;
     * }).<span style="color: #994747">ifPresent</span>(<span style="color: #553000">member</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     ...
     * });
     * </pre>
     * @param entityLambda The callback to predicate whether the entity is remained. (NotNull)
     * @return The filtered optional entity, might be empty. (NotNull)
     */
    public OptionalEntity<ENTITY> filter(OptionalThingPredicate<? super ENTITY> entityLambda) {
        assertEntityLambdaNotNull(entityLambda);
        return (OptionalEntity<ENTITY>) callbackFilter(entityLambda);
    }

    /** {@inheritDoc} */
    protected <ARG> OptionalEntity<ARG> createOptionalFilteredObject(ARG obj) {
        return new OptionalEntity<ARG>(obj, _thrower);
    }

    /**
     * Apply the mapping of entity to result object.
     * <pre>
     * <span style="color: #0000C0">memberBhv</span>.<span style="color: #994747">selectEntity</span>(<span style="color: #553000">cb</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> <span style="color: #553000">cb</span>.acceptPK(<span style="color: #2A00FF">1</span>)).<span style="color: #CC4747">map</span>(<span style="color: #553000">member</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #3F7E5E">// called if value exists, not called if not present</span>
     *     <span style="color: #70226C">return new</span> MemberWebBean(<span style="color: #553000">member</span>);
     * }).<span style="color: #994747">alwaysPresent</span>(<span style="color: #553000">member</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     ...
     * });
     * </pre>
     * @param <RESULT> The type of mapping result.
     * @param entityLambda The callback interface to apply, null return allowed as empty. (NotNull)
     * @return The optional thing as mapped result. (NotNull, EmptyOptionalAllowed: if not present or callback returns null)
     */
    @SuppressWarnings("unchecked")
    public <RESULT> OptionalEntity<RESULT> map(OptionalThingFunction<? super ENTITY, ? extends RESULT> entityLambda) {
        assertEntityLambdaNotNull(entityLambda);
        return (OptionalEntity<RESULT>) callbackMapping(entityLambda); // downcast allowed because factory is overridden
    }

    /** {@inheritDoc} */
    protected <ARG> OptionalEntity<ARG> createOptionalMappedObject(ARG obj) {
        return new OptionalEntity<ARG>(obj, _thrower);
    }

    /**
     * Apply the flat-mapping of entity to result object.
     * <pre>
     * <span style="color: #0000C0">memberBhv</span>.<span style="color: #994747">selectEntity</span>(<span style="color: #553000">cb</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> <span style="color: #553000">cb</span>.acceptPK(<span style="color: #2A00FF">1</span>)).<span style="color: #CC4747">flatMap</span>(<span style="color: #553000">member</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #3F7E5E">// called if value exists, not called if not present</span>
     *     <span style="color: #70226C">return</span> <span style="color: #553000">member</span>.getMemberWithdrawal();
     * }).<span style="color: #994747">ifPresent</span>(<span style="color: #553000">withdrawal</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     ...
     * });
     * </pre>
     * @param <RESULT> The type of mapping result.
     * @param entityLambda The callback interface to apply, cannot return null. (NotNull)
     * @return The optional thing as mapped result. (NotNull, EmptyOptionalAllowed: when not present)
     */
    public <RESULT> OptionalThing<RESULT> flatMap(OptionalThingFunction<? super ENTITY, OptionalThing<RESULT>> entityLambda) {
        assertEntityLambdaNotNull(entityLambda);
        return callbackFlatMapping(entityLambda);
    }

    /** {@inheritDoc} */
    protected <ARG> OptionalEntity<ARG> createOptionalFlatMappedObject(ARG obj) {
        return new OptionalEntity<ARG>(obj, _thrower);
    }

    // -----------------------------------------------------
    //                                                stream
    //                                                ------
    /** {@inheritDoc} */
    public Stream<ENTITY> stream() {
        return convertToStream();
    }

    // ===================================================================================
    //                                                                   DBFlute Extension
    //                                                                   =================
    /**
     * Handle the entity in the optional object or exception if not present.
     * <pre>
     * <span style="color: #0000C0">memberBhv</span>.<span style="color: #994747">selectEntity</span>(<span style="color: #553000">cb</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">cb</span>.setupSelect_MemberStatus();
     *     <span style="color: #553000">cb</span>.query().setMemberId_Equal(<span style="color: #2A00FF">1</span>);
     * }).<span style="color: #CC4747">alwaysPresent</span>(<span style="color: #553000">member</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> { <span style="color: #3F7E5E">// called if value exists, or exception</span>
     *     ... = <span style="color: #553000">member</span>.getMemberName();
     *     <span style="color: #553000">member</span>.getMemberStatus().<span style="color: #CC4747">alwaysPresent</span>(<span style="color: #553000">status</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> { <span style="color: #3F7E5E">// also relationship</span>
     *         ... = <span style="color: #553000">status</span>.getMemberStatusName();
     *     });
     * });
     * </pre>
     * @param entityLambda The callback interface to consume the optional value. (NotNull)
     * @throws EntityAlreadyDeletedException When the entity instance wrapped in this optional object is null, which means entity has already been deleted (point is not found).
     */
    public void alwaysPresent(OptionalThingConsumer<? super ENTITY> entityLambda) {
        assertEntityLambdaNotNull(entityLambda);
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
