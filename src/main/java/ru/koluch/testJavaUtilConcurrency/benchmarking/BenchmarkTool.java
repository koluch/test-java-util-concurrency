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
 * Created: 21.11.2015 03:00
 */
package ru.koluch.testJavaUtilConcurrency.benchmarking;


import com.google.common.collect.TreeBasedTable;
import com.google.gson.*;
import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.results.Result;
import org.openjdk.jmh.results.RunResult;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public class BenchmarkTool {


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
