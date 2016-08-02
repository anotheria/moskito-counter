package net.anotheria.moskitocounter.service.stats;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import net.anotheria.anoprise.processor.QueuedMultiProcessor;
import net.anotheria.anoprise.processor.QueuedMultiProcessorBuilder;
import net.anotheria.anoprise.processor.UnrecoverableQueueOverflowException;
import net.anotheria.moskitocounter.service.stats.counter.InspectPageCounter;
import net.anotheria.moskitocounter.service.stats.counter.ToolCounter;
import net.anotheria.moskitocounter.service.stats.counter.VersionCounter;
import net.anotheria.moskitocounter.service.stats.logging.LogEntry;
import net.anotheria.moskitocounter.service.stats.logging.LogProcessor;
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
		try {
			loggingProcessor.addToQueue(new LogEntry(new Date(), requestData.getIp(), application, pageName, requestData.getHeaders()));
		} catch (final UnrecoverableQueueOverflowException e) {
			LOG.warn("Not able to dumpl requeest, cause of processor overflow! Skipping.");
		}
	}


}
