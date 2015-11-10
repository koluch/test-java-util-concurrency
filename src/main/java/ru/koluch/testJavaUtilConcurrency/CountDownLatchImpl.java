package ru.koluch.testJavaUtilConcurrency;

import org.openjdk.jmh.annotations.Benchmark;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Function;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * Created by Nikolai_Mavrenkov on 03/06/15.
 */
public class CountDownLatchImpl {

    static final Function<Integer, Integer> f = x -> x * x;

    static final List<Integer> data = new ArrayList<>();
    static {
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            data.add(random.nextInt());
        }
    }
    
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(CountDownLatchImpl.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    @Benchmark
    public void testSerial() {
        map(new HashSet<>(data), f);

    }

    @Benchmark
    public void testParallel() {
        pmap(new HashSet<>(data), f);
    }


    public <T,R> Set<R> map(Set<T> list, Function<T,R> f) {
        HashSet<R> result = new HashSet<>();
        for (T x : list) {
            result.add(f.apply(x));
        }
        return result;
    }

    public <T,R> Set<R> pmap(Set<T> set, Function<T,R> f) {
        Set<R> result = new ConcurrentSkipListSet<>();
        java.util.concurrent.CountDownLatch lock = new java.util.concurrent.CountDownLatch(set.size());
        for (T x : set) {
            Thread thread = new Thread(() -> {
                result.add(f.apply(x));
                lock.countDown();
            });
            thread.start();
        }
        try {
            lock.await();
        } catch (InterruptedException e) {
            throw new RuntimeException("Error while await processing finish", e);
        }
        return result;
    }
}


