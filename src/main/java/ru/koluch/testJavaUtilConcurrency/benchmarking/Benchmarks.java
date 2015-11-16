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
import ru.koluch.testJavaUtilConcurrency.CountDownLatchImpl;
import ru.koluch.testJavaUtilConcurrency.SerialImpl;

import java.io.*;
import java.util.*;
import java.util.function.Function;

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
        return new SerialImpl().map(data, f);
    }

    @Benchmark
    public Set<?> testCountDownLatchImpl() {
        return new CountDownLatchImpl().map(data, f);
    }

    public static void main(String[] args) throws Throwable {
        Options opt = new OptionsBuilder()
                .include(Benchmarks.class.getSimpleName())
                .warmupIterations(5)
                .measurementIterations(5)
                .forks(1)
                .build();

        Collection<RunResult> results = new Runner(opt).run();
        print(results);
    }

    public static void print(Collection<RunResult> results) throws IOException {

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
        Gson gson = new Gson();
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
