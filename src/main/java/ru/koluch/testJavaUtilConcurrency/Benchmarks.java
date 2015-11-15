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
package ru.koluch.testJavaUtilConcurrency;


import com.google.common.collect.TreeBasedTable;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.Result;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@State(Scope.Benchmark)
public class Benchmarks {

    @Param({"10","100","1000"})
//    @Param({"10"})
    public int dataSize;

    @Param({"10","100","1000"})
//    @Param({"10"})
    public int busyFactor;

    Function<Integer, Integer> f;
    Set<Integer> data;
    CountDownLatchImpl impl;

    @Setup
    public void init() {
        impl = new CountDownLatchImpl();

        f = x -> {
            Blackhole.consumeCPU(1000 * busyFactor);
            return x * x;
        };
        data = new HashSet<>();
        {
            Random random = new Random();
            for (int i = 0; i < dataSize; i++) {
                data.add(random.nextInt());
            }
        }
    }



    @Benchmark
    public Set<?> testSerial() {
        return impl.map(data, f);
    }

    @Benchmark
    public Set<?> testParallel() {
        return impl.pmap(data, f);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(Benchmarks.class.getSimpleName())
                .warmupIterations(1)
                .measurementIterations(1)
                .forks(1)
                .build();

        Collection<RunResult> results = new Runner(opt).run();
        print(results);
    }

