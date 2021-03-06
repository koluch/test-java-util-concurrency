package ru.koluch.testJavaUtilConcurrency.setsMapping;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

/**
 * Created by Nikolai_Mavrenkov on 03/06/15.
 */
@State(Scope.Thread)
public class CountDownLatchMapper implements IMapper {

    @Override
    public <T,R> Set<R> map(Set<T> set, Function<T, R> f) {
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



}


