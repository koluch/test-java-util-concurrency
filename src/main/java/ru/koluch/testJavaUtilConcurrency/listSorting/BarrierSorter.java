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


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;

public class BarrierSorter<T> implements ISorter<T>{

    private final ThreadPoolExecutor executorService;

    private SerialSorter<T> simpleSorter = new SerialSorter<>();


    public BarrierSorter(ThreadPoolExecutor executorService) {
        this.executorService = executorService;
    }

    private List<T> aux(int threshold, List<T> list, Comparator<T> comp) {
        if(list.size() > threshold) {
            int splitPoint = list.size() / 2;

            List<T> left = list.subList(0, splitPoint);
            List<T> right = list.subList(splitPoint, list.size());

            CountDownLatch barrier = new CountDownLatch(2);

            CompletableFuture<List<T>> leftSortedF = runAux(threshold, barrier, left, comp);
            CompletableFuture<List<T>> rightSortedF = runAux(threshold, barrier, right, comp);

            try {
                barrier.await();
            } catch (InterruptedException e) {
                throw new RuntimeException("Error while waiting for end in recursive routines", e);
            }


            List<T> leftSorted = leftSortedF.getNow(null);
            List<T> rightSorted = rightSortedF.getNow(null);

            ArrayList<T> result = new ArrayList<>();

            int i = 0;
            int j = 0;

            while( i < leftSorted.size() || j < rightSorted.size()) {
                if(i == leftSorted.size()) {
                    result.add(rightSorted.get(j));
                    j++;
                }
                else if(j == rightSorted.size()) {
                    result.add(leftSorted.get(i));
                    i++;
                }
                else {
                    T nextL = leftSorted.get(i);
                    T nextR = rightSorted.get(j);

                    if (comp.compare(nextL, nextR) < 0) {
                        result.add(nextL);
                        i++;
                    } else {
                        result.add(nextR);
                        j++;
                    }
                }
            }

            return result;

        }
        else {
            return simpleSorter.sort(list, comp);
        }
    }


    private CompletableFuture<List<T>> runAux(int threshold, CountDownLatch barrier, List<T> list, Comparator<T> comp) {
        CompletableFuture<List<T>> leftSortedF = new CompletableFuture<>();
        if(executorService.getActiveCount() == executorService.getMaximumPoolSize()) {
            leftSortedF.complete(aux(threshold, list, comp));
            barrier.countDown();
        }
        else {
            executorService.execute(() -> {
                leftSortedF.complete(aux(threshold, list, comp));
                barrier.countDown();
            });
        }
        return leftSortedF;
    }

    @Override
    public List<T> sort(List<T> list, Comparator<T> comparator) {
        int threshold = list.size() / executorService.getMaximumPoolSize();
        return aux(10, list, comparator);
    }

}
