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
 * Created: 16.11.2015 03:16
 */
package ru.koluch.testJavaUtilConcurrency;


import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class SerialMapper implements IMapper{

    @Override
    public <T,R> Set<R> map(Set<T> list, Function<T,R> f) {
        HashSet<R> result = new HashSet<>();
        for (T x : list) {
            result.add(f.apply(x));
        }
        return result;
    }

}
