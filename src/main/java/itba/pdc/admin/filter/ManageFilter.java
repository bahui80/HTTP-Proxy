package itba.pdc.admin.filter;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;


public class ManageFilter {
	private static ManageFilter instance = null;
	private ConcurrentLinkedQueue<Filter> filters = null;
	private Set<String> set;
	private ManageFilter() {
		if (instance != null) {
			throw new IllegalAccessError("This class is already isntantiated");
		}
		filters = new ConcurrentLinkedQueue<Filter>();
		set = new HashSet<String>();
	}

	public static ManageFilter getInstace() {
		if (instance == null) {
			instance = new ManageFilter();
		}
		return instance;
	}

	public void addOrRemoveFilter(FilterStatus filterStatus) {
		switch (filterStatus) {
		case TRANSFORMER:
			TransformationFilter f = TransformationFilter.getInstace();
			if (filters.contains(f)) {
				set.remove(FilterStatus.TRANSFORMER.toString());
				filters.remove(f);
			} else {
				set.add(FilterStatus.TRANSFORMER.toString());
				filters.add(f);
			}
			break;
		default:
			break;
		}
	}
	
	public void doFilters(ByteBuffer buffer) {
		for (Filter f : filters) {
			f.doFilter(buffer);
		}
	}
	
	public Set<String> getSet() {
		return set;
	}
}
