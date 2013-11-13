package itba.pdc.proxy.data;

import itba.pdc.proxy.model.HttpRequest;
import itba.pdc.proxy.model.HttpResponse;
import itba.pdc.proxy.parser.HttpParserRequest;
import itba.pdc.proxy.parser.HttpParserResponse;
import itba.pdc.proxy.parser.interfaces.HttpParser;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * 
 * @author Martin Purita
 * @version 1.0
 * 
 *          TODO:
 */
public class AttachmentProxy implements Attachment {
	private ByteBuffer buff;
	private ProxyType proxyType;
	private ProcessType processID;
	private SelectionKey oppositeKey;
	private SocketChannel oppositeChannel;
	private HttpParser parser;
	private HttpRequest request;
	private HttpResponse response;

	public AttachmentProxy(ProcessType processID, ProxyType proxyType, int buffSize) {
		this.processID = processID;
		this.proxyType = proxyType;
		this.buff = ByteBuffer.allocate(buffSize);
		switch (processID) {
		case SERVER:
			this.response = new HttpResponse();
			parser = new HttpParserResponse(response);
			break;
		case CLIENT:
			this.request = new HttpRequest();
			parser = new HttpParserRequest(request);
			break;
		}
	}

	public ByteBuffer getBuff() {
		return buff;
	}

	public ProcessType getProcessID() {
		return processID;
	}

	public SelectionKey getOppositeKey() {
		return oppositeKey;
	}

	public SocketChannel getOppositeChannel() {
		return oppositeChannel;
	}

	public HttpParser getParser() {
		return parser;
	}

	public HttpRequest getRequest() {
		return request;
	}

	public void setOppositeKey(SelectionKey oppositeKey) {
		this.oppositeKey = oppositeKey;
	}

	public void setOppositeChannel(SocketChannel oppositeChannel) {
		this.oppositeChannel = oppositeChannel;
	}

	public void setBuff(ByteBuffer buff) {
		this.buff = ByteBuffer.allocate(buff.capacity());
		buff.flip();
		this.buff.put(buff);
	}
	
	public HttpResponse getResponse() {
		return this.response;
	}

	public ProxyType getProxyType() {
		return proxyType;
	}
}