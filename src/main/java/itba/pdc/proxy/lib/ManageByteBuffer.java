package itba.pdc.proxy.lib;

import itba.pdc.admin.MetricManager;
import itba.pdc.admin.filter.ManageFilter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
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
	private static Logger parserLogger = (Logger) LoggerFactory
			.getLogger("parser.log");

	private ManageByteBuffer() {
		parserLogger.error("The class ManageByteBuffer cannot be instantiated");
		throw new IllegalAccessError("This class cannot be instantiated");
	}

	public static ByteBuffer encode(String message) {
		try {
			return encoder.encode(CharBuffer.wrap(message));
		} catch (Exception e) {
			parserLogger
					.error("Problem encoding a string message into bytebuffer");
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
			parserLogger
					.error("Problem decoding a bytebuffer into string message");
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

	public static void writeToFile(ByteBuffer buffer, String filename) {
		try {
			FileOutputStream fo = new FileOutputStream(filename, true);
			FileChannel wChannel = fo.getChannel();

			buffer.flip();
			wChannel.write(buffer);
			wChannel.close();
			fo.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void readFromFile(SocketChannel channel, String filename) {
		try {
			FileInputStream fi = new FileInputStream(filename);
			FileChannel wChannel = fi.getChannel();

			ByteBuffer buffer = ByteBuffer.allocate(ReadConstantsConfiguration.getInstance().getBufferSize());
			while (wChannel.read(buffer) != -1) {
				ManageFilter.getInstace().doFilters(buffer);
				buffer.flip();
				do {
					if (channel.isOpen() && channel.isConnected()) {
						int bytesWritten = channel.write(buffer);
						MetricManager.getInstance().addBytesWritten(bytesWritten);
					} else {
						wChannel.close();
						fi.close();
						return;
					}
				} while (buffer.hasRemaining());
				buffer.clear();
			}
			wChannel.close();
			fi.close();
			File file = new File(filename);
			file.delete();
		} catch (IOException e) {
		}
	}
}
