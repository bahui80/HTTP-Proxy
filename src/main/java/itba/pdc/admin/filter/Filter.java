package itba.pdc.admin.filter;

import java.nio.ByteBuffer;

public interface Filter {
	public void doFilter(ByteBuffer buffer);
}
