package net.anotheria.moskitocounter.service.stats.logging;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import net.anotheria.anoprise.processor.PackageWorker;
import net.anotheria.util.StringUtils;
import net.anotheria.util.concurrency.IdBasedLock;
import net.anotheria.util.concurrency.IdBasedLockManager;
import net.anotheria.util.concurrency.SafeIdBasedLockManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Async data processor.
 *
 * @author sshscp
 */
public class LogProcessor implements PackageWorker<LogEntry> {
	/**
	 * Line separator.
	 */
	private static final char CSV_LINE_SEPARATOR = ',';
	/**
	 * Logger holder.
	 */
	private final LoggerProvider loggerProvider = new LoggerProvider();

	@Override
	public void doWork(final List<LogEntry> workingPackage) throws Exception {
		if (workingPackage == null || workingPackage.isEmpty())
			return;
		final Logger configuredLogger = loggerProvider.getConfiguredLogger();
		for (final LogEntry entry : workingPackage) {
			configuredLogger.info(entry.toDumpLine(CSV_LINE_SEPARATOR, LoggingConfig.getInstance()));
		}
	}

	@Override
	public int packageCapacity() {
		return 50;
	}


	/**
	 * Logger configuration util.
	 * In case if some initial/additional changes will be required, lot of stuff can be simply moved to config.
	 */
	public static final class LoggerProvider {
		/**
		 * Logger file name suffix. E.g. '[name].csv'
		 */
		protected static final String FILE_EXTENSION = ".csv";
		/**
		 * Clean up lock.
		 */
		private static final ReentrantLock CL_LOCK = new ReentrantLock();
		/**
		 * Logger content default patter.
		 */
		private static final String DEFAULT_NAME_PATTER = "%msg%n";
		/**
		 * Zip file suffix.
		 */
		private static final String ZIP_FILE_NAME_SUFFIX = ".%i." + FILE_EXTENSION + ".zip";
		/**
		 * Logger name separator.
		 */
		private static final String NAME_SEPARATOR = "_";
		/**
		 * Configured Loggers cache.
		 */
		private final ConcurrentMap<String, LoggerWrapper> loggersCache = new ConcurrentHashMap<String, LoggerWrapper>();
		/**
		 * {@link IdBasedLockManager} for Loggers cache operations management.
		 */
		private IdBasedLockManager<String> lockManager = new SafeIdBasedLockManager<String>();
		/**
		 * {@link LoggingConfig} instance.
		 */
		private LoggingConfig config = LoggingConfig.getInstance();

