package itba.pdc.proxy.model;

import java.nio.ByteBuffer;
import java.util.Map;

public interface HttpMessage {
	
	public void addHeader(String header, String value);
	public void setVersion(int[] version);
	public void setMethod(String method);
	public void setParams(Map<String, String> params);
	public void setBody(ByteBuffer buffer);
	public void setUri(String uri);
	public boolean bodyEnable();
	public boolean validVersion(int[] version);
	public String getHeader(String key);
	public String getHost();
	public boolean validMethod(String method);
	public StatusRequest getStatus();
	public void setStatusRequest(StatusRequest status);
	public Integer getPort();
}
