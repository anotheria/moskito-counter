package net.anotheria.moskitocounter.service.stats.logging;

import java.io.Serializable;
import java.util.Arrays;

import org.configureme.ConfigurationManager;
import org.configureme.annotations.Configure;
import org.configureme.annotations.ConfigureMe;
import org.configureme.annotations.DontConfigure;
import org.slf4j.LoggerFactory;

/**
 * Common logging config.
 *
 * @author sshscp
 */
@ConfigureMe (name = "logging-config")
public class LoggingConfig implements Serializable {
	/**
	 * Basic serial version UID.
	 */
	@DontConfigure
	private static final long serialVersionUID = -1982798099432268223L;
	/**
	 * Sync monitor.
	 */
	@DontConfigure
	private static final Object S_MON = new Object();
	/**
	 * Configuration instance.
	 */
	@DontConfigure
	private static volatile LoggingConfig instance;

	/**
	 * Allow to enable/disable logging func.
	 */
	@Configure
	private boolean loggingEnabled = true;
	/**
	 * LoggingConfig 'logRootDirPath'.
	 * Logs root dir path.
	 */
	@Configure
	private String logRootDirPath = "/work/data/counter_logging/";
	/**
	 * Allow to provide log file size.
	 * Note LogBack classic  supports next suffixes -'kb', 'mb', 'gb', this suffixes are actual for logback logger.
	 */
	@Configure
	private String logFileMaxSize = "500MB";
	/**
	 * Logging aggregation strategy.
	 */
	@Configure
	private LoggingAggregationStrategy aggregationStrategy = LoggingAggregationStrategy.DEFAULT;
	/**
	 * Parameter name array which may be dumped.
	 */
	@Configure
	private String[] paramsToDump = new String[0];
	/**
	 * Headers name array which may be dumped.
	 */
	@Configure
	private String[] headersToDump = new String[0];
	/**
	 * Dump - all headers or just selected.
	 */
	@Configure
	private boolean dumpAllParams = true;
	/**
	 * Dump - all params or just selected.
	 */
	@Configure
	private boolean dumpAllHeaders = true;

	/**
	 * Constructor.
	 */
	private LoggingConfig() {
	}

	/**
	 * Return {@link LoggingConfig} instance.
	 *
	 * @return {@link LoggingConfig}
	 */
	public static LoggingConfig getInstance() {
		if (instance != null)
			return instance;
		synchronized (S_MON) {
			if (instance != null)
				return instance;
			instance = new LoggingConfig();
			try {
				ConfigurationManager.INSTANCE.configure(instance);
				//CHECKSTYLE:OFF
			} catch (final RuntimeException e) {
				//CHECKSTYLE:ON
				LoggerFactory.getLogger(LoggingConfig.class).warn("LoggingConfig init failed! Relying on defaults! " + e.getMessage());
			}
			return instance;
		}
	}

	public boolean isLoggingEnabled() {
		return loggingEnabled;
	}

	public void setLoggingEnabled(boolean loggingEnabled) {
		this.loggingEnabled = loggingEnabled;
	}

	public String getLogRootDirPath() {
		return logRootDirPath;
	}

	public void setLogRootDirPath(String logRootDirPath) {
		this.logRootDirPath = logRootDirPath;
	}

	public String getLogFileMaxSize() {
		return logFileMaxSize;
	}

	public void setLogFileMaxSize(String logFileMaxSize) {
		this.logFileMaxSize = logFileMaxSize;
	}

	public LoggingAggregationStrategy getAggregationStrategy() {
		return aggregationStrategy;
	}

	public void setAggregationStrategy(LoggingAggregationStrategy aggregationStrategy) {
		this.aggregationStrategy = aggregationStrategy;
	}

	public String[] getParamsToDump() {
		return paramsToDump;
	}

	public void setParamsToDump(String[] paramsToDump) {
		this.paramsToDump = paramsToDump;
	}

	public String[] getHeadersToDump() {
		return headersToDump;
	}

	public void setHeadersToDump(String[] headersToDump) {
		this.headersToDump = headersToDump;
	}

	public boolean isDumpAllParams() {
		return dumpAllParams;
	}

	public void setDumpAllParams(boolean dumpAllParams) {
		this.dumpAllParams = dumpAllParams;
	}

	public boolean isDumpAllHeaders() {
		return dumpAllHeaders;
	}

	public void setDumpAllHeaders(boolean dumpAllHeaders) {
		this.dumpAllHeaders = dumpAllHeaders;
	}

	/**
	 * Provides logging aggregation alg.
	 */
	protected enum LoggingAggregationStrategy {
		/**
		 * To new log entry each hour.
		 */
		HOUR,
		/**
		 * To new log entry each day.
		 */
		DAY,
		/**
		 * To new log entry each week.
		 */
		WEEK,
		/**
		 * To new log entry each week.
		 */
		MONTH;
		/**
		 * Default value.
		 */
		protected static LoggingAggregationStrategy DEFAULT = WEEK;
	}

	@Override
	public String toString() {
		return "LoggingConfig{" +
				"loggingEnabled=" + loggingEnabled +
				", logRootDirPath='" + logRootDirPath + '\'' +
				", logFileMaxSize='" + logFileMaxSize + '\'' +
				", aggregationStrategy=" + aggregationStrategy +
				", paramsToDump=" + Arrays.toString(paramsToDump) +
				", headersToDump=" + Arrays.toString(headersToDump) +
				", dumpAllParams=" + dumpAllParams +
				", dumpAllHeaders=" + dumpAllHeaders +
				'}';
	}
}
