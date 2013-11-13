package itba.pdc.proxy;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;


/**
 * Hello world!
 * 
 */
public class LogTest {
	public static void main(String[] args) {
		Logger logger = (Logger) LoggerFactory.getLogger("error.log");
		// Keep in mind that all of those classes are from SLF4J package!
		logger.info("Here is my important-as-hell message!");
		logger.debug("Debug");
		logger.error("Error");
	}
}
