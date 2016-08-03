package net.anotheria.moskitocounter.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import net.anotheria.moskito.web.MoskitoHttpServlet;
import net.anotheria.moskitocounter.service.stats.logging.LoggingConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Render logs view.
 *
 * @author sshscp
 */
@WebServlet (name = "lView",
		urlPatterns = "/logs/view")
public class ShowLogsServlet extends MoskitoHttpServlet {
	/**
	 * Basic uuid.
	 */
	private static final long serialVersionUID = -4446078773706871186L;
	/**
	 * {@link Logger} instance.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(LogDeliveryServlet.class);
	/**
	 * Locale instance.
	 */
	private final static Locale LOCALE_US = Locale.US;
	/**
	 * GMT time zone .
	 */
	private final static TimeZone GMT_ZONE = TimeZone.getTimeZone("GMT");
	/**
	 * Format for RFC 1123 date string.
	 */
	private final static String RFC1123_PATTERN = "EEE, dd MMM yyyyy HH:mm:ss z";
	/**
	 * Logging config.
	 */
	private LoggingConfig config = LoggingConfig.getInstance();

	@Override
	protected void moskitoDoGet(final HttpServletRequest req, final HttpServletResponse res) throws ServletException, IOException {

		setHeaders(res);
		final StringBuilder content = new StringBuilder();
		content.append("<html>");
		content.append("<body>");
		content.append("<h1>").append("logs read/get page").append("</h1>");
		content.append("<ul>");
		final List<String> fileLinks = getFileLinks();
		for (final String link : fileLinks) {
			final String path = "/logs/get?name=" + link;
			content.append("<li>").append("<a href=").append("\"").append(path).append("\"").append(">").append(link).append("</li>");
		}
		content.append("</ul>");
		if (fileLinks.isEmpty())
			content.append("<p>").append("Logs dir - empty").append("</p");
		content.append("</body>");
		content.append("</html>");

		res.getWriter().write(content.toString());
		res.getWriter().flush();

	}

	/**
	 * Create and renders links.
	 *
	 * @return log links
	 */
	private List<String> getFileLinks() {
		final List<String> links = new ArrayList<String>();
		final File directory = new File(config.getLogRootDirPath());
		if (!directory.exists() || !directory.isDirectory()) {
			LOGGER.error("log dirs  does not exists");
			return Collections.emptyList();
		}
		final File[] files = directory.listFiles();
		if (files == null)
			return links;

		for (final File item : files) {
			if (item == null || !item.isFile() || !item.exists())
				continue;
			links.add(item.getName());

		}

		return links;
	}

	/**
	 * Set HTTP header's.
	 *
	 * @param res
	 * 		- response
	 */
	private void setHeaders(final HttpServletResponse res) {
		// csv - only - so )
		res.setContentType("text/html; charset=utf-8");
		DateFormat df = new SimpleDateFormat(RFC1123_PATTERN, LOCALE_US);
		df.setTimeZone(GMT_ZONE);
		// Setting expiration header
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_YEAR, 1);
		res.addHeader("Expires", df.format(cal.getTime()));
	}
}
