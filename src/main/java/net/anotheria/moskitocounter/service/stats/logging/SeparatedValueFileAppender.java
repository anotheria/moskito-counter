package net.anotheria.moskitocounter.service.stats.logging;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import ch.qos.logback.core.rolling.RollingFileAppender;

/**
 * Additional Rolling file appender ipl, which allow to provide desired header.
 *
 * @author sshscp
 */
public class SeparatedValueFileAppender<Type> extends RollingFileAppender<Type> {

	@Override
	public void openFile(String fileName) throws IOException {
		super.openFile(fileName);
		final File f = new File(fileName);
		if (f.exists() && f.isFile() && f.length() == 0) {
			lock.lock();
			try {
				//print header to new log files
				new PrintWriter(new OutputStreamWriter(getOutputStream(), StandardCharsets.UTF_8), true).
						println(LogEntry.toDumpHeader(LogProcessor.CSV_LINE_SEPARATOR));
			} finally {
				lock.unlock();
			}
		}
	}
}
