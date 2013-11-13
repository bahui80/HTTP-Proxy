package itba.pdc.proxy.handler;

import itba.pdc.admin.MetricManager;
import itba.pdc.proxy.ConnectionManager;
import itba.pdc.proxy.data.AttachmentProxy;
import itba.pdc.proxy.data.ProcessType;
import itba.pdc.proxy.data.ProxyType;
import itba.pdc.proxy.lib.GenerateHttpResponse;
import itba.pdc.proxy.lib.ManageParser;
import itba.pdc.proxy.lib.ReadingState;
import itba.pdc.proxy.model.HttpRequest;
import itba.pdc.proxy.model.HttpResponse;
import itba.pdc.proxy.model.StatusRequest;
import itba.pdc.proxy.parser.HttpParserResponse;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class HttpHandler implements TCPProtocol {
	private int bufferSize; // Size of I/O buffer
	private Logger accessLogger = (Logger) LoggerFactory
			.getLogger("access.log");
	private Logger debugLog = (Logger) LoggerFactory.getLogger("debug.log");
	private static final MetricManager metricManager = MetricManager
			.getInstance();

	public HttpHandler(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	public void handleAccept(SelectionKey key) throws IOException {
		SocketChannel clntChan = ((ServerSocketChannel) key.channel()).accept();
		clntChan.configureBlocking(false);

		SelectionKey clientKey = clntChan.register(key.selector(),
				SelectionKey.OP_READ);
		AttachmentProxy att = (AttachmentProxy) key.attachment();
		AttachmentProxy clientAtt = new AttachmentProxy(att.getProcessID(),
				att.getProxyType(), this.bufferSize);
		clientKey.attach(clientAtt);
		accessLogger.info("Accept new connection");
		metricManager.addAccess();
	}

	public void handleRead(final SelectionKey key) throws IOException {
		AttachmentProxy att = (AttachmentProxy) key.attachment();
		SocketChannel channel = (SocketChannel) key.channel();

		ByteBuffer buf = att.getBuff();
		if (channel.isOpen() && channel.isConnected()) {
			final long bytesRead = channel.read(buf);
			if (bytesRead == -1) {
				if (att.getProcessID().equals(ProcessType.SERVER)) {
					HttpParserResponse parser = (HttpParserResponse) att
							.getParser();
					if (parser.isConnectionClose()) {
						MetricManager.getInstance().addStatusCode(
								att.getResponse().getStatusCode());
						att.getResponse().setBody(att.getParser().getBuffer());
						sendMessageToClient(att);
					}
				}
				StringBuilder builder = new StringBuilder();
				accessLogger.info(builder.append("Close connection with ")
						.append(channel.getRemoteAddress()).toString());
				key.cancel();
				channel.close();
			} else if (bytesRead > 0) {
				metricManager.addBytesRead(bytesRead);
				switch (att.getProcessID()) {
				case CLIENT:
					handleClient(key);
					break;
				case SERVER:
					handleServer(key);
					break;
				default:
					debugLog.error("Trying to read from an invalid process");
					break;
				}
			}
		} else {
			buf.compact();
		}
	}

	public void handleWrite(SelectionKey key) throws IOException {
		/*
		 * Channel is available for writing, and key is valid (i.e., client
		 * channel not closed).
		 */
		// Retrieve data read earlier
		AttachmentProxy att = (AttachmentProxy) key.attachment();
		SocketChannel channel = (SocketChannel) key.channel();

		ByteBuffer buf = att.getBuff();
		buf.flip();
		do {
			if (channel.isOpen() && channel.isConnected()) {
				int bytesWritten = channel.write(buf);
				metricManager.addBytesWritten(bytesWritten);
			} else {
				break;
			}
		} while (buf.hasRemaining());
		if (!buf.hasRemaining()) { // Buffer completely written?
			// Nothing left, so no longer interested in writes
			if (att.getProcessID().equals(ProcessType.CLIENT)) {
				channel.close();
				key.cancel();
			} else {
				key.interestOps(SelectionKey.OP_READ);
			}
		}
		buf.compact(); // Make room for more data to be read in
	}

	private void handleClient(SelectionKey key) {
		AttachmentProxy att = (AttachmentProxy) key.attachment();
		ReadingState requestFinished = ManageParser.parse(att.getParser(),
				att.getBuff());
		switch (requestFinished) {
		case FINISHED:
			HttpRequest request = att.getRequest();
			SocketChannel oppositeChannel = null;
			SelectionKey oppositeKey = null;
			try {
				 oppositeChannel = ConnectionManager.getInstance().getChannel(
				 request.getHost(), request.getPort());
				oppositeChannel.configureBlocking(false);
			} catch (Exception e) {
				accessLogger
						.error("Trying to connect to an invalid host or invalid port: "
								+ request.getHost() + ", " + request.getPort());
				try {
					request.setStatus(StatusRequest.INVALID_HOST_PORT);
					sendError(key);
				} catch (IOException e1) {
				}
				return;
			}
			try {
				oppositeKey = oppositeChannel.register(key.selector(),
						SelectionKey.OP_WRITE);
			} catch (ClosedChannelException e) {
				debugLog.error("Trying to register a key in a closed channel");
				try {
					request.setStatus(StatusRequest.CLOSED_CHANNEL);
					sendError(key);
				} catch (IOException e1) {
				}
				return;
			}
			AttachmentProxy serverAtt = new AttachmentProxy(ProcessType.SERVER,
					ProxyType.PROXY, this.bufferSize);

			serverAtt.setOppositeKey(key);
			serverAtt.setOppositeChannel((SocketChannel) key.channel());

			att.setOppositeChannel(oppositeChannel);
			att.setOppositeKey(oppositeKey);
			ByteBuffer requestBuffer = request.getStream();
			serverAtt.setBuff(requestBuffer);
			oppositeKey.attach(serverAtt);
			break;
		case UNFINISHED:
			key.interestOps(SelectionKey.OP_READ);
			break;
		case ERROR:
			try {
				sendError(key);
			} catch (IOException e) {
			}
			break;
		}
	}

	private void sendError(SelectionKey key) throws IOException {
		AttachmentProxy att = (AttachmentProxy) key.attachment();
		String responseMessage = GenerateHttpResponse.generateResponseError(att
				.getRequest().getStatus());
		att = new AttachmentProxy(att.getProcessID(), att.getProxyType(),
				this.bufferSize);
		key.attach(att);
		ByteBuffer buffResponse = ByteBuffer.allocate(responseMessage
				.getBytes().length);
		buffResponse.put(responseMessage.getBytes());
		att.setBuff(buffResponse);
		key.interestOps(SelectionKey.OP_WRITE);
	}

	private void handleServer(SelectionKey key) throws FileNotFoundException,
			IOException {
		AttachmentProxy att = (AttachmentProxy) key.attachment();
		AttachmentProxy otherAtt = (AttachmentProxy) att.getOppositeKey()
				.attachment();
		HttpParserResponse parser = (HttpParserResponse) att.getParser();
		parser.setMethod(otherAtt.getRequest().getMethod());
		ReadingState responseFinished = ManageParser.parse(parser,
				att.getBuff());
		switch (responseFinished) {
		case FINISHED:
			MetricManager.getInstance().addStatusCode(
					att.getResponse().getStatusCode());
			 ConnectionManager.getInstance().close(
			 otherAtt.getRequest().getHost(),
			 (SocketChannel) key.channel());
			sendMessageToClient(att);
			break;
		case UNFINISHED:
			key.interestOps(SelectionKey.OP_READ);
			break;
		case ERROR:
			break;
		}
	}

	private void sendMessageToClient(AttachmentProxy att) {
		if (att.getOppositeKey().isValid()) {
			AttachmentProxy oppositeAtt = (AttachmentProxy) (AttachmentProxy) att
					.getOppositeKey().attachment();
			HttpResponse response = att.getResponse();
//			response.setBody(att.getParser().getBuffer());
			oppositeAtt.setBuff(response.getStream());
			metricManager.addStatusCode(response.getStatusCode());
			att.getOppositeKey().interestOps(SelectionKey.OP_WRITE);
		}
	}
}
