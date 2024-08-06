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
package net.automatalib.util.graphs;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Predicate;

import net.automatalib.graphs.IndefiniteGraph;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Unweighted shortest path search in graphs.
 * <p>
 * This class offers an iterator-style approach to shortest path search: the methods in this class generally return
 * either an {@link Iterator} or an {@link Iterable} wrapped around an iterator which allows for enumerating all
 * shortest paths to the given set of target nodes. The iterators implement this lazily, i.e., a call to the {@link
 * Iterator#next() next()} method of an iterator will continue the shortest path search on an as-needed basis.
 *
 * @author Malte Isberner
 */
public final class ShortestPaths {

    private ShortestPaths() {
        // prevent instantiation
    }

    public static <N, E> Iterable<Path<N, E>> shortestPaths(IndefiniteGraph<N, E> graph, N start, int limit, N target) {
        return shortestPaths(graph, start, limit, (Predicate<? super N>) n -> Objects.equals(n, target));
    }

    public static <N, E> Iterable<Path<N, E>> shortestPaths(IndefiniteGraph<N, E> graph,
                                                            N start,
                                                            int limit,
                                                            Predicate<? super N> targetPred) {
        return shortestPaths(graph, Collections.singleton(start), limit, targetPred);
    }

    public static <N, E> Iterable<Path<N, E>> shortestPaths(final IndefiniteGraph<N, E> graph,
                                                            final Collection<? extends N> start,
                                                            final int limit,
                                                            final Predicate<? super N> targetPred) {
        Objects.requireNonNull(graph);
        Objects.requireNonNull(start);
        Objects.requireNonNull(targetPred);
        return () -> shortestPathsIterator(graph, start, limit, targetPred);
    }

    public static <N, E> Iterable<Path<N, E>> shortestPaths(IndefiniteGraph<N, E> graph,
                                                            N start,
                                                            int limit,
                                                            Collection<?> targets) {
        return shortestPaths(graph, start, limit, (Predicate<? super N>) targets::contains);
    }

    public static <N, E> Iterable<Path<N, E>> shortestPaths(IndefiniteGraph<N, E> graph,
                                                            Collection<? extends N> start,
                                                            int limit,
                                                            N target) {
        return shortestPaths(graph, start, limit, (Predicate<? super N>) n -> Objects.equals(n, target));
    }

    public static <N, E> Iterable<Path<N, E>> shortestPaths(IndefiniteGraph<N, E> graph,
                                                            Collection<? extends N> start,
                                                            int limit,
                                                            Collection<?> targets) {
        return shortestPaths(graph, start, limit, (Predicate<? super N>) targets::contains);
    }

    public static <N, E> @Nullable Path<N, E> shortestPath(IndefiniteGraph<N, E> graph,
                                                           N start,
                                                           int limit,
                                                           Collection<?> targets) {
        return shortestPath(graph, start, limit, (Predicate<? super N>) targets::contains);
    }

    public static <N, E> @Nullable Path<N, E> shortestPath(IndefiniteGraph<N, E> graph,
                                                           N start,
                                                           int limit,
                                                           Predicate<? super N> targetPred) {
        return shortestPath(graph, Collections.singleton(start), limit, targetPred);
    }

    public static <N, E> @Nullable Path<N, E> shortestPath(IndefiniteGraph<N, E> graph,
                                                           Collection<? extends N> start,
                                                           int limit,
                                                           Predicate<? super N> targetPred) {
        Iterator<Path<N, E>> spIt = shortestPathsIterator(graph, start, limit, targetPred);
        return spIt.hasNext() ? spIt.next() : null;
    }

    public static <N, E> @Nullable Path<N, E> shortestPath(IndefiniteGraph<N, E> graph, N start, int limit, N target) {
        return shortestPath(graph, start, limit, (Predicate<? super N>) n -> Objects.equals(n, target));
    }

    public static <N, E> @Nullable Path<N, E> shortestPath(IndefiniteGraph<N, E> graph,
                                                           Collection<? extends N> start,
                                                           int limit,
                                                           Collection<?> targets) {
        return shortestPath(graph, start, limit, (Predicate<? super N>) targets::contains);
    }

    public static <N, E> @Nullable Path<N, E> shortestPath(IndefiniteGraph<N, E> graph,
                                                           Collection<? extends N> start,
                                                           int limit,
                                                           N target) {
        return shortestPath(graph, start, limit, (Predicate<? super N>) n -> Objects.equals(n, target));
    }

    public static <N, E> Iterator<Path<N, E>> shortestPathsIterator(IndefiniteGraph<N, E> graph,
                                                                    Collection<? extends N> start,
                                                                    int limit,
                                                                    Predicate<? super N> targetPred) {
        return new FindShortestPathsIterator<>(graph, start, limit, targetPred);
    }

}
