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
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

public class ForkJoinSorter<T> implements ISorter<T>{


    ForkJoinPool forkJoinPool;
    private final int threshold;

    public ForkJoinSorter(ForkJoinPool forkJoinPool, int threshold) {
        this.forkJoinPool = forkJoinPool;
        this.threshold = threshold;
    }

    private static final class SortingTask<T> extends RecursiveTask<List<T>> {

        private final List<T> list;
        private final Comparator<T> comp;
        private final ISorter<T> simpleSorter;
        private final int threshold;

        public SortingTask(List<T> list, Comparator<T> comp, ISorter<T> simpleSorter, int threshold) {
            this.list = list;
            this.comp = comp;
            this.simpleSorter = simpleSorter;
            this.threshold = threshold;
        }


        @Override
        protected List<T> compute() {
            if(list.size() > threshold) {
                int splitPoint = list.size() / 2;

                Iterator<T> iterator = list.iterator();

                int part = list.size() / 8;




                List<T> left = list.subList(0, splitPoint);
                List<T> right = list.subList(splitPoint, list.size());

                SortingTask<T> leftTask = new SortingTask<T>(left, comp, simpleSorter, threshold);
                SortingTask<T> rightTask = new SortingTask<T>(right, comp, simpleSorter, threshold);

                ForkJoinTask<List<T>> leftFork = leftTask.fork();
                ForkJoinTask<List<T>> rightFork = rightTask.fork();

                List<T> leftSorted = leftFork.join();
                List<T> rightSorted = rightFork.join();

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
    }


    @Override
    public List<T> sort(List<T> list, Comparator<T> comparator) {
        SerialSorter<T> objectSerialSorter = new SerialSorter<>();
        return forkJoinPool.invoke(new SortingTask<>(list, comparator, objectSerialSorter, threshold));
    }



}
