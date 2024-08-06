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

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A {@link ThreadPoolExecutor} that internally uses a {@link ScalingLinkedBlockingQueue} to manage scheduled tasks.
 * This allows us to manage a dynamically sized thread pool that actually spawns new threads when the pool still allows
 * for it.
 * <p>
 * Additionally, this implementation keeps track of the current number of active threads by using an
 * {@link AtomicInteger} counter, rather than querying its list of worker threads.
 * <p>
 * For further information, see
 * <a href="https://github.com/kimchy/kimchy.github.com/blob/master/_posts/2008-11-23-juc-executorservice-gotcha.textile">
 * this blog post</a>.
 *
 * @author frohme
 */
public final class ScalingThreadPoolExecutor extends ThreadPoolExecutor {

    private final AtomicInteger activeCount = new AtomicInteger();

    public ScalingThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, new ScalingLinkedBlockingQueue());

        ((ScalingLinkedBlockingQueue) getQueue()).setTpe(this);
        setRejectedExecutionHandler(new ForceEnqueuingHandler());
    }

    @Override
    public int getActiveCount() {
        return activeCount.get();
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        activeCount.incrementAndGet();
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        activeCount.decrementAndGet();
    }

    /**
     * A modified {@link LinkedBlockingQueue} that rejects offering new elements if its associated
     * {@link ScalingThreadPoolExecutor} still has threads to spare. This means we will force a creation of a new thread
     * whenever there is room for parallel processing.
     * <p>
     * To prevent {@link RejectedExecutionException}s when reaching the limit of the thread pool, this class is only
     * useful in combination with a {@link ForceEnqueuingHandler} that forcefully enqueues the scheduled task to this
     * (otherwise unbounded) queue.
     *
     * @author frohme
     */
    private static class ScalingLinkedBlockingQueue extends LinkedBlockingQueue<Runnable> {

        private ThreadPoolExecutor tpe;

        void setTpe(ThreadPoolExecutor tpe) {
            this.tpe = tpe;
        }

        @Override
        public boolean offer(Runnable r) {
            if (tpe.getActiveCount() + size() < tpe.getMaximumPoolSize()) {
                return false;
            }

            return super.offer(r);
        }
    }

    /**
     * A {@link RejectedExecutionHandler} that forces the enqueuing of rejected tasks to the queue of a
     * {@link ThreadPoolExecutor}. Mainly useful in combination with {@link ScalingLinkedBlockingQueue}.
     *
     * @author frohme
     */
    static class ForceEnqueuingHandler implements RejectedExecutionHandler {

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            try {
                executor.getQueue().put(r);
            } catch (InterruptedException e) {
                throw new RejectedExecutionException(e);
            }
        }
    }

}