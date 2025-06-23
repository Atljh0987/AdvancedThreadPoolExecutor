package org.kubek;

import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.

class Test {
    private Runnable message(int num) {
        return () -> {
            if(num == 3) {
                throw new RuntimeException();
            }

            try {
//                Thread.sleep(1000);

                Thread.sleep(ThreadLocalRandom.current().nextInt(100, 4001));
                System.out.printf("ProcessMessage: %d. Thread name: %s%n", num, Thread.currentThread().getName());
            } catch (InterruptedException e) {
                System.out.println("Exception");
            }
        };
    }

    private final ExecutorService cached = Executors.newCachedThreadPool();
    private final ExecutorService fixed = Executors.newFixedThreadPool(3);
    private final ExecutorService custom = custom();
    private final ExecutorService advanced = advanced();

    private ExecutorService custom() {
        return new ThreadPoolExecutor(3, 3,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(1));
    }

    private ExecutorService advanced() {
        return new AdvancedThreadPoolExecutor(10);
    }

    public void start() {
        var executor = advanced;

        List<Runnable> messages = IntStream.rangeClosed(1, 50)
                .mapToObj(this::message)
                .collect(Collectors.toList());

        for (int i = 0; i < 500; i++) {
            for (Runnable message : messages) {
                new Thread(() -> executor.submit(message)).start();
//                executor.submit(message);
            }

            printThreadPoolInfo(executor);

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

//        executor.shutdown();
    }

    private void printThreadPoolInfo(ExecutorService executorService) {
        var t = (ThreadPoolExecutor) executorService;

        System.out.printf(
                "Pool size: %d. Active threads: %d. Queue elements: %d. Queue remaining: %d\n",
                t.getPoolSize(), t.getActiveCount(), t.getQueue().size(), t.getQueue().remainingCapacity()
        );
    }
}

public class Main {
    public static void main(String[] args) {
        new Test().start();
    }
}