package euromsg.com.euromobileandroid.utils;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Objects;

/**
 * Represents the logging configuration.
 * Equivalent to the Kotlin data class LogConfig nested within LogUtils.
 */
public class LogConfig {

    @SerializedName("isLoggingEnabled")
    private final boolean isLoggingEnabled;

    @SerializedName("excludedCustomerIds")
    private final List<String> excludedCustomerIds;

    /**
     * Constructor for LogConfig.
     * @param isLoggingEnabled Whether logging is globally enabled.
     * @param excludedCustomerIds A list of customer IDs for whom logging should be disabled.
     */
    public LogConfig(boolean isLoggingEnabled, List<String> excludedCustomerIds) {
        this.isLoggingEnabled = isLoggingEnabled;
        this.excludedCustomerIds = excludedCustomerIds;
    }

    /**
     * Checks if logging is enabled.
     * @return true if logging is enabled, false otherwise.
     */
    public boolean isLoggingEnabled() {
        return isLoggingEnabled;
    }

    /**
     * Gets the list of excluded customer IDs.
     * @return A list of customer IDs.
     */
    public List<String> getExcludedCustomerIds() {
        return excludedCustomerIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LogConfig logConfig = (LogConfig) o;
        return isLoggingEnabled == logConfig.isLoggingEnabled &&
                Objects.equals(excludedCustomerIds, logConfig.excludedCustomerIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isLoggingEnabled, excludedCustomerIds);
    }

    @Override
    public String toString() {
        return "LogConfig{" +
                "isLoggingEnabled=" + isLoggingEnabled +
                ", excludedCustomerIds=" + excludedCustomerIds +
                '}';
    }
}
