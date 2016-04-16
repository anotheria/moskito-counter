package net.anotheria.moskitocounter;

import net.anotheria.moskito.web.MoskitoHttpServlet;
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

/**
 * TODO comment this class
 *
 * @author lrosenberg
 * @since 27.11.13 09:25
 */
@WebServlet(urlPatterns = "/counter/*")
public class CounterServlet extends MoskitoHttpServlet{

	private InspectPageCounter inspectPageCounter;
	private ToolCounter toolCounter;
	private VersionCounter versionCounter;
	private byte[] data;
	private static Logger log = LoggerFactory.getLogger(CounterServlet.class);

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		toolCounter = new ToolCounter();
		inspectPageCounter = new InspectPageCounter();
		versionCounter = new VersionCounter();

		File f = new File(config.getServletContext().getRealPath("spacer.gif"));
		FileInputStream fIn = null;
		try{
			fIn = new FileInputStream(f);
			data = new byte[fIn.available()];
			fIn.read(data);
			log.info("read "+data.length+" bytes");
		}catch(IOException e){
			log.warn("couldn't read file", e);
		}finally {
			if (fIn!=null){
				try{
					fIn.close();
				}catch(IOException ignored){}
			}
		}


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
		String version = t.length>1 ? t[1] : "unknown";
		boolean appHandled = false;

		if (!appHandled && (application.equals("webui") || application.equals("inspect"))){
			appHandled = true;
			toolCounter.inspect();
			versionCounter.inspect(version);

			String pageName = t.length==1 ? "Unknown" : t[2];
			if (pageName==null || pageName.length()==0)
				pageName = "Unknown";
			inspectPageCounter.countPage(pageName);
		}

		if (!appHandled && application.equals("control")){
			appHandled = true;
			toolCounter.control();
			versionCounter.control(version);
		}


		OutputStream oOut = resp.getOutputStream();
		try{
			resp.setHeader("Content-Type", "image/gif");
			resp.setHeader("Content-Length", ""+data.length);
			resp.setHeader("Content-Disposition", "attachment; filename=\"s.gif\"");
			oOut.write(data);
			oOut.flush();
		}catch(IOException e){
			log.warn("couldn't write pixel "+e.getMessage());
		}



	}


}
