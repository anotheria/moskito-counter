package net.anotheria.moskitocounter.service.stats.logging;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import net.anotheria.moskitocounter.service.stats.type.Application;

/**
 * Log entry.
 *
 * @author sshscp
 */
public class LogEntry implements Serializable {
	/**
	 * Basic UUID.
	 */
	private static final long serialVersionUID = -2724401464881865408L;
	/**
	 * Log date.
	 */
	private Date time;
	/**
	 * Log ip.
	 */
	private String ip;
	/**
	 * Log application.
	 */
	private Application application;
	/**
	 * Log app page.
	 */
	private String page;
	/**
	 * Log headers.
	 */
	private Map<String, String[]> headers;

	/**
	 * Constructor.
	 */
	public LogEntry(final Date time, final String ip, final Application app, final String page, final Map<String, String[]> headerString) {
		this.time = time;
		this.ip = ip;
		this.application = app;
		this.page = page;
		this.headers = headerString;
	}

	/**
	 * Creates data line, which may be dumped into log file.
	 *
	 * @param lineSeparator
	 * 		data separator in line scope
	 * @return line
	 */
	public String toDumpLine(final char lineSeparator, final LoggingConfig cnf) {
		//TODO : improve me in config scope....
		final StringBuilder bld = new StringBuilder();
		bld.append(time).append(lineSeparator).append(ip).append(lineSeparator);
		bld.append(application).append(lineSeparator).append(page);
		bld.append(lineSeparator).append("\"");
		for (final Map.Entry<String, String[]> header : headers.entrySet())
			if (header.getValue() != null)
				for (final String value : header.getValue())
					bld.append(header.getKey()).append(":").append(value).append(";");
			else
				//no value case
				bld.append(header.getKey()).append(":").append(";");

		bld.append("\"");
		return bld.toString();
	}


}
