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
 * Created: 19.11.2015 01:19
 */
package ru.koluch.testJavaUtilConcurrency.listSorting;


import java.util.*;

public class SerialSorter<T> implements ISorter<T>{


    @Override
    public List<T> sort(List<T> list, Comparator<T> comparator) {
        ArrayList<T> result = new ArrayList<>(list);
        aux(result, 0, list.size(), comparator);
        return result;
    }

    private static <T> void aux(List<T> list, int start, int end, Comparator<T> comp) {
        if(end!=start) {
            int pivot = start;
            int i = start+1;
            int j = start+1;
            while(j<end) {
                if(comp.compare(list.get(pivot), list.get(j)) > 0) {
                    T tmp = list.get(i);
                    list.set(i, list.get(j));
                    list.set(j, tmp);
                    i++;
                }
                j++;
            }
            int newPivot = i - 1;
            T tmp = list.get(newPivot);
            list.set(newPivot, list.get(pivot));
            list.set(pivot, tmp);
            aux(list, start, newPivot, comp);
            aux(list, newPivot+1, end, comp);
        }
    }
}
