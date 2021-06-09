package euromsg.com.euromobileandroid.utils;

import android.util.Log;

public class RetryCounterManager {
    private static final String LOG_TAG = "RetryCounterManager";
    private static int counter1 = -1;
    private static int counter2 = -1;
    private static int counter3 = -1;
    private static int counter4 = -1;
    private static int counter5 = -1;

    public static int getCounterId() {
        int result;
        if(counter1 == -1) {
            counter1 = 0;
            result = 1;
        } else {
            if (counter2 == -1) {
                counter2 = 0;
                result = 2;
            } else {
                if(counter3 == -1) {
                    counter3 = 0;
                    result = 3;
                } else {
                    if(counter4 == -1) {
                        counter4 = 0;
                        result = 4;
                    } else {
                        if(counter5 == -1) {
                            counter5 = 0;
                            result = 5;
                        } else {
                            result = -1;
                            Log.i(LOG_TAG, "No counter could be found for re-try!");
                        }
                    }
                }
            }
        }
        return result;
    }

    public static void increaseCounter(int id) {
        switch (id) {
            case 1: {
                counter1++;
                break;
            }
            case 2: {
                counter2++;
                break;
            }
            case 3: {
                counter3++;
                break;
            }
            case 4: {
                counter4++;
                break;
            }
            case 5: {
                counter5++;
                break;
            }
            default: {
                Log.i(LOG_TAG, "There is no counter whose id matches!");
                break;
            }
        }
    }

    public static int getCounterValue(int id) {
        int result;
        switch (id) {
            case 1: {
                result = counter1;
                break;
            }
            case 2: {
                result = counter2;
                break;
            }
            case 3: {
                result = counter3;
                break;
            }
            case 4: {
                result = counter4;
                break;
            }
            case 5: {
                result = counter5;
                break;
            }
            default: {
                Log.i(LOG_TAG, "There is no counter whose id matches!");
                result = -1;
                break;
            }
        }
        return result;
    }

    public static void clearCounter(int id) {
        switch (id) {
            case 1: {
                counter1 = -1;
                break;
            }
            case 2: {
                counter2 = -1;
                break;
            }
            case 3: {
                counter3 = -1;
                break;
            }
            case 4: {
                counter4 = -1;
                break;
            }
            case 5: {
                counter5 = -1;
                break;
            }
            default: {
                Log.i(LOG_TAG, "There is no counter whose id matches!");
                break;
            }
        }
    }
}
