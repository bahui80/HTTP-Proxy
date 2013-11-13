package itba.pdc.proxy;

import java.io.FileNotFoundException;
import java.io.IOException;

import itba.pdc.proxy.lib.ReadProxyConfiguration;

public class ReadProxyConfigurationTest {
	public static void main(String[] args) throws FileNotFoundException, IOException {
		ReadProxyConfiguration configuration = ReadProxyConfiguration.getInstance();
		System.out.println("SERVER" + configuration.getServerIp().isEmpty());
	}
}
