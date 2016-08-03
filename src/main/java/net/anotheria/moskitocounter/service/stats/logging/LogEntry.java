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
	 * Log app version.
	 */
	private String version;
	/**
	 * Log headers.
	 */
	private Map<String, String[]> headers;
	/**
	 * Log params.
	 */
	private Map<String, String[]> params;

	/**
	 * Constructor.
	 */
	public LogEntry(final Date time, final String ip, final Application app, final String page, final String ver,
					final Map<String, String[]> headerString, final Map<String, String[]> paramsString) {
		this.time = time;
		this.ip = ip;
		this.application = app;
		this.page = page;
		this.version = ver;
		this.headers = headerString;
		this.params = paramsString;
	}

	/**
	 * Creates data line, which may be dumped into log file.
	 *
	 * @param lineSeparator
	 * 		data separator in line scope
	 * @return line
	 */
	public String toDumpLine(final char lineSeparator, final LoggingConfig cnf) {
		final StringBuilder bld = new StringBuilder();
		bld.append(time).append(lineSeparator).append(ip).append(lineSeparator);
		bld.append(application).append(lineSeparator).append(version).
				append(lineSeparator).append("\"").append(page).append("\"");
		if (!cnf.isDumpHeaders() && !cnf.isDumpParams())
			return bld.toString();
		// headers
		if (cnf.isDumpHeaders())
			addMapData(headers, bld, lineSeparator);
		// params
		if (cnf.isDumpParams())
			addMapData(params, bld, lineSeparator);

		return bld.toString();
	}


	/**
	 * Add key-value pairs to 'container'.
	 *
	 * @param map
	 * 		source data map
	 * @param container
	 * 		data container
	 * @param lineSeparator
	 * 		data separator
	 */
	private static void addMapData(final Map<String, String[]> map, final StringBuilder container, final char lineSeparator) {
		if (container == null || map == null)
			return;
		container.append(lineSeparator).append("\"");
		for (final Map.Entry<String, String[]> entry : map.entrySet()) {
			if (entry.getValue() != null)
				for (final String value : entry.getValue())
					container.append(entry.getKey()).append(":").append(value).append(";");
			else
				//no value case
				container.append(entry.getKey()).append(":").append(";");
		}
		container.append("\"");
	}

}
