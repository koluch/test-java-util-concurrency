package ru.koluch.testJavaUtilConcurrency.setsMapping;

import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Nikolai_Mavrenkov on 03/06/15.
 */
@State(Scope.Thread)
public class ExecutorServiceMapper implements IMapper {


    private static ExecutorService executor;

    public ExecutorServiceMapper(ExecutorService executor) {
        this.executor = executor;
    }

    @Override
    public <T, R> Set<R> map(Set<T> set, Function<T, R> f) {

        List<Callable<R>> runnables = set.stream()
                .map((x) -> (Callable<R>) () -> f.apply(x))
                .collect(Collectors.toList());
        try {
            HashSet<R> result = new HashSet<>();
            List<Future<R>> futures = executor.invokeAll(runnables);
            for (Future<R> future : futures) {
                result.add(future.get());
            }
            return result;
        } catch (InterruptedException e) {
            throw new RuntimeException(e); //todo: handle properly
        } catch (ExecutionException e) {
            throw new RuntimeException(e); //todo: handle properly
        }
    }


}


