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
 * Created: 16.11.2015 00:15
 */
package ru.koluch.testJavaUtilConcurrency.benchmarking;


import com.google.common.collect.TreeBasedTable;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class BenchmarkDraw {

    public static void main(String[] args) throws IOException {

        TreeBasedTable<Integer, String, String> table = TreeBasedTable.create();
        try(BufferedReader reader = new BufferedReader(new FileReader("data.json"))) {
            String json = reader.lines().collect(Collectors.joining());
            Gson gson = new Gson();
            JsonArray fromJson = gson.fromJson(json, JsonArray.class);
            int row = 0;
            for (JsonElement jsonElement : fromJson) {
                JsonObject rowJson = jsonElement.getAsJsonObject();
                for (Map.Entry<String, JsonElement> keyValue : rowJson.entrySet()) {
                    table.put(row, keyValue.getKey(), keyValue.getValue().getAsString());
                }
                row++;
            }
        }

        /*
            Group table rows by busy factor and draw chart for each
         */
        String group1Column = "(dataSize)";
        String group2Column = "(busyFactor)";

        Map<String, List<SortedMap<String, String>>> byBusyFactor = table.rowKeySet()
                .stream()
                .map(table::row)
                .collect(Collectors.groupingBy((row) -> row.get(group1Column)));

        ChartTool chartTool = new ChartTool();
        for (Map.Entry<String, List<SortedMap<String, String>>> group1ToRows : byBusyFactor.entrySet()) {

            Map<String, List<Double>> seriesList = new HashMap<>();

            String group1Category = group1ToRows.getKey();
            Map<String, List<SortedMap<String, String>>> byBench = group1ToRows
                    .getValue()
                    .stream()
                    .collect(Collectors.groupingBy((row) -> (String) row.get("bench")));
            for (Map.Entry<String, List<SortedMap<String, String>>> benchToRows: byBench.entrySet()){
                String bench = benchToRows.getKey();
                List<SortedMap<String, String>> rows = benchToRows.getValue();
                List<Double> series = rows.stream()
                        .map((row) -> Double.valueOf(row.get("score")))
                        .collect(Collectors.toList());
                seriesList.put(bench, series);
            }

            Set<String> dataSizes = group1ToRows.getValue().stream().collect(Collectors.groupingBy((row) -> row.get(group2Column))).keySet();
            ArrayList<String> xLabels = new ArrayList<>(dataSizes);
            Collections.sort(xLabels, (x, y) -> Double.valueOf(x).compareTo(Double.valueOf(y)));
            System.out.println(group1Category + ": " + chartTool.draw(group2Column + ": " + group1Category, seriesList, xLabels));
        }
    }


}
