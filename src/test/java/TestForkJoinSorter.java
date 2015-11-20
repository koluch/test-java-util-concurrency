import org.junit.Before;
import org.junit.Test;
import ru.koluch.testJavaUtilConcurrency.listSorting.ForkJoinSorter;
import ru.koluch.testJavaUtilConcurrency.listSorting.SerialSorter;

import java.util.*;
import java.util.concurrent.ForkJoinPool;

import static org.junit.Assert.assertEquals;

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
 * Created: 19.11.2015 01:58
 */



public class TestForkJoinSorter {

    ForkJoinSorter<Integer> sorter;
    Random random;

    @Before
    public void init() {
        sorter = new ForkJoinSorter<>(new ForkJoinPool());
        random = new Random(42);
    }

    @Test
    public void simpleTest() {
        List<Integer> data = new ArrayList<>(Arrays.asList(7, 2, 6, 9, 1, 5, 3, 8, 0, 4));

        List<Integer> sorted = sorter.sort(data, Integer::compareTo);

        ArrayList<Integer> expected = new ArrayList<>(data);
        Collections.sort(expected);

        assertEquals(expected, sorted);
    }

    @Test
    public void randomTest() {
        List<Integer> data = new ArrayList<>();
        for (int i = 0; i < 100000; i++) {
            data.add(random.nextInt(100000));
        }

        List<Integer> sorted = sorter.sort(data, Integer::compareTo);

        ArrayList<Integer> expected = new ArrayList<>(data);
        Collections.sort(expected);

        assertEquals(expected, sorted);
    }

    @Test
    public void emptyTest() {
        List<Integer> data = new ArrayList<>();

        List<Integer> sorted = sorter.sort(data, Integer::compareTo);

        ArrayList<Integer> expected = new ArrayList<>(data);
        Collections.sort(expected);

        assertEquals(expected, sorted);
    }

    @Test
    public void singletonTest() {
        List<Integer> data = new ArrayList<>();
        data.add(random.nextInt());

        List<Integer> sorted = sorter.sort(data, Integer::compareTo);

        ArrayList<Integer> expected = new ArrayList<>(data);
        Collections.sort(expected);

        assertEquals(expected, sorted);
    }

}
