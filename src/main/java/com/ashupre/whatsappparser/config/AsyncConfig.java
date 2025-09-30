package com.ashupre.whatsappparser.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Changes how @Async methods run, by using virtual threads for those methods.
     *
     * We are configuring Async tasks to use virtual thread per task executor. It is not like a thread pool where there
     * are fixed number of os threads. Here for every task, one new virtual thread (jvm managed) is created. So there
     * is not thread reuse. But these virtual threads are very lightweight and as they are managed by JVM instead of
     * OS, context switch is much more efficient (no syscalls, no kernel involvement, no storing and loading registers
     * etc.. ).
     */
    @Bean
    public Executor taskExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
