package itba.pdc.proxy.lib;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class ReadConstantsConfiguration {
	private static ReadConstantsConfiguration instance;
	private Logger infoLogger = (Logger) LoggerFactory.getLogger("info.log");
	private Properties prop;

	private ReadConstantsConfiguration() {
		if (instance != null) {
			infoLogger
					.error("Instance of ReadProxyConfiguration already created");
			throw new IllegalArgumentException("Istance already created");
		}
		prop = new Properties();
		try {
			prop.load(new FileInputStream(
					"src/main/resources/constants.properties"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
	}

	public static synchronized ReadConstantsConfiguration getInstance() {
		if (instance == null) {
			instance = new ReadConstantsConfiguration();
		}
		return instance;
	}

	public Integer getBufferSize() {
		String bufferSize = (String) prop.get("buffer-size");
		if (bufferSize.isEmpty()) {
			infoLogger.error("The buffer size can not be empty");
			return null;
		}
		try {
			Integer size = Integer.parseInt(bufferSize);
			if (size <= 0) {
				infoLogger
						.error("The buffer size must be a number greater than 0");
				return null;
			}
			return size;
		} catch (NumberFormatException e) {
			infoLogger.error("The buffer size must be a number");
			return null;
		}
	}

	public Integer getServerDefaultPort() {
		String defaultPort = (String) prop.get("server-default-port");
		if (defaultPort.isEmpty()) {
			infoLogger.error("The default port can not be empty");
			return null;
		}
		try {
			Integer port = Integer.parseInt(defaultPort);
			if (port <= 1024) {
				infoLogger.error("The default must be greater than 1024");
				return null;
			}
			return port;
		} catch (NumberFormatException e) {
			infoLogger.error("The default port must be a number");
			return null;
		}
	}

	public Integer getAdmingDefaultPort() {
		String defaultPort = (String) prop.get("admin-default-port");
		if (defaultPort.isEmpty()) {
			infoLogger.error("The default port can not be empty");
			return null;
		}
		try {
			Integer port = Integer.parseInt(defaultPort);
			if (port <= 1024) {
				infoLogger.error("The default must be greater than 1024");
				return null;
			}
			return port;
		} catch (NumberFormatException e) {
			infoLogger.error("The default port must be a number");
			return null;
		}
	}

	public Integer getCR() {
		return Integer.parseInt((String) prop.get("cr-byte"));
	}

	public Integer getLF() {
		return Integer.parseInt((String) prop.get("lf-byte"));
	}

	public Integer getA_Byte() {
		return Integer.parseInt((String) prop.get("a-byte"));
	}

	public Integer getE_Byte() {
		return Integer.parseInt((String) prop.get("e-byte"));
	}

	public Integer getI_Byte() {
		return Integer.parseInt((String) prop.get("i-byte"));
	}

	public Integer getO_Byte() {
		return Integer.parseInt((String) prop.get("o-byte"));
	}

	public Integer getC_Byte() {
		return Integer.parseInt((String) prop.get("c-byte"));
	}

	public Integer get4_Byte() {
		return Integer.parseInt((String) prop.get("4-byte"));
	}

	public Integer get3_Byte() {
		return Integer.parseInt((String) prop.get("3-byte"));
	}

	public Integer get1_Byte() {
		return Integer.parseInt((String) prop.get("1-byte"));
	}

	public Integer get0_Byte() {
		return Integer.parseInt((String) prop.get("0-byte"));
	}

	public Integer getLess_Byte() {
		return Integer.parseInt((String) prop.get("<-byte"));
	}

	public Integer getTimeout() {
		String bufferSize = (String) prop.get("timeout");
		if (bufferSize.isEmpty()) {
			infoLogger.error("The timeout can not be empty");
			return null;
		}
		try {
			Integer size = Integer.parseInt(bufferSize);
			if (size <= 0) {
				infoLogger
						.error("The timeout size must be a number greater than 0");
				return null;
			}
			return size;
		} catch (NumberFormatException e) {
			infoLogger.error("The timeout size must be a number");
			return null;
		}
	}

}
