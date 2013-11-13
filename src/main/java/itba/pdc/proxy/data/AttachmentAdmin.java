package itba.pdc.proxy.data;

import itba.pdc.proxy.model.EHttpRequest;
import itba.pdc.proxy.parser.HttpParserRequest;
import itba.pdc.proxy.parser.interfaces.HttpParser;

import java.nio.ByteBuffer;

/**
 * 
 * @author Martin Purita
 * @version 1.0
 * 
 *          TODO:
 */
public class AttachmentAdmin implements Attachment {
	private ByteBuffer buff;
	private ProxyType proxyType;
	private HttpParser parser;
	private EHttpRequest request;

	public AttachmentAdmin(ProxyType proxyType, int buffSize) {
		this.proxyType = proxyType;
		this.buff = ByteBuffer.allocate(buffSize);
		this.request = new EHttpRequest();
		this.parser = new HttpParserRequest(request);
	}

	public ByteBuffer getBuff() {
		return buff;
	}

	public HttpParser getParser() {
		return parser;
	}

	public EHttpRequest getRequest() {
		return request;
	}

	public void setBuff(ByteBuffer buff) {
		this.buff = ByteBuffer.allocate(buff.capacity());
		buff.flip();
		this.buff.put(buff);
	}

	public ProxyType getProxyType() {
		return proxyType;
	}
}