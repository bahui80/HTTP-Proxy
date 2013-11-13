package itba.pdc.admin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class JsonFormatter implements Formatter {
	
	public String format(Map<String, String> data) {
		final StringBuilder builder = new StringBuilder();
		String to_send = "{ ";
		String end = " }";
		
		Set<String> keys = data.keySet();
		int i = 0;
		builder.append(to_send);
		for (String k : keys) {
			builder.append("\"").append(k).append("\"").append(" : ")
			.append("\"").append(data.get(k)).append("\"");
			if(i++ != keys.size() -1)
				builder.append(", ");
		}
		builder.append(end);
		return builder.append("\n").toString();
	}
}
