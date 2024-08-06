/* Copyright (C) 2013-2022 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.automatalib.commons.util.mappings;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Collection of various methods dealing with {@link Mapping}s.
 *
 * @author Malte Isberner
 */
public final class Mappings {

    private static final Mapping<?, ?> NULL_MAPPING = (Mapping<Object, @Nullable Object>) elem -> null;

    private static final Mapping<?, ?> IDENTITY_MAPPING = (Mapping<Object, Object>) elem -> elem;

    private static final Mapping<?, String> TOSTRING_MAPPING = (Mapping<Object, String>) String::valueOf;

    private Mappings() {
        // prevent instantiation
    }

    /**
     * Retrieves the <code>null</code> mapping, which maps each domain value to <code>null</code>.
     *
     * @param <D>
     *         domain class.
     * @param <R>
     *         range class.
     *
     * @return the <code>null</code> mapping.
     */
    @SuppressWarnings("unchecked")
    public static <D, R> Mapping<D, R> nullMapping() {
        return (Mapping<D, R>) NULL_MAPPING;
    }

    /**
     * Retrieves the identity mapping, which maps each domain value to itself.
     *
     * @param <T>
     *         domain/range class.
     *
     * @return the identity mapping.
     */
    @SuppressWarnings("unchecked")
    public static <T> Mapping<T, T> identity() {
        return (Mapping<T, T>) IDENTITY_MAPPING;
    }

    /**
     * Returns a mapping that maps objects to their {@link String} representation, as obtained by {@link
     * String#valueOf(Object)}.
     *
     * @return the "{@code toString()}" mapping
     */
    @SuppressWarnings("unchecked")
    public static <D> Mapping<D, String> toStringMapping() {
        return (Mapping<D, String>) TOSTRING_MAPPING;
    }

    /**
     * Returns a mapping that maps objects to a supertype representation.
     *
     * @return the "upcast" mapping
     */
    @SuppressWarnings("unchecked")
    public static <S, T extends S> Mapping<T, S> upcast() {
        return (Mapping<T, S>) IDENTITY_MAPPING;
    }

    /**
     * Retrieves the composition of two mappings, i.e., that mapping that results from applying the {@link
     * Mapping#get(Object)} method consecutively.
     *
     * @param <D>
     *         domain class of the first (and resulting) mapping.
     * @param <I>
     *         intermediate object class, range class of the first and domain class of the second mapping.
     * @param <R>
     *         range class of the second (and resulting) mapping.
     * @param first
     *         first mapping.
     * @param second
     *         second mapping.
     *
     * @return the composed mapping.
     */
    public static <D, I, R> Mapping<D, R> compose(Mapping<D, ? extends I> first, Mapping<? super I, R> second) {
        return new MappingComposition<D, I, R>(first, second);
    }

    /**
     * Applies a mapping to a collection, resulting in a collection containing the result of applying the specified
     * mapping to each element in the collection.
     * <p>
     * Note that more specific properties of the specified collection won't be preserved: If the given collection is
     * e.g. a set, and the provided mapping is not bijective, then the resulting collections may contain some values
     * multiple times.
     *
     * @param <D>
     *         domain class.
     * @param <R>
     *         range class.
     * @param mapping
     *         the mapping to apply.
     * @param coll
     *         the collection.
     *
     * @return the mapped collection.
     */
    public static <D, R> Collection<R> apply(final Mapping<? super D, R> mapping, final Collection<? extends D> coll) {
        return Collections2.transform(coll, mapping::get);
    }

    /**
     * Applies a mapping to an iterator. For the behavior, see {@link #apply(Mapping, Iterable)}. The resulting iterator
     * supports each operation which the underlying supports.
     *
     * @param <D>
     *         domain class.
     * @param <R>
     *         range class.
     * @param mapping
     *         the mapping to apply.
     * @param baseIt
     *         the underlying iterator.
     *
     * @return the mapped iterator.
     */
    public static <D, R> Iterator<R> apply(Mapping<? super D, R> mapping, Iterator<? extends D> baseIt) {
        return Iterators.transform(baseIt, mapping::get);
    }

    /**
     * Applies a mapping to a list, resulting in a list containing the result of applying the specified mapping to each
     * element in the list.
     *
     * @param mapping
     *         the mapping to apply.
     * @param list
     *         the list.
     *
     * @return the mapped list.
     */
    public static <D, R> List<R> apply(final Mapping<? super D, R> mapping, final List<? extends D> list) {
        return Lists.transform(list, mapping::get);
    }

    /**
     * Applies a mapping to an iterable. The result is an iterable whose iterator returns the results of applying the
     * specified mapping to each of the elements returned by the original iterable.
     *
     * @param <D>
     *         domain class.
     * @param <R>
     *         range clas.
     * @param mapping
     *         the mapping to apply.
     * @param it
     *         the underlying iterable.
     *
     * @return the mapped iterable.
     */
    public static <D, R> Iterable<R> apply(final Mapping<? super D, R> mapping, final Iterable<? extends D> it) {
        return Iterables.transform(it, mapping::get);
    }

    public static <D> D idGet(Mapping<D, D> mapping, D key) {
        return safeGet(mapping, key, key);
    }

    /**
     * Safely retrieves a value from a mapping. If the mapping is <code>null</code> or returns a <code>null</code>
     * value, the given fallback value is returned.
     *
     * @param mapping
     *         the mapping.
     * @param key
     *         the key.
     * @param fallback
     *         the fallback value to return if either the mapping or the originally returned value are
     *         <code>null</code>.
     *
     * @return the value returned by the specified mapping, or the fallback value.
     */
    public static <D, R> R safeGet(@Nullable Mapping<? super D, ? extends R> mapping, D key, R fallback) {
        if (mapping == null) {
            return fallback;
        }
        R val = mapping.get(key);
        if (val == null) {
            return fallback;
        }
        return val;
    }

    public static <D, R> @Nullable R nullGet(@Nullable Mapping<? super D, ? extends R> mapping, D key) {
        return safeGet(mapping, key, null);
    }

    public static <D, R> Mapping<D, R> fromMap(Map<D, R> map) {
        return new MapMapping<>(map);
    }

    public static Mapping<String, Long> stringToIndex() {
        return StringIndexMapping::stringToIndex;
    }

    public static Mapping<@NonNegative Long, String> indexToString() {
        return StringIndexMapping::indexToString;
    }
}
