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
package ru.koluch.testJavaUtilConcurrency.benchmarking;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import ru.koluch.testJavaUtilConcurrency.listSorting.ForkJoinSorter;
import ru.koluch.testJavaUtilConcurrency.listSorting.SerialSorter;

import java.util.*;
import java.util.concurrent.ForkJoinPool;

@State(Scope.Benchmark)
public class BenchmarkSorting {
    @Param({"1000","10000","100000"})
//    @Param({"10"})
    public int dataSize;

    @Param({"100","1000","10000","20000"})
    public int threshold;

    List<Integer> list;
    ForkJoinPool forkJoinPool;

    /*
        Init and stop functions
     */
    @Setup
    public void setup() {
        list = new ArrayList<>();
        {
            Random random = new Random();
            for (int i = 0; i < dataSize; i++) {
                list.add(random.nextInt(dataSize));
            }
        }
        forkJoinPool = new ForkJoinPool();
    }


    @Benchmark
    public Object testSorterSerial() {
        return new SerialSorter<Integer>().sort(list, Integer::compareTo);
    }

    @Benchmark
    public Object testSorterForkJoin() {
        return new ForkJoinSorter<Integer>(forkJoinPool, threshold).sort(list, Integer::compareTo);
    }


    /*
        Running and dumping results
     */
    public static void main(String[] args) throws Throwable {
        Options opt = new OptionsBuilder()
                .include(BenchmarkSorting.class.getSimpleName())
                .warmupIterations(3)
                .measurementIterations(6)
                .forks(1)
                .build();

        BenchmarkTool.dump(new Runner(opt).run());
    }


}
