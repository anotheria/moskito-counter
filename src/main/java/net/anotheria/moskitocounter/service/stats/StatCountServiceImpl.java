package net.anotheria.moskitocounter.service.stats;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.anotheria.anoprise.processor.QueuedMultiProcessor;
import net.anotheria.anoprise.processor.QueuedMultiProcessorBuilder;
import net.anotheria.anoprise.processor.UnrecoverableQueueOverflowException;
import net.anotheria.moskitocounter.service.stats.counter.InspectPageCounter;
import net.anotheria.moskitocounter.service.stats.counter.ToolCounter;
import net.anotheria.moskitocounter.service.stats.counter.VersionCounter;
import net.anotheria.moskitocounter.service.stats.logging.LogEntry;
import net.anotheria.moskitocounter.service.stats.logging.LogProcessor;
import net.anotheria.moskitocounter.service.stats.logging.LoggingConfig;
import net.anotheria.moskitocounter.service.stats.type.Application;
import net.anotheria.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common {@link StatCountService} implementation.
 *
 * @author sshscp
 */
public class StatCountServiceImpl implements StatCountService {
	/**
	 * Logging util.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(StatCountServiceImpl.class);
	/**
	 * Path delimiter.
	 */
	private static final char PATH_DELIMITER = '/';
	/**
	 * Path delimiter as string.
	 */
	private static final String PATH_DELIMITER_STRING = String.valueOf(PATH_DELIMITER);
	/**
	 * Unknown const.
	 */
	private static final String UNKNOWN = "unknown";
	/**
	 * Page counter stat.
	 */
	private final InspectPageCounter inspectPageCounter;
	/**
	 * Tool counter stat.
	 */
	private final ToolCounter toolCounter;
	/**
	 * Version counter stat.
	 */
	private final VersionCounter versionCounter;
	/**
	 * QueuedMultiProcessor  for logEntries dump.
	 */
	private final QueuedMultiProcessor<LogEntry> loggingProcessor;
	/**
	 * Configuration instance.
	 */
	private final LoggingConfig conf = LoggingConfig.getInstance();

	/**
	 * Constructor.
	 */
	public StatCountServiceImpl() {
		this.inspectPageCounter = new InspectPageCounter();
		this.toolCounter = new ToolCounter();
		this.versionCounter = new VersionCounter();
		this.loggingProcessor = new QueuedMultiProcessorBuilder<LogEntry>().
				//TODO : config required...
						setSleepTime(TimeUnit.SECONDS.toMillis(1)).
						setQueueSize(10000).
						setProcessorChannels(3).
						setProcessingLog(LOG).
						build("loggingDataDumpProcessor", new LogProcessor());
		loggingProcessor.start();
	}

	@Override
	public void count(final RequestData requestData) {
		if (requestData == null)
			throw new IllegalArgumentException("requestData param is null");

		String path = requestData.getPathInfo();
		if (path == null || path.length() == 0)
			return;

		if (path.startsWith(PATH_DELIMITER_STRING) && path.length() > 1)
			path = path.substring(1);

		final String pathParts[] = StringUtils.tokenize(path, PATH_DELIMITER);
		final Application application = Application.get(pathParts[0]);
		final String version = pathParts.length > 1 ? pathParts[1] : UNKNOWN;
		String pageName = "";
		switch (application) {
			case WEB_UI:
			case INSPECT:
				toolCounter.inspect();
				versionCounter.inspect(version);
				if (pathParts.length > 1 && !StringUtils.isEmpty(pathParts[2]))
					pageName = pathParts[2];
				pageName = StringUtils.isEmpty(pageName) ? UNKNOWN : pageName;
				inspectPageCounter.countPage(pageName);
				break;
			case CONTROL:
				toolCounter.control();
				versionCounter.control(version);
				break;
			default:

		}
		//add to log
		if (conf.isLoggingEnabled())
			processLogData(requestData, application, version, pageName);
	}

	/**
	 * Creates log entry for further processing.
	 *
	 * @param requestData
	 * 		{@link RequestData}
	 * @param application
	 * 		{@link Application}
	 * @param version
	 * 		app version
	 * @param pageName
	 * 		page name
	 */
	private void processLogData(final RequestData requestData, final Application application, final String version, final String pageName) {
		try {
			Map<String, String[]> headers = null;
			Map<String, String[]> params = null;
			// build header collection in case if required
			if (conf.isDumpHeaders()) {
				final String[] hd = conf.getHeadersToDump();
				if (hd == null || hd.length == 0)
					headers = requestData.getHeaders();
				else {
					headers = new HashMap<String, String[]>();
					for (final String headerName : hd) {
						final String[] value = requestData.getHeaders().get(headerName);
						headers.put(headerName, value == null ? new String[0] : value);
					}
				}

			}
			// build parameter collection in case if required
			if (conf.isDumpParams()) {
				final String[] prm = conf.getParamsToDump();
				if (prm == null || prm.length == 0)
					params = requestData.getParameters();
				else {
					params = new HashMap<String, String[]>();
					for (final String paramName : prm) {
						final String[] value = requestData.getParameters().get(paramName);
						params.put(paramName, value == null ? new String[0] : value);
					}
				}
			}
			loggingProcessor.addToQueue(new LogEntry(new Date(), requestData.getIp(), application, pageName, version,
					headers == null ? Collections.<String, String[]>emptyMap() : headers,
					params == null ? Collections.<String, String[]>emptyMap() : params));
		} catch (final UnrecoverableQueueOverflowException e) {
			LOG.warn("Not able to dumpl requeest, cause of processor overflow! Skipping.");
		}
	}


}
