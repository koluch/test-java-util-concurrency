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


import com.google.common.collect.TreeBasedTable;
import com.google.gson.*;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.Result;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import ru.koluch.testJavaUtilConcurrency.CountDownLatchMapper;
import ru.koluch.testJavaUtilConcurrency.ExecutorServiceMapper;
import ru.koluch.testJavaUtilConcurrency.SerialMapper;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    ExecutorService executor1;
    ExecutorService executor2;
    ExecutorService executor3;

    /*
        Init and stop functions
     */
    @Setup
    public void setup() {

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
        executor1 = Executors.newFixedThreadPool(4);
        executor2 = Executors.newFixedThreadPool(8);
        executor3 = Executors.newFixedThreadPool(16);
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
    public Set<?> testSerial() {
        return new SerialMapper().map(data, f);
    }

    @Benchmark
    public Set<?> testCountDownLatchImpl() {
        return new CountDownLatchMapper().map(data, f);
    }

    @Benchmark
    public Set<?> testExecutorServiceImpl1() {
        return new ExecutorServiceMapper(executor1).map(data, f);
    }

    @Benchmark
    public Set<?> testExecutorServiceImpl2() {
        return new ExecutorServiceMapper(executor2).map(data, f);
    }

    @Benchmark
    public Set<?> testExecutorServiceImpl3() {
        return new ExecutorServiceMapper(executor3).map(data, f);
    }

    /*
        Running and dumping results
     */
    public static void main(String[] args) throws Throwable {
        Options opt = new OptionsBuilder()
                .include(Benchmarks.class.getSimpleName())
                .warmupIterations(1)
                .measurementIterations(1)
                .forks(1)
                .build();

        dump(new Runner(opt).run());
    }

    public static void dump(Collection<RunResult> results) throws IOException {

        /*
         Build table by benchmark results
         */
        TreeBasedTable<Integer, String, Object> table = TreeBasedTable.create();
        int rowKey = 0;
        for (RunResult result : results) {
            Result primaryResult = result.getPrimaryResult();
            BenchmarkParams params = result.getParams();

            table.put(rowKey, "bench", primaryResult.getLabel());
            for (String key : params.getParamsKeys()) {
                table.put(rowKey, key.replaceAll("([A-Z])", "_$1").toLowerCase(), params.getParam(key));
            }
            table.put(rowKey, "mode", params.getMode());
            table.put(rowKey, "cnt", primaryResult.getSampleCount());
            table.put(rowKey, "score", primaryResult.getScore());
            table.put(rowKey, "error", primaryResult.getScoreError());
            table.put(rowKey, "units", primaryResult.getScoreUnit());
            rowKey++;
        }

        /*
            Dump data to JSON file
         */
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        Gson gson = gsonBuilder.create();
        JsonArray tableJson = new JsonArray();
        for (Map.Entry<Integer, Map<String, Object>> row : table.rowMap().entrySet()) {
            JsonObject rowJson = new JsonObject();
            for (Map.Entry<String, Object> keyValue : row.getValue().entrySet()) {
                rowJson.add(keyValue.getKey(), new JsonPrimitive(keyValue.getValue().toString()));
            }
            tableJson.add(rowJson);
        }
        String json = gson.toJson(tableJson);
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(new File("data.json")))) {
            writer.write(json);
        }
    }

}
