import org.junit.Test;
import ru.koluch.testJavaUtilConcurrency.setsMapping.CountDownLatchMapper;
import ru.koluch.testJavaUtilConcurrency.setsMapping.SerialMapper;

import java.util.*;
import java.util.function.Function;

import static junit.framework.Assert.assertEquals;

/**
 * Created by Nikolai_Mavrenkov on 10/11/15.
 */
public class TestCountDownLatch {

    @Test
    public void testEquals() {

        Function<Integer, Integer> f = x -> x * x;

        List<Integer> data = new ArrayList<>();
        Random random = new Random(42);
        for (int i = 0; i < 10000; i++) {
            data.add(random.nextInt());
        }

        Set<Integer> serial = new SerialMapper().map(new HashSet<>(data), f);
        Set<Integer> parallel = new CountDownLatchMapper().map(new HashSet<>(data), f);
        assertEquals(serial, parallel);
    }

    @Test(expected = RuntimeException.class)
    public void testPmapException() {
        Function<Integer, Integer> f = x -> x / x;
        List<Integer> data = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            data.add(i);
        }
        CountDownLatchMapper countDownLatch = new CountDownLatchMapper();
        countDownLatch.map(new HashSet<>(data), f);
    }

}
