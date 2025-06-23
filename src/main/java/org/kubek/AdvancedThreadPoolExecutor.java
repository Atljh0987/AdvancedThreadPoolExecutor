package org.kubek;

import java.util.concurrent.*;

public class AdvancedThreadPoolExecutor extends ThreadPoolExecutor {
    public AdvancedThreadPoolExecutor(int maxActiveThreads) {
        this(maxActiveThreads, 60, TimeUnit.SECONDS);
    }

    public AdvancedThreadPoolExecutor(int maxActiveThreads, int keepAliveTime, TimeUnit timeUnit) {
        super(0, maxActiveThreads, keepAliveTime, timeUnit, new advancedQueue<>());
        ((advancedQueue<?>)super.getQueue()).initQueue(this);
    }

    private static class advancedQueue<T> extends SynchronousQueue<T> {
        final private Semaphore blocker = new Semaphore(0);
        private ThreadPoolExecutor executor;

        private void initQueue(ThreadPoolExecutor executor) {
            this.executor = executor;
            this.blocker.release(executor.getMaximumPoolSize());
        }

        @Override
        public T take() throws InterruptedException {
            blocker.release();
            return super.take();
        }

        @Override
        public T poll() {
            blocker.release();
            return super.poll();
        }

        @Override
        public T poll(long timeout, TimeUnit unit) throws InterruptedException {
            blocker.release();
            return super.poll(timeout, unit);
        }

        @Override
        public boolean offer(T t) {
            try {
                blocker.acquire();

                return offerTimeout(t);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        private boolean offerTimeout(T t) throws InterruptedException {
            if(canAddWorker()) {
                return super.offer(t);
            }

            return super.offer(t, executor.getKeepAliveTime(TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);
        }

        private boolean canAddWorker() {
            return executor.getPoolSize() < executor.getMaximumPoolSize();
        }
    }
}
