import org.junit.Test;
import org.openjdk.jmh.infra.Blackhole;
import ru.koluch.testJavaUtilConcurrency.ExecutorServiceMapper;
import ru.koluch.testJavaUtilConcurrency.SerialMapper;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import static junit.framework.Assert.assertEquals;

/**
 * Created by Nikolai_Mavrenkov on 10/11/15.
 */
public class TestExecutorServiceImpl {

    @Test
    public void testEquals() {

        Function<Integer, Integer> f = x -> {
            Blackhole.consumeCPU(1000 * 10);
            return x * x;
        };
        Set<Integer> data = new HashSet<>();
        {
            Random random = new Random();
            for (int i = 0; i < 10; i++) {
                data.add(random.nextInt());
            }
        }
        ExecutorService executor = Executors.newFixedThreadPool(4);
        try {

            Set<Integer> serial = new SerialMapper().map(new HashSet<>(data), f);
            Set<Integer> parallel = new ExecutorServiceMapper(executor).map(new HashSet<>(data), f);
            assertEquals(serial, parallel);
        }
        finally {
            executor.shutdown();
        }
    }

}
