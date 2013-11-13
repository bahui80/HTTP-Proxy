package itba.pdc.proxy.lib;

import itba.pdc.proxy.parser.enums.ParserCode;
import itba.pdc.proxy.parser.interfaces.HttpParser;

import java.io.IOException;
import java.nio.ByteBuffer;

public final class ManageParser {

	private ManageParser() {
		throw new IllegalAccessError("This class cannot be instantiated");
	}

	public static ReadingState parse(HttpParser parser, ByteBuffer buff) {
		ParserCode code = ParserCode.INVALID;
		try {
			ByteBuffer preparedBuffer = ByteBuffer.allocate(buff.capacity());
			buff.flip();
			preparedBuffer.put(buff);
			buff.compact();
			code = parser.parseMessage(preparedBuffer);
		} catch (IOException e) {
			return ReadingState.ERROR;
		}
		switch (code) {
		case INVALID:
			return ReadingState.ERROR;
		case VALID:
			return ReadingState.FINISHED;
		default:
			return ReadingState.UNFINISHED;
		}
	}
}
