package com.ashupre.whatsappparser.util;

import java.util.concurrent.CompletableFuture;

public final class TimerUtil {

    private TimerUtil() {
        throw new UnsupportedOperationException("Utility class, should not be initialized");
    }

    /**
     * Measures and logs time taken by async tasks that use CompletableFuture.
     * @param label - what label to give while printing
     * @param future the asynchronous task whose execution time is to be measured
     */
    public static <T> CompletableFuture<T> doOperationAndlogAsyncTime(String label, CompletableFuture<T> future) {
        long start = System.nanoTime();

        return future.whenComplete((result, throwable) -> {
            long end = System.nanoTime();
            System.out.println(label + " Took " + (end - start) / 1_000_000 + " ms");
        });
    }
}
