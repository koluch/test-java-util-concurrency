import org.junit.Test;
import ru.koluch.testJavaUtilConcurrency.CountDownLatchImpl;

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
        
        CountDownLatchImpl countDownLatch = new CountDownLatchImpl();
        Set<Integer> serial = countDownLatch.map(new HashSet<>(data), f);
        Set<Integer> parallel = countDownLatch.pmap(new HashSet<>(data), f);
        assertEquals(serial, parallel);
    }

    @Test(expected = RuntimeException.class)
    public void testPmapException() {
        Function<Integer, Integer> f = x -> x / x;
        List<Integer> data = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            data.add(i);
        }
        CountDownLatchImpl countDownLatch = new CountDownLatchImpl();
        countDownLatch.pmap(new HashSet<>(data), f);
    }

}
