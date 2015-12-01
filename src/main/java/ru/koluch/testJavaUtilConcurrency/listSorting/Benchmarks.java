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
 * Created: 21.11.2015 02:56
 */
package ru.koluch.testJavaUtilConcurrency.listSorting;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import ru.koluch.testJavaUtilConcurrency.benchmarking.BenchmarkTool;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadPoolExecutor;

@State(Scope.Benchmark)
public class Benchmarks {



    @State(Scope.Benchmark)
    public static class SingleValueState {
        ForkJoinPool forkJoinPool;
        ThreadPoolExecutor threadPoolExecutor;

        @Setup
        public void setup() {
            forkJoinPool = new ForkJoinPool();
            threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(32);
        }

        @TearDown
        public void tearDown() {
            forkJoinPool.shutdownNow();
            threadPoolExecutor.shutdownNow();
        }


    }

    @State(Scope.Benchmark)
    public static class DataState {
        @Param({"1000","10000","100000"})
        public int dataSize;

        List<Integer> list;

        @Setup
        public void setup() {
            list = new ArrayList<>();
            {
                Random random = new Random();
                for (int i = 0; i < dataSize; i++) {
                    list.add(random.nextInt(dataSize));
                }
            }
        }
    }

    @State(Scope.Benchmark)
    public static class ThresholdState {
        @Param({"100","1000","10000","20000"})
        public int threshold;
    }


    @Benchmark
    public Object testSorterSerial(DataState dataState) {
        return new SerialSorter<Integer>().sort(dataState.list, Integer::compareTo);
    }

    @Benchmark
    public Object testSorterForkJoin(SingleValueState singleValueState, DataState dataState, ThresholdState thresholdState) {
        return new ForkJoinSorter<Integer>(singleValueState.forkJoinPool, thresholdState.threshold).sort(dataState.list, Integer::compareTo);
    }

    @Benchmark
    public Object testSorterBarrier(SingleValueState singleValueState, DataState dataState) {
        return new BarrierSorter<Integer>(singleValueState.threadPoolExecutor).sort(dataState.list, Integer::compareTo);
    }


    /*
        Running and dumping results
     */
    public static void main(String[] args) throws Throwable {
        Options opt = new OptionsBuilder()
                .include(Benchmarks.class.getCanonicalName())
                .warmupIterations(5)
                .measurementIterations(8)
                .forks(1)
                .build();

        BenchmarkTool.dump(new Runner(opt).run());
    }


}
