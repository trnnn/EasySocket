package easysocket.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;

public class PrintStackTrace {
	public static void print(Logger logger, Throwable e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw, true));
		String strStack = sw.toString();
		logger.error("Exception in thread \""
				+ Thread.currentThread().getName() + "\": "
				+ e.getClass().getName());
		logger.error(strStack);
	}
}