    public static void print(Collection<RunResult> results) {

        TreeBasedTable<Integer, String, Object> table = TreeBasedTable.create();

        int rowKey = 0;

        for (RunResult result : results) {
            Result primaryResult = result.getPrimaryResult();
            BenchmarkParams params = result.getParams();

            table.put(rowKey, "Benchmark", primaryResult.getLabel());
            for (String key : params.getParamsKeys()) {
                table.put(rowKey, "(" + key + ")", params.getParam(key));
            }
            table.put(rowKey, "Mode", params.getMode());
            table.put(rowKey, "Cnt", primaryResult.getSampleCount());
            table.put(rowKey, "Score", primaryResult.getScore());
            table.put(rowKey, "Error", primaryResult.getScoreError());
            table.put(rowKey, "Units", primaryResult.getScoreUnit());

            rowKey++;
        }


        Map<String, List<SortedMap<String, Object>>> byBusyFactor = table.rowKeySet()
                .stream()
                .map(table::row)
                .collect(Collectors.groupingBy((row) -> (String) row.get("(busyFactor)")));

        ChartTool chartTool = new ChartTool();
        for (Map.Entry<String, List<SortedMap<String, Object>>> busyToRows : byBusyFactor.entrySet()) {

            Map<String, List<Double>> seriesList = new HashMap<>();

            String busyFactor = busyToRows.getKey();
            Map<String, List<SortedMap<String, Object>>> byBench = busyToRows
                                                                        .getValue()
                                                                        .stream()
                                                                        .collect(Collectors.groupingBy((row) -> (String) row.get("Benchmark")));
            for (Map.Entry<String, List<SortedMap<String, Object>>> benchToRows: byBench.entrySet()){
                String bench = benchToRows.getKey();
                List<SortedMap<String, Object>> rows = benchToRows.getValue();
                List<Double> series = rows.stream()
                                            .map((row) -> Double.valueOf(row.get("Score").toString()))
                                            .collect(Collectors.toList());
                seriesList.put(bench, series);
            }

            Set<String> dataSizes = busyToRows.getValue().stream().collect(Collectors.groupingBy((row) -> (String) row.get("(dataSize)"))).keySet();
            ArrayList<String> xLabels = new ArrayList<>(dataSizes);
            Collections.sort(xLabels, (x,y) -> Double.valueOf(x).compareTo(Double.valueOf(y)));
            System.out.println(busyFactor + ": " + chartTool.draw("busy factor: " + busyFactor, seriesList, xLabels));
        }


        /*
        Benchmark                                        (busyFactor)  (dataSize)   Mode  Cnt     Score     Error  Units
        testJavaUtilConcurrency.Benchmarks.testParallel            10          10  thrpt    5  1366.662 ± 646.087  ops/s
        testJavaUtilConcurrency.Benchmarks.testParallel            10         100  thrpt    5   146.484 ±  11.958  ops/s
        testJavaUtilConcurrency.Benchmarks.testParallel            10        1000  thrpt    5    14.685 ±   1.912  ops/s
        testJavaUtilConcurrency.Benchmarks.testParallel           100          10  thrpt    5   954.819 ±  85.052  ops/s
        testJavaUtilConcurrency.Benchmarks.testParallel           100         100  thrpt    5   108.589 ±   9.055  ops/s
        testJavaUtilConcurrency.Benchmarks.testParallel           100        1000  thrpt    5    10.930 ±   2.410  ops/s
        testJavaUtilConcurrency.Benchmarks.testParallel          1000          10  thrpt    5   186.438 ±   4.251  ops/s
        testJavaUtilConcurrency.Benchmarks.testParallel          1000         100  thrpt    5    26.278 ±   3.294  ops/s
        testJavaUtilConcurrency.Benchmarks.testParallel          1000        1000  thrpt    5     2.764 ±   0.431  ops/s
        testJavaUtilConcurrency.Benchmarks.testSerial              10          10  thrpt    5  4253.568 ± 369.363  ops/s
        testJavaUtilConcurrency.Benchmarks.testSerial              10         100  thrpt    5   422.466 ±  25.657  ops/s
        testJavaUtilConcurrency.Benchmarks.testSerial              10        1000  thrpt    5    41.618 ±   2.528  ops/s
        testJavaUtilConcurrency.Benchmarks.testSerial             100          10  thrpt    5   426.718 ±  31.018  ops/s
        testJavaUtilConcurrency.Benchmarks.testSerial             100         100  thrpt    5    41.889 ±   2.312  ops/s
        testJavaUtilConcurrency.Benchmarks.testSerial             100        1000  thrpt    5     4.185 ±   0.509  ops/s
        testJavaUtilConcurrency.Benchmarks.testSerial            1000          10  thrpt    5    41.789 ±   3.060  ops/s
        testJavaUtilConcurrency.Benchmarks.testSerial            1000         100  thrpt    5     4.192 ±   0.255  ops/s
        testJavaUtilConcurrency.Benchmarks.testSerial            1000        1000  thrpt    5     0.420 ±   0.036  ops/s



        Benchmark,(busyFactor),(dataSize),Mode,Cnt,Score,Error,Units,
        testParallel,10,10,Throughput,5,1366.6621267058251,646.0867167523671,ops/s,
        testParallel,10,100,Throughput,5,146.4842736874518,11.958394786886377,ops/s,
        testParallel,10,1000,Throughput,5,14.685110179535709,1.9118393345404197,ops/s,
        testParallel,100,10,Throughput,5,954.8187046972325,85.0522932287249,ops/s,
        testParallel,100,100,Throughput,5,108.58914321384427,9.05510527689327,ops/s,
        testParallel,100,1000,Throughput,5,10.92993242304136,2.4097372581006233,ops/s,
        testParallel,1000,10,Throughput,5,186.43806328391423,4.251094662073061,ops/s,
        testParallel,1000,100,Throughput,5,26.27773572276063,3.2937646168891095,ops/s,
        testParallel,1000,1000,Throughput,5,2.764127577535008,0.4311534552082873,ops/s,
        testSerial,10,10,Throughput,5,4253.567668853938,369.36328904691646,ops/s,
        testSerial,10,100,Throughput,5,422.4655570195423,25.657492231622275,ops/s,
        testSerial,10,1000,Throughput,5,41.618473321456705,2.5275716676587776,ops/s,
        testSerial,100,10,Throughput,5,426.71845467055346,31.01761340825451,ops/s,
        testSerial,100,100,Throughput,5,41.8891224323816,2.3116379218898797,ops/s,
        testSerial,100,1000,Throughput,5,4.184730813288839,0.5093059232359516,ops/s,
        testSerial,1000,10,Throughput,5,41.78852009915731,3.0595626370995768,ops/s,
        testSerial,1000,100,Throughput,5,4.191811158157812,0.2547552600398061,ops/s,
        testSerial,1000,1000,Throughput,5,0.4204416869596897,0.03635827915527775,ops/s,
         */
    }

}
