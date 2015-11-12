package ru.koluch.testJavaUtilConcurrency;

import org.openjdk.jmh.annotations.Benchmark;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * Created by Nikolai_Mavrenkov on 03/06/15.
 */
@State(Scope.Thread)
public class CountDownLatchImpl {



    public <T,R> Set<R> map(Set<T> list, Function<T,R> f) {
        HashSet<R> result = new HashSet<>();
        for (T x : list) {
            result.add(f.apply(x));
        }
        return result;
    }

    public <T,R> Set<R> pmap(Set<T> set, Function<T,R> f) {
        AtomicReference<Throwable> ex = new AtomicReference<>();
        Thread.UncaughtExceptionHandler uncaughtExceptionHandler = (t, e) -> {
            ex.set(e);
        };
        Set<R> result = new ConcurrentSkipListSet<>();
        java.util.concurrent.CountDownLatch lock = new java.util.concurrent.CountDownLatch(set.size());
        for (T x : set) {
            Thread thread = new Thread(() -> {
                try {
                    result.add(f.apply(x));
                } finally {
                    lock.countDown();
                }
            });
            thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
            thread.start();
        }
        try {
            boolean reached = lock.await(30, TimeUnit.SECONDS);
            Throwable throwable = ex.get();
            if(throwable!=null) {
                throw new RuntimeException("Exception caught in some thread", throwable);
            }
            if(!reached) {
                throw new RuntimeException("Timeout while waiting for threads finish");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Error while await processing finish", e);
        }
        return result;
    }


    Function<Integer, Integer> f;
    Set<Integer> data;

    @Setup
    public void init() {
        f = x -> {
            Blackhole.consumeCPU(200000);
            return x * x;
        };
        data = new HashSet<>();
        {
            Random random = new Random();
            for (int i = 0; i < 100; i++) {
                data.add(random.nextInt());
            }
        }
    }


    @Benchmark
    public Set<?> testSerial() {
        return map(data, f);
    }

    @Benchmark
    public Set<?> testParallel() {
        return pmap(data, f);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(CountDownLatchImpl.class.getSimpleName())
                .warmupIterations(5)
                .measurementIterations(5)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}


