package itba.pdc.admin.filter;

import itba.pdc.admin.MetricManager;
import itba.pdc.proxy.lib.ReadConstantsConfiguration;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * @author jbuireo
 * 
 *         Change some bytes of the body for some other. In this version change
 *         a->4, e->3, i->1, o->0 and c-><
 */
public class TransformationFilter implements Filter {

	private static TransformationFilter instance = null;
	private String id = "transformer";
	private Map<Integer, Integer> bytesTransformations = null;
	private MetricManager metricManager = MetricManager.getInstance();

	private TransformationFilter() {
		if (instance != null) {
			throw new IllegalAccessError("This class is already isntantiated");
		}
		bytesTransformations = new ConcurrentHashMap<Integer, Integer>();
		ReadConstantsConfiguration prop = ReadConstantsConfiguration.getInstance();
		bytesTransformations.put(prop.getA_Byte(), prop.get4_Byte());
		bytesTransformations.put(prop.getE_Byte(), prop.get3_Byte());
		bytesTransformations.put(prop.getI_Byte(), prop.get1_Byte());
		bytesTransformations.put(prop.getO_Byte(), prop.get0_Byte());
		bytesTransformations.put(prop.getC_Byte(), prop.getLess_Byte());
	}

	public static TransformationFilter getInstace() {
		if (instance == null) {
			instance = new TransformationFilter();
		}
		return instance;
	}

	public void doFilter(ByteBuffer buffer) {
		byte b;
		int position = buffer.position();
		buffer.flip();
		for (int i = 0; i < buffer.limit(); i++) {
			b = buffer.get(i);
			Integer byteChanged = bytesTransformations.get(new Integer(b));
			if (byteChanged != null) {
				metricManager.addBytesChanged(1);
				buffer.put(i, byteChanged.byteValue());
			}
//			switch (b) {
//			case 97:
//				buffer.put(i, (byte) 52);
//				break;
//			case 101:
//				buffer.put(i, (byte) 51);
//				break;
//			case 105:
//				buffer.put(i, (byte) 49);
//				break;
//			case 111:
//				buffer.put(i, (byte) 48);
//				break;
//			case 99:
//				buffer.put(i, (byte) 60);
//				break;
//			}
		}
		buffer.position(position);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		TransformationFilter t = (TransformationFilter) obj;
		return id.equals(t.id);
	}
	
}
