package net.anotheria.moskitocounter.servlet;

import net.anotheria.anoprise.metafactory.MetaFactory;
import net.anotheria.anoprise.metafactory.MetaFactoryException;
import net.anotheria.moskito.web.MoskitoHttpServlet;
import net.anotheria.moskitocounter.service.shared.TierConfigurationUtil;
import net.anotheria.moskitocounter.service.stats.RequestData;
import net.anotheria.moskitocounter.service.stats.StatCountService;
import net.anotheria.moskitocounter.service.stats.counter.InspectPageCounter;
import net.anotheria.moskitocounter.service.stats.counter.ToolCounter;
import net.anotheria.moskitocounter.service.stats.counter.VersionCounter;
import net.anotheria.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base counting endpoint.
 *
 * @author lrosenberg
 * @since 27.11.13 09:25
 */
@WebServlet (urlPatterns = "/counter/*")
public class CounterServlet extends MoskitoHttpServlet {
	/**
	 * Logging util.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(CounterServlet.class);
	/**
	 * Pixel file name.
	 */
	private static final String PIXEL = "spacer.gif";
	/**
	 * {@link StatCountService} instance.
	 */
	private StatCountService scService;
	/**
	 * Pixel itself.
	 */
	private byte[] data;

	/**
	 * Static init.
	 * In case if required should be moved away.
	 */
	static {
		TierConfigurationUtil.configureServices();
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		try {
			scService = MetaFactory.get(StatCountService.class);
		} catch (MetaFactoryException e) {
			LOGGER.error("FATAL", "I'm not able to init StatCount-Facility!", e);
		}

		final File f = new File(config.getServletContext().getRealPath(PIXEL));
		FileInputStream fIn = null;
		try {
			fIn = new FileInputStream(f);
			data = new byte[fIn.available()];
			//noinspection ResultOfMethodCallIgnored
			fIn.read(data);
			LOGGER.info("read " + data.length + " bytes");
		} catch (final IOException e) {
			LOGGER.warn("couldn't read file", e);
		} finally {
			if (fIn != null) {
				try {
					fIn.close();
				} catch (IOException ignored) {
				}
			}
		}


	}

	@Override
	protected void moskitoDoGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		final RequestData.Builder dBuilder = new RequestData.Builder();
		dBuilder.withIp(req.getRemoteAddr()).withPathInfo(req.getPathInfo()).withQueryString(req.getQueryString()).withParameters(req.getParameterMap()).
				withHeaders(getHeadersMap(req));
		scService.count(dBuilder.build());

		final OutputStream oOut = resp.getOutputStream();
		try {
			resp.setHeader("Content-Type", "image/gif");
			resp.setHeader("Content-Length", "" + data.length);
			resp.setHeader("Content-Disposition", "attachment; filename=\"s.gif\"");
			oOut.write(data);
			oOut.flush();
		} catch (final IOException e) {
			LOGGER.warn("couldn't write pixel " + e.getMessage());
		}
	}

	/**
	 * Extract headers collection from {@link HttpServletRequest}.
	 *
	 * @param request
	 * 		{@link HttpServletRequest}
	 * @return headers collection
	 */
	private static Map<String, String[]> getHeadersMap(final HttpServletRequest request) {
		final Map<String, String[]> headers = new HashMap<String, String[]>();
		final Enumeration<String> hNames = request.getHeaderNames();
		if (hNames == null)
			return headers;
		while (hNames.hasMoreElements()) {
			final String name = hNames.nextElement();
			if (StringUtils.isEmpty(name))
				continue;
			final List<String> values = new ArrayList<String>();
			final Enumeration<String> vValues = request.getHeaders(name);
			if (vValues == null)
				continue;
			while (vValues.hasMoreElements()) {
				final String val = vValues.nextElement();
				if (StringUtils.isEmpty(val))
					continue;
				values.add(val);
			}
			if (!values.isEmpty())
				headers.put(name.toUpperCase(), values.toArray(new String[values.size()]));
		}
		return headers;
	}


}
