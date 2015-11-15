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
 * Created: 15.11.2015 19:38
 */
package ru.koluch.testJavaUtilConcurrency;


import static com.googlecode.charts4j.Color.*;
import static org.junit.Assert.assertEquals;

import java.util.*;

import com.googlecode.charts4j.*;

public class ChartTool {


    public static void main(String[] args) {

        ChartTool chartTool = new ChartTool();
        HashMap<String, List<Double>> data = new HashMap<>();
        data.put("line 1", Arrays.asList(1.0, 257.0, 30.0, 1114.0));
        data.put("line 2", Arrays.asList(1.0, 2.0, 4.0, 8.0));
        String url = chartTool.draw("test", data, Arrays.asList("first", "second", "third", "fourth"));
        System.out.println(url);
    }

    public String draw(String title, Map<String, List<Double>> data, List<String> xLabels) {

        /*
             Scale values
         */
        Double maxValue = Double.MIN_VALUE;
        Double minValue = Double.MAX_VALUE;
        for (List<Double> doubles : data.values()) {
            for (Double d:doubles){
                maxValue = Math.max(d, maxValue);
                minValue = Math.min(d, minValue);
            }
        }
        double k = 100.0 / maxValue;
        HashMap<String, List<Double>> scaled = new HashMap<>();
        for (Map.Entry<String, List<Double>> entry : data.entrySet()) {
            List<Double> value = new ArrayList<>();
            for (Double next : entry.getValue()) {
                value.add(next * k);
            }
            scaled.put(entry.getKey(), value);
        }
        data = scaled;


        Random rnd = new Random(42);

        // Defining lines
        ArrayList<Line> lines = new ArrayList<>();
        for (Map.Entry<String, List<Double>> entry : data.entrySet()) {
            Color color = Color.newColor(String.format("%02X%02X%02X", rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256)));
            Line line = Plots.newLine(Data.newData(entry.getValue()), color, entry.getKey());
            line.setLineStyle(LineStyle.newLineStyle(3, 1, 0));
            line.addShapeMarkers(Shape.DIAMOND, color, 12);
            line.addShapeMarkers(Shape.DIAMOND, Color.WHITE, 8);
            lines.add(line);
        }


        // Defining chart.
        LineChart chart = GCharts.newLineChart(lines);
        chart.setSize(600, 450);
        chart.setTitle(title, WHITE, 14);
        int stepCount = 5;
        chart.setGrid(100 / stepCount, 100 / stepCount, 3, 3);

        // Defining axis info and styles

        // Make x axis
        AxisStyle axisStyle = AxisStyle.newAxisStyle(WHITE, 12, AxisTextAlignment.CENTER);
        AxisLabels xAxis = AxisLabelsFactory.newAxisLabels(xLabels);
        xAxis.setAxisStyle(axisStyle);

        // Calculate y axis labels
        List<String> yLabels = new ArrayList<>();
        for (int i = 0; i <= stepCount; i++) {
            yLabels.add(String.valueOf(maxValue / stepCount * i));
        }
        AxisLabels yAxis = AxisLabelsFactory.newAxisLabels(yLabels);
        yAxis.setAxisStyle(axisStyle);

        // Adding axis info to chart.
        chart.addXAxisLabels(xAxis);
        chart.addYAxisLabels(yAxis);

        // Defining background and chart fills.
        chart.setBackgroundFill(Fills.newSolidFill(Color.newColor("1F1D1D")));
        LinearGradientFill fill = Fills.newLinearGradientFill(0, Color.newColor("363433"), 100);
        fill.addColorAndOffset(Color.newColor("2E2B2A"), 0);
        chart.setAreaFill(fill);

        return chart.toURLString();
    }



}

