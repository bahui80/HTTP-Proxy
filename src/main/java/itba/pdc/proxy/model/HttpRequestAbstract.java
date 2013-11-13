package itba.pdc.proxy.model;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public abstract class HttpRequestAbstract {
	private String method;
	private ByteBuffer body = ByteBuffer.allocate(0);
	private String uri;
	private int[] version = new int[2];
	private StatusRequest status = StatusRequest.OK;
	private Map<String, String> params = new HashMap<String, String>();
	private Map<String, String> headers = new HashMap<String, String>();

	public void addHeader(String header, String value) {
		headers.put(header, value);
	}

	public void setVersion(int[] version) {
		this.version[0] = version[0];
		this.version[1] = version[1];
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public void setParams(Map<String, String> params) {
		this.params.putAll(params);
	}

	public void setBody(ByteBuffer buffer) {
		if (headers.containsKey("content-length")) {
			this.body = ByteBuffer.allocate(buffer.limit());
			buffer.flip();
			this.body.put(buffer);
		}
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public boolean bodyEnable() {
		if (headers.containsKey("content-length")) {
			return true;
		}
		return false;
	}

	public boolean validVersion(int[] version) {
		if (version[0] != 1 || version[1] != 1 && version[1] != 0) {
			status = StatusRequest.VERSION_NOT_SUPPORTED;
			return false;
		}
		return true;
	}

	public String getHeader(String key) {
		return headers.get(key);
	}

	public void setStatus(StatusRequest status) {
		this.status = status;
	}

	public String getHost() {
		return headers.get("host");
	}

	public String getMethod() {
		return this.method;
	}

	public ByteBuffer getBody() {
		return body;
	}

	public String getUri() {
		return uri;
	}

	public int[] getVersion() {
		return version;
	}

	public StatusRequest getStatus() {
		return status;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}
}
