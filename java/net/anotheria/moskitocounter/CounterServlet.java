package net.anotheria.moskitocounter;

import net.anotheria.moskito.web.MoskitoHttpServlet;
import net.anotheria.util.StringUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * TODO comment this class
 *
 * @author lrosenberg
 * @since 27.11.13 09:25
 */
@WebServlet(urlPatterns = "/counter/*")
public class CounterServlet extends MoskitoHttpServlet{

	private WebUIPageCounter webUIPageCounter;
	private ToolCounter toolCounter;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		toolCounter = new ToolCounter();
		webUIPageCounter = new WebUIPageCounter();
	}

	@Override
	protected void moskitoDoGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String path = req.getPathInfo();
		if (path==null || path.length()==0)
			return;

		if (path.startsWith("/") && path.length()>1)
			path = path.substring(1);

		String t[] = StringUtils.tokenize(path, '/');
		String application = t[0];
		boolean appHandled = false;

		if (!appHandled && application.equals("webui")){
			appHandled = true;
			toolCounter.webui();

			String pageName = t.length==1 ? "Unknown" : t[1];
			if (pageName==null || pageName.length()==0)
				pageName = "Unknown";
			webUIPageCounter.countPage(pageName);
		}

		if (!appHandled && application.equals("control")){
			appHandled = true;
			toolCounter.control();
		}



	}


}
