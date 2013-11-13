package itba.pdc.proxy;

import itba.pdc.exceptions.InvalidBufferSizeException;
import itba.pdc.exceptions.InvalidDefaultPortException;
import itba.pdc.proxy.data.AttachmentAdmin;
import itba.pdc.proxy.data.AttachmentProxy;
import itba.pdc.proxy.data.ProcessType;
import itba.pdc.proxy.data.ProxyType;
import itba.pdc.proxy.lib.ReadConstantsConfiguration;
import itba.pdc.proxy.lib.ReadProxyConfiguration;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class ConnectionManager {
	private static ConnectionManager instance = null;
	private Logger connectionLogger = (Logger) LoggerFactory
			.getLogger("connection.log");
	private String chained_ip;
	private Integer chained_port;
	private int max_conns = 2;
	private Map<String, Set<SocketChannel>> persistent_connections;

	private ConnectionManager() throws FileNotFoundException, IOException {
		if (instance != null) {
			connectionLogger
					.error("Instance of ReadProxyConfiguration already created");
			throw new IllegalArgumentException("Istance already created");
		}
		ReadProxyConfiguration pconf = ReadProxyConfiguration.getInstance();
		chained_ip = pconf.getChainedIp();
		chained_port = pconf.getChainedPort();
		persistent_connections = new HashMap<String, Set<SocketChannel>>();
		max_conns = ReadProxyConfiguration.getInstance().getMaxConns();
	}

	public static synchronized ConnectionManager getInstance()
			throws FileNotFoundException, IOException {
		if (instance == null) {
			instance = new ConnectionManager();
		}
		return instance;
	}

	public ServerSocketChannel registerServerSocket(Selector selector) throws IOException {
		ReadProxyConfiguration proxyConfiguration = ReadProxyConfiguration
				.getInstance();
		ReadConstantsConfiguration constantsConfiguration = ReadConstantsConfiguration
				.getInstance();
		String host = proxyConfiguration.getServerIp();
		Integer port = proxyConfiguration.getServerPort();
		ServerSocketChannel listnChannel = ServerSocketChannel.open();
		if (port == null) {
			Integer defaultPort = constantsConfiguration.getServerDefaultPort();
			if (defaultPort == null) {
				connectionLogger
						.error("Default port of server proxy is not defined");
				throw new InvalidDefaultPortException();
			}
			port = defaultPort;
		}
		if (host == null) {
			StringBuilder builder = new StringBuilder();
			connectionLogger.info(builder
					.append("The proxy will listen at my ip at port ")
					.append(port).toString());
			listnChannel.socket().bind(new InetSocketAddress(port));
		} else {
			StringBuilder builder = new StringBuilder();
			connectionLogger.info(builder.append("The proxy will listen at ")
					.append(host).append(" at port ").append(port).toString());
			listnChannel.socket().bind(new InetSocketAddress(host, port));
		}
		listnChannel.configureBlocking(false);
		Integer bufferSize = constantsConfiguration.getBufferSize();
		if (bufferSize == null) {
			throw new InvalidBufferSizeException();
		}
		listnChannel.register(selector, SelectionKey.OP_ACCEPT,
				new AttachmentProxy(ProcessType.CLIENT, ProxyType.PROXY,
						bufferSize));
		return listnChannel;
	}

	public ServerSocketChannel registerAdminSocket(Selector selector) throws IOException {
		ReadProxyConfiguration proxyConfiguration = ReadProxyConfiguration
				.getInstance();
		ReadConstantsConfiguration constantsConfiguration = ReadConstantsConfiguration
				.getInstance();
		Integer port = proxyConfiguration.getAdminPort();
		ServerSocketChannel listnChannel = ServerSocketChannel.open();
		if (port == null) {
			Integer defaultPort = constantsConfiguration.getAdmingDefaultPort();
			if (defaultPort == null) {
				connectionLogger
						.error("Default port of admin proxy is not defined");
				throw new InvalidDefaultPortException();
			}
			port = defaultPort;
		}
		StringBuilder builder = new StringBuilder();
		connectionLogger
				.info(builder
						.append("The proxy will listen at my ip for admin clients at port ")
						.append(port).toString());
		listnChannel.socket().bind(new InetSocketAddress("127.0.0.1", port));
		listnChannel.configureBlocking(false);
		Integer bufferSize = constantsConfiguration.getBufferSize();
		if (bufferSize == null) {
			connectionLogger.error("Buffer size is not define");
			throw new InvalidBufferSizeException();
		}
		listnChannel.register(selector, SelectionKey.OP_ACCEPT,
				new AttachmentAdmin(ProxyType.ADMIN, bufferSize));
		return listnChannel;
	}

	public SocketChannel getChannel(String host, Integer port)
			throws IOException {
		if (chained_ip == null || chained_port == null) {
			return persistentConnection(host, port);
		}
		return persistentConnection(chained_ip, chained_port);
	}

	public void close(String host, SocketChannel channel) throws IOException {
		Set<SocketChannel> channels = persistent_connections.get(host);
		if (channels.size() == max_conns)
			channel.close();
		else
			channels.add(channel);
		persistent_connections.put(host, channels);
	}

	private SocketChannel persistentConnection(String host, Integer port)
			throws IOException {
		Set<SocketChannel> opened_channels;
		SocketChannel channel = null;
		if ((opened_channels = this.persistent_connections.get(host)) == null) {
			opened_channels = new HashSet<SocketChannel>();
			this.persistent_connections.put(host, opened_channels);
		}

		StringBuilder builder = new StringBuilder();
//		connectionLogger.info(	builder.append("Open channel: ")
//				.append(opened_channels.size()).toString());
		if (opened_channels.size() == 0) {
			channel = SocketChannel.open(new InetSocketAddress(host, port));
			connectionLogger.info("Open new channel");
		} else {
			Iterator<SocketChannel> it = opened_channels.iterator();
			boolean found = false;
			connectionLogger
			.info(builder.append("There are ").append(opened_channels.size()).append(" opened").toString());
			while (it.hasNext() && !found) {
				channel = it.next();
				if (channel.isOpen() && channel.isConnected()) {
					connectionLogger
							.info("There is one available channel");
					found = true;
				}
				it.remove();
			}
		}
		return (channel != null) ? channel : SocketChannel
				.open(new InetSocketAddress(host, port));
	}
}
