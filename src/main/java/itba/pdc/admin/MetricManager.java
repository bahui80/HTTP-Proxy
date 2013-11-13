/*
 * MetricManager
 */

package itba.pdc.admin;

import itba.pdc.admin.filter.FilterStatus;
import itba.pdc.admin.filter.ManageFilter;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class MetricManager {
	private static final MetricManager instance = new MetricManager();

	private int accesses = 0;
	private long bytesRead = 0;
	private long bytesWritten = 0;
	private long bytesChanged = 0;
	private ConcurrentMap<Integer, List<Date>> to_histogram;

	private MetricManager() {
		if (instance != null)
			throw new IllegalStateException();
		to_histogram = new ConcurrentHashMap<Integer, List<Date>>();
	}

	public static MetricManager getInstance() {
		return instance;
	}

	public void addAccess() {
		this.accesses++;
	}

	public void addBytesRead(long qty) {
		bytesRead += qty;
	}

	public void addBytesWritten(long qty) {
		bytesWritten += qty;
	}

	public void addBytesChanged(long qty) {
		bytesChanged += qty;
	}

	public void addStatusCode(Integer code) {
		List<Date> events = this.to_histogram.get(code);
		if (events == null)
			events = new ArrayList<Date>();
		events.add(new Date());
		this.to_histogram.put(code, events);
	}

	public String generateHistogram(Integer code, Formatter format,
			GroupMetrics groupby) {
		List<Date> list = this.to_histogram.get(code);
		if (list == null || list.isEmpty()) {
			return "{ \"0\" : \"0\", \"1\" : \"0\", \"2\" : \"0\", \"3\" : \"0\", \"4\" : \"0\", \"5\" : \"0\", \"6\" : \"0\", \"7\" : \"0\", \"8\" : \"0\", \"9\" : \"0\", \"10\" : \"0\", \"11\" : \"0\", \"12\" : \"0\", \"13\" : \"0\", \"14\" : \"0\", \"15\" : \"0\", \"16\" : \"0\", \"17\" : \"0\", \"18\" : \"0\", \"19\" : \"0\", \"20\" : \"0\", \"21\" : \"0\", \"22\" : \"0\", \"23\" : \"0\" }\n";
		}
		Map<String, String> to_format = groupby.group(this.to_histogram
				.get(code));
		return format.format(to_format);
	}

	public String generateBytes(Formatter format) {
		Map<String, String> data = new HashMap<String, String>();
		data.put("Bytes read", String.valueOf(bytesRead));
		data.put("Bytes written", String.valueOf(bytesWritten));
		data.put("Bytes total tranfered",
				String.valueOf(bytesRead + bytesWritten));
		data.put("Bytes changed", String.valueOf(bytesChanged));
		return format.format(data);
	}

	public String generateStatus(Formatter format) {
		Map<String, String> data = new HashMap<String, String>();
		Set<String> set = ManageFilter.getInstace().getSet();
		if (set.contains(FilterStatus.TRANSFORMER.toString())) {
			data.put(FilterStatus.TRANSFORMER.toString(), "Enabled");
		} else {
			data.put(FilterStatus.TRANSFORMER.toString(), "Disabled");
		}
		return format.format(data);
	}

	public String generateAccesses(Formatter format) {
		Map<String, String> data = new HashMap<String, String>();
		data.put("Accesses", String.valueOf(accesses));
		return format.format(data);
	}
}
