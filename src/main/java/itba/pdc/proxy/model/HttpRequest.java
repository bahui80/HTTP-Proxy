package itba.pdc.proxy.model;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class HttpRequest extends HttpRequestAbstract implements HttpMessage {

	private static final Set<String> supportedMethods = createMethods();
	private static final Set<String> supportedHeaders = createHeaders();
	private Logger accessLogger = (Logger) LoggerFactory.getLogger("access.log");
	private int port = 80;

	protected static Set<String> createHeaders() {
		Set<String> headers = new HashSet<String>();
		headers.add("accept");
		// headers.add("Accept-Charset");
		// headers.add("Accept-Encoding");
		headers.add("accept-Language");
		// headers.add("Accept-Datetime");
		// headers.add("Authorization");
//		headers.add("cache-control");
		headers.add("connection");
		headers.add("cookie");
		headers.add("content-length");
		// headers.add("Content-MD5");
		headers.add("content-type");
		headers.add("date");
		headers.add("expect");
		headers.add("from");
		headers.add("host");
		headers.add("proxy-connection");
		// headers.add("If-Match");
		// headers.add("If-None-Match");
		// headers.add("If-Modified-Since");
		// headers.add("If-Range");
		// headers.add("Max-Forwards");
		// headers.add("If-Unmodified-Since");
		 headers.add("origin");
		headers.add("pragma");
		// headers.add("Proxy-Authorization");
		// headers.add("Range");
		headers.add("referer");
		// headers.add("TE");
		// headers.add("Upgrade");
		headers.add("user-Agent");
		// headers.add("Via");
		// headers.add("Warning");

		return headers;
	}

	protected static Set<String> createMethods() {
		Set<String> headers = new HashSet<String>();
		headers.add("GET");
		headers.add("POST");
		headers.add("HEAD");
		return headers;
	}

	@Override
	public void addHeader(String header, String value) {
		if (supportedHeaders.contains(header)) {
			if (header.equals("host")) {
				int idx = value.indexOf(":");
				int length = value.length();
				if (idx > 0) {
					port = Integer.parseInt(value.substring(idx + 1, length));
					value = value.substring(0, idx);
				}
			}
			super.addHeader(header, value);
		}
	}

	public ByteBuffer getStream() {
		final StringBuilder builder = new StringBuilder();
		String uri = super.getUri();
		if (uri.charAt(0) == 'h') {
			int j = 0;
			int n = uri.length();
			int i;
			for (i = 0; i < n && j != 3; i++) {
				if (uri.charAt(i) == '/') {
					j++;
				}
			}
			uri = uri.substring(i - 1, n);
		}
		builder.append(super.getMethod()).append(" ").append(uri);
		if (!super.getParams().isEmpty()) {
			builder.append("?");
		}
		int n = super.getParams().size();
		int paramsNumber = 1;
		for (Entry<String, String> param : super.getParams().entrySet()) {
			builder.append(param.getKey()).append("=")
					.append(param.getValue().replace(" ", "+"));
			if (paramsNumber != n) {
				builder.append("&");
			}
			paramsNumber++;
		}
		builder.append(" HTTP/").append(super.getVersion()[0]).append(".")
				.append(super.getVersion()[1]).append("\n");

		for (Entry<String, String> entry : super.getHeaders().entrySet()) {
			if (!entry.getKey().contains("encoding")) {
				builder.append(entry.getKey()).append(": ")
						.append(entry.getValue());
				if (entry.getKey().equals("host") && port != 80) {
					builder.append(":").append(port);
				}
				builder.append("\n");
			}
		}
		builder.append("\n");
		final String head = builder.toString();
		StringBuilder build = new StringBuilder();
		accessLogger.info(build.append("Send to origin server: ").append(head).toString());
		ByteBuffer body = super.getBody();
		ByteBuffer buff = ByteBuffer.allocate(head.getBytes().length
				+ body.position());
		buff.put(head.getBytes());
		body.flip();
		buff.put(body);
		return buff;
	}

	public boolean validMethod(String method) {
		if (supportedMethods.contains(method)) {
			return true;
		}
		super.setStatus(StatusRequest.METHOD_NOT_ALLOWED);
		return false;
	}

	public void setStatusRequest(StatusRequest status) {
		super.setStatus(status);
	}

	public Integer getPort() {
		return port;
	}
}
