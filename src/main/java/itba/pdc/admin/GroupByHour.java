package itba.pdc.admin;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class GroupByHour implements GroupMetrics {

	public Map<String, String> group(List<Date> data) {
		Map<String, String> map_data = new TreeMap<String, String>(
				new Comparator<String>() {
					public int compare(String o1, String o2) {
						Integer i1 = null;
						Integer i2 = null;
						try{
							i1 = Integer.valueOf(o1);
							i2 = Integer.valueOf(o2);
						}catch(NumberFormatException ex){
							throw new IllegalStateException();
						}
						return i1.compareTo(i2);
					}
				});
		final String[] keys = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
				"10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
				"20", "21", "22", "23" };
		
		for (String k : keys) {
			map_data.put(k,"0");
		}
		
		for (Date d : data) {
			int h = d.getHours();
			map_data.put(keys[h], String.valueOf(Integer.valueOf(map_data
					.get(keys[h])) + 1));
		}
		
		return map_data;
	}

}
