package euromsg.com.euromobileandroid;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import euromsg.com.euromobileandroid.utils.AppUtils;

@RunWith(JUnit4.class)
public class AppUtilsTest {

    @Test
    public void testGetCurrentDateString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String result1 = dateFormat.format(new Date());
        String result2 = AppUtils.getCurrentDateString();
        assert(result1.equals(result2));
    }

    @Test
    public void testGetCurrentTurkeyDateString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        TimeZone tzTurkey = TimeZone.getTimeZone("Turkey");
        dateFormat.setTimeZone(tzTurkey);
        String result1 = dateFormat.format(new Date());
        String result2 = AppUtils.getCurrentTurkeyDateString(null);
        assert(result1.equals(result2));
    }

    @Test
    public void testIsDateDifferenceGreaterThan() {
        boolean result;
        String date1_1 = "2021-11-30 11:21:26";
        String date1_2 = "2021-11-24 11:21:26";
        int thresholdDay1 = 4;

        String date2_1 = "2021-11-30 11:21:26";
        String date2_2 = "2021-11-27 11:21:26";
        int thresholdDay2 = 4;

        String date3_1 = "2021-11-29 11:21:26";
        String date3_2 = "2021-11-30 11:21:26";
        int thresholdDay3 = 4;

        String date4_1 = "2021-11-30 11:21:26";
        String date4_2 = "2021.11.22 11:21:26";
        int thresholdDay4 = 4;

        result = AppUtils.isDateDifferenceGreaterThan(date1_1, date1_2, thresholdDay1) &&
                !AppUtils.isDateDifferenceGreaterThan(date2_1, date2_2, thresholdDay2) &&
                !AppUtils.isDateDifferenceGreaterThan(date3_1, date3_2, thresholdDay3) &&
                AppUtils.isDateDifferenceGreaterThan(date4_1, date4_2, thresholdDay4);
        assert(result);
    }
}