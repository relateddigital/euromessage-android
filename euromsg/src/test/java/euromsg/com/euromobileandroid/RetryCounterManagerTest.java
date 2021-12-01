package euromsg.com.euromobileandroid;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import euromsg.com.euromobileandroid.utils.RetryCounterManager;

@RunWith(JUnit4.class)
public class RetryCounterManagerTest {

    @Test
    public void testGetCounterId() {
        int res1 = RetryCounterManager.getCounterId();
        int res2 = RetryCounterManager.getCounterId();
        int res3 = RetryCounterManager.getCounterId();
        int res4 = RetryCounterManager.getCounterId();
        int res5 = RetryCounterManager.getCounterId();

        boolean result;

        result = (res1 == 1) && (res2 == 2) && (res3 == 3) && (res4 == 4) && (res5 == 5);

        assert(result);
    }

    @Test
    public void testCounter() {
        boolean result;
        RetryCounterManager.increaseCounter(1);
        RetryCounterManager.increaseCounter(1);
        RetryCounterManager.increaseCounter(3);
        RetryCounterManager.increaseCounter(5);
        RetryCounterManager.increaseCounter(5);
        RetryCounterManager.increaseCounter(5);
        RetryCounterManager.increaseCounter(4);
        RetryCounterManager.increaseCounter(4);
        RetryCounterManager.increaseCounter(4);
        RetryCounterManager.clearCounter(4);

        result = (RetryCounterManager.getCounterValue(1) == 1) &&
                (RetryCounterManager.getCounterValue(2) == -1) &&
                (RetryCounterManager.getCounterValue(3) == 0) &&
                (RetryCounterManager.getCounterValue(4) == -1) &&
                (RetryCounterManager.getCounterValue(5) == 2);

        assert(result);
    }
}
