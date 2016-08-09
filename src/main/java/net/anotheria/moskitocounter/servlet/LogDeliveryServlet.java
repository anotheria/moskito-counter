package net.anotheria.moskitocounter.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import net.anotheria.moskito.web.MoskitoHttpServlet;
import net.anotheria.moskitocounter.service.stats.logging.LoggingConfig;
import net.anotheria.util.StringUtils;
import net.anotheria.util.log.LogMessageUtil;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Log delivery endpoint.
 *
 * @author sshscp
 */
@WebServlet (name = "lDev",
		urlPatterns = "/logs/get")
public class LogDeliveryServlet extends MoskitoHttpServlet {
	/**
	 * Basic UUID.
	 */
	private static final long serialVersionUID = 7881832094348671135L;
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
		final String fName = getName(req);
		if (StringUtils.isEmpty(fName)) {
			res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		final File file = new File(config.getLogRootDirPath() + File.separator + fName);
		if (!file.exists() || file.isDirectory()) {
			res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		// setting right header's
		setHeaders(res, file);
		// streaming resource
		stream(file, res);
	}

	/**
	 * Read file name.
	 *
	 * @param request {@link HttpServletRequest}
	 * @return name
	 */
	private String getName(final HttpServletRequest request) {
		String fName = request.getParameter("name");
		if (StringUtils.isEmpty(fName))
			return fName;
		try {
			fName = URLDecoder.decode(fName, "UTF-8");
			return fName;
		} catch (final UnsupportedEncodingException uee) {
			LOGGER.error("fName can't be decoded", uee);
			return fName;
		}
	}

	/**
	 * Stream resource if exist.
	 *
	 * @param resourceFile
	 * 		- resource file path
	 * @param res
	 * 		- response
	 */
	private void stream(final File resourceFile, final HttpServletResponse res) {
		InputStream in = null;
		try {
			in = new FileInputStream(resourceFile);
			OutputStream out = res.getOutputStream();
			IOUtils.copyLarge(in, out);
			out.flush();
		} catch (final IOException e) {
			String message = LogMessageUtil.failMsg(e, resourceFile, res);
			LOGGER.warn(message, e);
			res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}


	/**
	 * Set HTTP header's.
	 *
	 * @param res
	 * 		- response
	 * @param resource
	 * 		- file itself
	 */
	private void setHeaders(final HttpServletResponse res, final File resource) {
		// csv - only - so )
		res.setContentType("text/comma-separated-values");
		DateFormat df = new SimpleDateFormat(RFC1123_PATTERN, LOCALE_US);
		df.setTimeZone(GMT_ZONE);
		// Setting last modified header
		res.setHeader("Last-Modified", df.format(new java.util.Date(resource.lastModified())));
		res.setHeader("Content-Length", String.valueOf(resource.length()));
		res.setHeader("content-disposition", "attachment; filename=\"" + resource.getName() + "\"");
		// Setting expiration header
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_YEAR, 3); // set resource expiration time to 3 day's
		res.addHeader("Expires", df.format(cal.getTime()));
	}


}

