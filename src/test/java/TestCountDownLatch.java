import org.junit.Test;
import ru.koluch.testJavaUtilConcurrency.CountDownLatchImpl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import static junit.framework.Assert.assertEquals;

/**
 * Created by Nikolai_Mavrenkov on 10/11/15.
 */
public class TestCountDownLatch {

    static final Function<Integer, Integer> f = x -> x * x;

    static final List<Integer> data = new ArrayList<>();
    static {
        Random random = new Random(42);
        for (int i = 0; i < 10000; i++) {
            data.add(random.nextInt());
        }
    }

    @Test
    public void testEquals() {
        CountDownLatchImpl countDownLatch = new CountDownLatchImpl();
        countDownLatch.testParallel();
        assertEquals(countDownLatch.map(new HashSet<>(data), f), countDownLatch.pmap(new HashSet<>(data), f));
    }
    
}
