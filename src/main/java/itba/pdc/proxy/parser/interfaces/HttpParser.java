package itba.pdc.proxy.parser.interfaces;

import itba.pdc.proxy.parser.enums.ParserCode;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface HttpParser {
	public ParserCode parseMessage(ByteBuffer buff) throws IOException;

	public String getState();

	public ByteBuffer getBuffer();
}
