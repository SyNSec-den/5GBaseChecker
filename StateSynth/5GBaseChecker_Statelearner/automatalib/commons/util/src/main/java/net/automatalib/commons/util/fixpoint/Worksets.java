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
package net.automatalib.commons.util.fixpoint;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.automatalib.commons.util.Pair;

/**
 * @author msc
 */
public final class Worksets {

    private Worksets() {
        // prevent instantiation
    }

    public static <T, R> R process(WorksetAlgorithm<T, R> algorithm) {

        final int expectedElementCount = algorithm.expectedElementCount();
        final Deque<T> stack = new ArrayDeque<>(expectedElementCount);
        final Set<T> tracking = Sets.newHashSetWithExpectedSize(expectedElementCount);

        final Collection<T> initialElements = algorithm.initialize();
        stack.addAll(initialElements);
        tracking.addAll(initialElements);

        while (!stack.isEmpty()) {

            T current = stack.pop();
            tracking.remove(current);

            final Collection<T> discovered = algorithm.update(current);

            for (T element : discovered) {
                if (!tracking.contains(element)) {
                    tracking.add(element);
                    stack.addLast(element);
                }
            }

        }

        return algorithm.result();
    }

    public static <T, E, R> Pair<Map<T, E>, R> map(WorksetMappingAlgorithm<T, E, R> algorithm) {

        final Deque<T> stack = new ArrayDeque<>(algorithm.expectedElementCount());
        final Set<T> tracking = Sets.newHashSetWithExpectedSize(algorithm.expectedElementCount());
        final Map<T, E> mapping = Maps.newHashMapWithExpectedSize(algorithm.expectedElementCount());

        final Collection<T> initialElements = algorithm.initialize(mapping);
        stack.addAll(initialElements);
        tracking.addAll(initialElements);

        while (!stack.isEmpty()) {

            T currentT = stack.pop();
            tracking.remove(currentT);

            final Collection<T> discovered = algorithm.update(mapping, currentT);

            for (T element : discovered) {
                if (!tracking.contains(element)) {
                    tracking.add(element);
                    stack.addLast(element);
                }
            }
        }

        return Pair.of(mapping, algorithm.result());
    }

}
