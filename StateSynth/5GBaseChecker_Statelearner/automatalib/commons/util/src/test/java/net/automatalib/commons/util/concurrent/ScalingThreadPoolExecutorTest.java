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
package net.automatalib.commons.util.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;
import org.testng.internal.thread.ThreadTimeoutException;

/**
 * @author frohme
 */
public class ScalingThreadPoolExecutorTest {

    @Test(timeOut = 2000)
    public void testCachedThreadCreation() throws ExecutionException, InterruptedException {

        final int tasks = 10;
        final CountDownLatch latch = new CountDownLatch(tasks);

        final ScalingThreadPoolExecutor executor = new ScalingThreadPoolExecutor(0, tasks, 60L, TimeUnit.SECONDS);
        final List<Future<?>> futures = new ArrayList<>(tasks);

        for (int i = 0; i < tasks; i++) {
            futures.add(executor.submit(() -> {
                try {
                    latch.countDown();
                    latch.await();
                } catch (InterruptedException e) {
                    throw new IllegalStateException(e);
                }
            }));
        }

        // this code only returns, if 'tasks' threads are spawned, which all decrease the shared latch
        for (final Future<?> f : futures) {
            f.get();
        }

        executor.shutdown();
    }

    @Test(timeOut = 2000)
    public void testThreadSchedulingNormal() throws ExecutionException, InterruptedException {
        testThreadScheduling(2);
    }

    @Test(timeOut = 1000, expectedExceptions = ThreadTimeoutException.class)
    public void testCachedThreadSchedulingTimeout() throws ExecutionException, InterruptedException {
        testThreadScheduling(1);
    }

    private static void testThreadScheduling(int availableThreads) throws ExecutionException, InterruptedException {

        final int tasks = 10;
        final CountDownLatch latch = new CountDownLatch(tasks - 1);

        final ScalingThreadPoolExecutor executor =
                new ScalingThreadPoolExecutor(0, availableThreads, 60L, TimeUnit.SECONDS);
        final List<Future<?>> futures = new ArrayList<>(tasks);

        // submit a long-running task
        futures.add(executor.submit(() -> {
            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }));

        // submit remaining short-running tasks
        for (int i = 0; i < tasks - 1; i++) {
            futures.add(executor.submit(latch::countDown));
        }

        // this code only returns, if the short-running tasks were scheduled 9 times to unblock the long-running task
        for (final Future<?> f : futures) {
            f.get();
        }

        executor.shutdown();
    }

}
