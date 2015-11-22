/**
 * --------------------------------------------------------------------
 * Copyright 2015 Nikolay Mavrenkov
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * --------------------------------------------------------------------
 * <p/>
 * Author:  Nikolay Mavrenkov <koluch@koluch.ru>
 * Created: 15.11.2015 18:08
 */
package ru.koluch.testJavaUtilConcurrency.benchmarking;


import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import ru.koluch.testJavaUtilConcurrency.setsMapping.CountDownLatchMapper;
import ru.koluch.testJavaUtilConcurrency.setsMapping.ExecutorServiceMapper;
import ru.koluch.testJavaUtilConcurrency.setsMapping.SerialMapper;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;

@State(Scope.Benchmark)
public class BenchmarkMapping {

    @Param({"1000","10000","100000"})
//    @Param({"10"})
    public int dataSize;

//        @Param({"1000","10000","100000"})
    @Param({"10"})
    public int busyFactor;

    @Param({"100","1000","10000","20000"})
    public int threshold;

    Function<Integer, Integer> f;
    Set<Integer> set;
    List<Integer> list;
    ExecutorService executor1;
    ExecutorService executor2;
    ExecutorService executor3;
    ForkJoinPool forkJoinPool;

    /*
        Init and stop functions
     */
    @Setup
    public void setup() {

        f = x -> {
            Blackhole.consumeCPU(1000 * busyFactor);
            return x * x;
        };
        set = new HashSet<>();
        {
            Random random = new Random();
            for (int i = 0; i < dataSize; i++) {
                set.add(random.nextInt(dataSize));
            }
        }
        list = new ArrayList<>();
        {
            Random random = new Random();
            for (int i = 0; i < dataSize; i++) {
                list.add(random.nextInt(dataSize));
            }
        }
        executor1 = Executors.newFixedThreadPool(4);
        executor2 = Executors.newFixedThreadPool(8);
        executor3 = Executors.newFixedThreadPool(16);
        forkJoinPool = new ForkJoinPool();

    }

    @TearDown
    public void tearDown() {
        executor1.shutdown();
        executor2.shutdown();
        executor3.shutdown();
    }


    /*
        Benchmarks
     */
    @Benchmark
    public Object testMapperSerial() {
        return new SerialMapper().map(set, f);
    }

    @Benchmark
    public Object testMapperCountDownLatch() {
        return new CountDownLatchMapper().map(set, f);
    }

    @Benchmark
    public Object testMapperExecutorService() {
        return new ExecutorServiceMapper(executor1).map(set, f);
    }

    @Benchmark
    public Object testMapperExecutorService2() {
        return new ExecutorServiceMapper(executor2).map(set, f);
    }

    @Benchmark
    public Object testMapperExecutorService3() {
        return new ExecutorServiceMapper(executor3).map(set, f);
    }

    /*
        Running and dumping results
     */
    public static void main(String[] args) throws Throwable {
        Options opt = new OptionsBuilder()
                .include(BenchmarkMapping.class.getSimpleName())
                .warmupIterations(3)
                .measurementIterations(3)
                .forks(1)
                .build();

        BenchmarkTool.dump(new Runner(opt).run());
    }

}
