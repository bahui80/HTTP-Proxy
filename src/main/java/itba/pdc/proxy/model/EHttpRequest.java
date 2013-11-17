package itba.pdc.proxy.model;

import itba.pdc.admin.filter.FilterStatus;
import itba.pdc.proxy.lib.ReadProxyConfiguration;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;

public class EHttpRequest extends HttpRequestAbstract implements HttpMessage {

	private static final Set<String> supportedMethods = createMethods();
	private static final Set<String> supportedHeaders = createHeaders();
	private FilterStatus filterStatus = FilterStatus.NO_STATUS;
	private int port = 80;

	private static Set<String> createMethods() {
		Set<String> headers = new HashSet<String>();
		headers.add("GET");
		headers.add("POST");
		return headers;
	}

	private static Set<String> createHeaders() {
		Set<String> headers = new HashSet<String>();
		headers.add("accept");
		headers.add("authorization");
		headers.add("date");
		headers.add("host");
		headers.add("histogram");
		headers.add("content-length");
		return headers;
	}

	@Override
	public void setUri(String uri) {
		super.setUri(uri);
		if (uri.equals("/bytes")) {
			super.setStatus(StatusRequest.BYTES);
		} else if (uri.equals("/histogram")) {
			super.setStatus(StatusRequest.HISTOGRAM);
		} else if (uri.equals("/status")) {
			super.setStatus(StatusRequest.STATUS);
		} else if (uri.equals("/accesses")) {
			super.setStatus(StatusRequest.ACCESSES);
		} else if (uri.equals("/transformer")
				&& super.getMethod().equals("POST")) {
			super.setStatus(StatusRequest.FILTER);
			filterStatus = FilterStatus.TRANSFORMER;
		}
	}

	@Override
	public void setParams(Map<String, String> params) {
		super.setParams(params);
		for (Entry<String, String> p : params.entrySet()) {
			StatusRequest status = super.getStatus();
			if (status.equals(StatusRequest.HISTOGRAM)) {
				if (p.getKey().equals("code")) {
					addHeader("histogram", p.getValue());
				}
			}
			// else if (status.equals(StatusRequest.FILTER)) {
			// if (p.getKey().equals("transformer")) {
			// addHeader("filter", p.getValue());
			// filterStatus = FilterStatus.TRANSFORMER;
			// }
			// }
		}
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
					// super.addHeader(header, value.substring(0, idx));
				}
				// else {
				// super.addHeader(header, value);
				// }
			}
			super.addHeader(header, value);
		}
	}

	public boolean validMethod(String method) {
		if (supportedMethods.contains(method)) {
			return true;
		}
		super.setStatus(StatusRequest.METHOD_NOT_ALLOWED);
		// status = StatusRequest.METHOD_NOT_ALLOWED;
		return false;
	}

	public void setStatusRequest(StatusRequest status) {
		super.setStatus(status);
	}

	public Integer getPort() {
		return port;
	}

	public FilterStatus getFilterStatus() {
		return filterStatus;
	}

	public boolean validUser() {
		String user = getHeader("authorization");
		if (user == null) {
			return false;
		}
		String[] params = user.split(" ");
		if (params.length != 2 || !params[0].toLowerCase().equals("basic")) {
			return false;
		}
		byte[] decoded = Base64.decodeBase64(params[1]);
		String[] authentication = new String(decoded).split(":");
		if (authentication.length != 2) {
			return false;
		}
		
		ReadProxyConfiguration prop = ReadProxyConfiguration.getInstance();
		if (!prop.getAdminUsername().equals(authentication[0].trim())
				|| !prop.getAdminPassword()
						.equals(authentication[1].trim())) {
			return false;
		}
		return true;
	}
}
