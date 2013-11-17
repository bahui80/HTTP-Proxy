package itba.pdc.proxy.parser;

import itba.pdc.proxy.lib.ManageByteBuffer;
import itba.pdc.proxy.model.HttpResponse;
import itba.pdc.proxy.parser.enums.ParserCode;
import itba.pdc.proxy.parser.enums.ParserState;
import itba.pdc.proxy.parser.interfaces.HttpParser;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class HttpParserResponse implements HttpParser {
	private Logger parserLogger = (Logger) LoggerFactory
			.getLogger("parser.log");
	private HttpResponse response;
	private ParserState state;
	private ByteBuffer buffer;
	private boolean connectionClose = false;
	private String method;
	private int currentLength = 0;

	public HttpParserResponse(HttpResponse response) {
		this.response = response;
		this.state = ParserState.METHOD;
		this.buffer = ByteBuffer.allocate(0);
	}

	/**
	 * 
	 * @author mpurita
	 * 
	 * @param Receive
	 *            the buffer that the socket read
	 * 
	 * @return A code that indicate if the parser is valid or invalid when the
	 *         request is finished or loop in case the parser need more data
	 *         from the socket or continue if finish one state and the need to
	 *         go to the next state and the buffer still have data
	 * 
	 */
	public ParserCode parseMessage(ByteBuffer buff) throws IOException {
		ParserCode code;
		concatBuffer(buff);
		switch (state) {
		case METHOD:
			code = parseMethod();
			if (code.equals(ParserCode.LOOP)
					|| !code.equals(ParserCode.CONTINUE)) {
				return code;
			}
		case HEADERS:
			code = parseHeaders();
			if (code.equals(ParserCode.LOOP)
					|| !code.equals(ParserCode.CONTINUE)) {
				return code;
			}
		case DATA:
			code = parseData();
			if (code.equals(ParserCode.LOOP)
					|| !code.equals(ParserCode.CONTINUE)) {
				return code;
			}
		case END:
			return ParserCode.VALID;
		default:
			return ParserCode.INVALID;
		}
	}

	private void concatBuffer(ByteBuffer buff) {
		ByteBuffer aux = ByteBuffer.allocate(buffer.position()
				+ buff.position());
		buff.flip();
		buffer.flip();
		aux.put(buffer);
		aux.put(buff);
		buffer = aux;
	}

	private ParserCode parseMethod() throws UnsupportedEncodingException {
		String cmd[], temp[];
		int version[] = { 0, 0 };
		String line = ManageByteBuffer.readLine(this.buffer);

		if (line == null) {
			return ParserCode.LOOP;
		}

		cmd = line.split("\\s");

		if (cmd[0].indexOf("HTTP/") == 0 && cmd[0].indexOf('.') > 5) {
			temp = cmd[0].substring(5).split("\\.");
			try {
				version[0] = Integer.parseInt(temp[0]);
				version[1] = Integer.parseInt(temp[1]);
				if (!response.validVersion(version)) {
					parserLogger.error("Response: Invalid http version");
					return ParserCode.INVALID;
				}
			} catch (NumberFormatException nfe) {
				parserLogger.error("Response: The version of http must be a number");
				return ParserCode.INVALID;
			}
		} else {
			parserLogger
					.error("Response: The protocol name mas be HTTP/ and the version (HTTP/1.1, HTTP/1.0)");
			return ParserCode.INVALID;
		}

		Integer code = Integer.parseInt(cmd[1]);
		response.setVersion(version);
		response.setCode(code);
		this.state = ParserState.HEADERS;
		return ParserCode.CONTINUE;
	}

	private ParserCode parseHeaders() throws IOException {
		int idx;

		String line = ManageByteBuffer.readLine(this.buffer);
		if (line == null) {
			return ParserCode.LOOP;
		}
		while (!line.trim().equals("")) {
			idx = line.indexOf(':');
			if (idx < 0) {
				parserLogger.error("Response: The header field is not well formed");
				return ParserCode.INVALID;
			} else {
				String headerType = line.substring(0, idx).toLowerCase();
				String headerValue = line.substring(idx + 1).trim();
				response.addHeader(headerType, headerValue);
			}
			line = ManageByteBuffer.readLine(this.buffer);
			if (line == null) {
				return ParserCode.LOOP;
			}
		}
		state = ParserState.DATA;
		return ParserCode.CONTINUE;
	}

	private ParserCode parseData() {
		if (method.equals("HEAD") || response.getStatusCode().equals(204)) {
			state = ParserState.END;
			return ParserCode.VALID;
		}
		String chunked = response.getHeader("transfer-encoding");
		if (chunked != null) {
			return manageChunked();
		} else if (response.bodyEnable()) {
			Integer bytes = Integer.parseInt(response
					.getHeader("content-length"));
			if (bytes >= 10000000) {
				currentLength += buffer.limit();
				ManageByteBuffer.writeToFile(buffer, response.toString());
				buffer = ByteBuffer.allocate(0);
				if (!readBuffer(currentLength, bytes)) {
					return ParserCode.LOOP;
				}
				response.readFromFile();
			} else {
				if (!readBuffer(bytes)) {
					return ParserCode.LOOP;
				}
				response.setBody(this.buffer);
			}
			this.state = ParserState.END;
			return ParserCode.VALID;
		} else {
			connectionClose = true;
			return ParserCode.LOOP;
		}
	}

	private ParserCode manageChunked() {
		Integer chunkedSize = response.getChunkSize();
		if (chunkedSize == null) {
			String hexa = ManageByteBuffer.readLine(buffer);
			if (hexa == null) {
				return ParserCode.LOOP;
			}
			Integer size = Integer.parseInt(hexa, 16);
			if (size == 0) {
				response.setBody(response.getChunkedBuffer());
				response.addHeader("content-length", response.getLength());
				response.removeHeader("transfer-encoding");
				state = ParserState.END;
				return ParserCode.VALID;
			}
			response.setChunkedSize(size);
		}
		return readChunck(buffer);
	}

	private ParserCode readChunck(ByteBuffer buffer) {
		boolean completed;
		do {
			if (response.isChunkComplete()) {
				String hexa = ManageByteBuffer.readLine(buffer);
				if (hexa == null) {
					return ParserCode.LOOP;
				}
				if (!hexa.trim().equals("")) {
					Integer size = Integer.parseInt(hexa, 16);
					if (size == 0) {
						response.setBody(response.getChunkedBuffer());
						response.addHeader("content-length",
								response.getLength());
						response.removeHeader("transfer-encoding");
						state = ParserState.END;
						return ParserCode.VALID;
					}
					response.setChunkedSize(size);
				}
			}
			completed = response.addChunkedBuffer(buffer);
		} while (completed);
		return ParserCode.LOOP;
	}

	private boolean readBuffer(Integer length, Integer contentLength) {
		if (length.equals(contentLength)) {
			return true;
		}
		return false;
	}
	
	private boolean readBuffer(Integer contentLength) {
		if (this.buffer.limit() == contentLength) {
			return true;
		}
		return false;
	}

	public String getState() {
		return this.state.toString();
	}

	public boolean isConnectionClose() {
		return connectionClose;
	}

	public ByteBuffer getBuffer() {
		return this.buffer;
	}

	public void setMethod(String method) {
		this.method = method;
	}
}