		/**
		 * Constructor.
		 */
		protected LoggerProvider() {
			Timer timer = new Timer(true);
			// init cache clean up thread
			timer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					cleanUpLoggersCache();
				}
			}, 0, TimeUnit.MINUTES.toMillis(30));
		}

		/**
		 * Return pre-configured {@link Logger} instance.
		 *
		 * @return {@link Logger}
		 */
		public Logger getConfiguredLogger() {

			final String loggerName = getLoggerName();

			final IdBasedLock<String> lock = lockManager.obtainLock(loggerName);
			lock.lock();
			try {
				final LoggerWrapper resultLogger = loggersCache.get(loggerName);
				if (resultLogger != null && resultLogger.get() != null) {
					final Logger log = resultLogger.get();
					if (log instanceof ch.qos.logback.classic.Logger) {
						ch.qos.logback.classic.Logger logger = ch.qos.logback.classic.Logger.class.cast(log);
						//check whether appender was not detached!
						if (logger.getAppender(loggerName) != null)
							return logger;
					}
				}
				//  note - in case if some lo4j over slf will be in class path - this won't ever work (
				final ch.qos.logback.classic.Logger logger = ch.qos.logback.classic.Logger.class.cast(LoggerFactory.getLogger(loggerName));
				logger.setLevel(Level.INFO);
				logger.setAdditive(false);
				if (logger.getAppender(loggerName) != null) {
					LoggerWrapper cLog = new LoggerWrapper(logger);
					loggersCache.put(loggerName, cLog);
					return logger;
				}

				final PatternLayoutEncoder ple = new PatternLayoutEncoder();
				final LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
				ple.setPattern(DEFAULT_NAME_PATTER);
				ple.setContext(lc);
				ple.start();

				// Appenders
				final RollingFileAppender<ILoggingEvent> fileAppender = new RollingFileAppender<ILoggingEvent>();
				fileAppender.setName(loggerName);
				// set file path
				fileAppender.setFile(resolveFilePath(loggerName, FILE_EXTENSION));
				fileAppender.setEncoder(ple);
				final FixedWindowRollingPolicy rollingPolicy = new FixedWindowRollingPolicy();
				rollingPolicy.setContext(lc);
				// rolling policies need to know their parent
				rollingPolicy.setParent(fileAppender);
				rollingPolicy.setMinIndex(1);
				rollingPolicy.setMaxIndex(1000);
				rollingPolicy.setFileNamePattern(resolveFilePath(loggerName, ZIP_FILE_NAME_SUFFIX));
				rollingPolicy.start();
				final SizeBasedTriggeringPolicy<ILoggingEvent> triggeringPolicy = new SizeBasedTriggeringPolicy<ILoggingEvent>();
				triggeringPolicy.setMaxFileSize(config.getLogFileMaxSize());
				triggeringPolicy.start();
				fileAppender.setContext(lc);
				fileAppender.setTriggeringPolicy(triggeringPolicy);
				fileAppender.setRollingPolicy(rollingPolicy);
				fileAppender.start();
				logger.addAppender(fileAppender);
				LoggerWrapper cLog = new LoggerWrapper(logger);
				loggersCache.put(loggerName, cLog);
				return logger;
			} finally {
				lock.unlock();
			}
		}


		/**
		 * Performs cache clean up.
		 */
		protected void cleanUpLoggersCache() {
			if (loggersCache.isEmpty() || CL_LOCK.isLocked())
				return;
			CL_LOCK.lock();
			try {
				final Map<String, LoggerWrapper> view = new HashMap<String, LoggerWrapper>(loggersCache);
				for (final Map.Entry<String, LoggerWrapper> entry : view.entrySet()) {
					final LoggerWrapper value = entry.getValue();
					if (value != null && value.isExpired()) {
						stopLogger(value.get());
						loggersCache.remove(entry.getKey());
					}
				}
			} finally {
				CL_LOCK.unlock();
			}

		}

		/**
		 * Stop {@link Logger} inner facilities (appender, rolling policy... so on) when logger was not used during some period of time.
		 *
		 * @param logger
		 * 		{@link Logger}
		 */
		private void stopLogger(final Logger logger) {
			if (logger == null)
				return;
			if (!(logger instanceof ch.qos.logback.classic.Logger))
				return;
			final IdBasedLock<String> lock = lockManager.obtainLock(logger.getName());
			lock.lock();
			try {
				ch.qos.logback.classic.Logger classicLogger = (ch.qos.logback.classic.Logger) logger;
				classicLogger.detachAndStopAllAppenders();
				//remove from cache
				loggersCache.remove(logger.getName());
			} finally {
				lock.unlock();
			}
		}

		/**
		 * Resolves log - file path.
		 *
		 * @param firstPathParam
		 * 		var - first path param (date as string )
		 * @param suffix
		 * 		- file extension
		 * @return file path
		 */
		protected String resolveFilePath(final String firstPathParam, final String suffix) {
			boolean separatorRequired = !StringUtils.isEmpty(config.getLogRootDirPath()) && !config.getLogRootDirPath().endsWith(File.separator);
			return config.getLogRootDirPath() + (separatorRequired ? File.separator : "") + firstPathParam + suffix;
		}

		/**
		 * Cached logger. Wraps logger instance.
		 */
		private static class LoggerWrapper {
			/**
			 * Max time during - which cached logger will remains active.
			 * Afterwards it should be removed during cleanUp operation.
			 * For now 10 minutes.
			 */
			private static final long MAX_INACTIVE_TIME = TimeUnit.HOURS.toMillis(1);
			/**
			 * LoggerWrapper 'logger'.
			 */
			private final Logger logger;
			/**
			 * LoggerWrapper last logger use/access ts.
			 * Actual only in scope of cleanning thread.
			 */
			private long lastAccessTs;

			/**
			 * Constructor.
			 *
			 * @param logger
			 * 		{@link Logger}
			 */
			public LoggerWrapper(final Logger logger) {
				this.logger = logger;
				this.lastAccessTs = System.currentTimeMillis();
			}

			/**
			 * Return {@link Logger} instance, both with last access time adjustment.
			 *
			 * @return {@link Logger}
			 */
			public Logger get() {
				try {
					return logger;
				} finally {
					lastAccessTs = System.currentTimeMillis();
				}
			}

			@Override
			public String toString() {
				return "CachedLogger{" +
						"logger_name=" + logger.getName() +
						"lastAccessTs=" + lastAccessTs +
						'}';
			}

			/**
			 * Return {@code true} in case if logger has been expired and should be stopped.
			 *
			 * @return boolean value
			 */
			public boolean isExpired() {
				return System.currentTimeMillis() >= lastAccessTs + MAX_INACTIVE_TIME;
			}
		}

		/**
		 * Return name for the logger.
		 *
		 * @return name to be used
		 */
		protected String getLoggerName() {
			final Calendar calendar = Calendar.getInstance();
			final LoggingConfig.LoggingAggregationStrategy strategy = config.getAggregationStrategy();
			switch (strategy) {
				case HOUR:
					return calendar.get(Calendar.YEAR) + NAME_SEPARATOR + "w_" + calendar.get(Calendar.WEEK_OF_YEAR) + NAME_SEPARATOR +
							"d_" + calendar.get(Calendar.DAY_OF_YEAR) + NAME_SEPARATOR + "h_" + calendar.get(Calendar.HOUR_OF_DAY);
				case DAY:
					return calendar.get(Calendar.YEAR) + NAME_SEPARATOR + "w_" + calendar.get(Calendar.WEEK_OF_YEAR) + NAME_SEPARATOR +
							"d_" + calendar.get(Calendar.DAY_OF_YEAR);
				case WEEK:
					return calendar.get(Calendar.YEAR) + NAME_SEPARATOR + "w_" + calendar.get(Calendar.WEEK_OF_YEAR);
				case MONTH:
					return calendar.get(Calendar.YEAR) + NAME_SEPARATOR + "m_" + calendar.get(Calendar.MONTH);
				default:
					throw new AssertionError(strategy + " not supported");
			}

		}


	}

}
