package itba.pdc.proxy.lib;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

public final class ManageByteBuffer {
	private static Charset charset = Charset.forName("UTF-8");
	private static CharsetEncoder encoder = charset.newEncoder();
	private static CharsetDecoder decoder = charset.newDecoder();
	private static Integer cr = ReadConstantsConfiguration.getInstance()
			.getCR();
	private static Integer lf = ReadConstantsConfiguration.getInstance()
			.getLF();
	private static Logger parserLogger = (Logger) LoggerFactory.getLogger("parser.log");

	private ManageByteBuffer() {
		parserLogger.error("The class ManageByteBuffer cannot be instantiated");
		throw new IllegalAccessError("This class cannot be instantiated");
	}

	public static ByteBuffer encode(String message) {
		try {
			return encoder.encode(CharBuffer.wrap(message));
		} catch (Exception e) {
			parserLogger.error("Problem encoding a string message into bytebuffer");
		}
		return null;
	}

	public static String decode(ByteBuffer buffer) {
		String data = "";
		try {
			int old_position = buffer.position();
			data = decoder.decode(buffer).toString();
			buffer.position(old_position);
		} catch (Exception e) {
			parserLogger.error("Problem decoding a bytebuffer into string message");
			return "";
		}
		return data;
	}

	/**
	 * @author mpurita
	 * 
	 * @brief Parse a line at byte level
	 * 
	 * @return The first line of the buffer that have \r\n or \n otherwise
	 *         return null
	 */
	public static String readLine(ByteBuffer buffer) {
		boolean crFlag = false;
		boolean lfFlag = false;
		if (buffer.limit() == 0) {
			return null;
		}
		byte[] array = new byte[buffer.limit()];
		int i = 0;
		byte b;
		buffer.flip();
		do {
			b = buffer.get();
			array[i++] = b;
			if (b == cr) {
				crFlag = true;
			} else if (b == lf) {
				lfFlag = true;
			}
		} while (buffer.hasRemaining() && !crFlag && !lfFlag);
		if (!crFlag && !lfFlag) {
			return null;
		} else {
			if (crFlag) {
				if (buffer.limit() == 0 || buffer.limit() == buffer.position()) {
					return null;
				}
				b = buffer.get();
				if (b != lf) {
					return null;
				}
				array[i] = b;
			}
			buffer.compact();
			int position = buffer.position();
			buffer.limit(position);
			return new String(array).trim();
		}
	}
}
