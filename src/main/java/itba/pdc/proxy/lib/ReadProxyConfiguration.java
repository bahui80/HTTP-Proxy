package itba.pdc.proxy.lib;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class ReadProxyConfiguration {
	private static ReadProxyConfiguration instance;
	private Logger infoLogger = (Logger) LoggerFactory.getLogger("info.log");
	private Properties prop;
	private Map<String, String> data;
	
	private ReadProxyConfiguration() {
		if (instance != null) {
			infoLogger.error("Instance of ReadProxyConfiguration already created");
			throw new IllegalArgumentException("Instance already created");
		}
		prop = new Properties();
		data = new HashMap<String,String>();
		try {
			prop.load(new FileInputStream("src/main/resources/proxy.properties"));
		} catch (FileNotFoundException e) {
			try {
				prop.load(new FileInputStream(
						"classes/proxy.properties"));
			} catch (FileNotFoundException e1) {
				throw new RuntimeException("File proxy.properties not found");
			} catch (IOException e1) {
				throw new RuntimeException("File proxy.properties couldn't be opened");
			}
		} catch (IOException e) {
			throw new RuntimeException("File proxy.properties couldn't be opened");
		}
	}
	
	public static synchronized ReadProxyConfiguration getInstance() {
		if (instance == null) {
			instance = new ReadProxyConfiguration();
		}
		return instance;
	}
	
	public String getServerIp() {
		return this.getString("server-ip");
	}
	
	public String getChainedIp() {
		return this.getString("chained-ip");
	}
	
	public String getAdminUsername() {
		return this.getString("username");
	}
	
	public String getAdminPassword() {
		return this.getString("password");
	}
	
	public Integer getServerPort() {
		return this.getInteger("server-port");
	}
	
	public Integer getAdminPort() {
		return this.getInteger("admin-port");
	}
	
	public Integer getChainedPort() {
		return this.getInteger("chained-port");
	}
	
	public Integer getMaxConns() {
		return this.getInteger("max-cons");
	}
	
	private String getString(String s) {
		String str = data.get(s);
		if(str == null){
			str = prop.getProperty(s);
			if (str.isEmpty()) {
				return null;
//				throw new IllegalStateException("No esta seteado ese parametro en el .properties");
			}
			data.put(s, str);
		}
		return str;
	}
	
	private Integer getInteger(String s){
		String value = this.getString(s);
		if (value == null) {
			return null;
		}
		return Integer.parseInt(this.getString(s));
	}
}
