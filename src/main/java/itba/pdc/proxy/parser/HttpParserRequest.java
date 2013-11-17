package itba.pdc.proxy.parser;

import itba.pdc.proxy.lib.ManageByteBuffer;
import itba.pdc.proxy.model.HttpMessage;
import itba.pdc.proxy.model.StatusRequest;
import itba.pdc.proxy.parser.enums.ParserCode;
import itba.pdc.proxy.parser.enums.ParserState;
import itba.pdc.proxy.parser.interfaces.HttpParser;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * Parse with a state machine the full request. This parser use ByteBuffer
 * instead of String
 * 
 * @author mpurita
 * 
 */
public class HttpParserRequest implements HttpParser {
	private Logger parserLogger = (Logger) LoggerFactory
			.getLogger("parser.log");
	private HttpMessage request;
	private ParserState state;
	private ByteBuffer buffer;

	public HttpParserRequest(final HttpMessage request) {
		this.request = request;
		this.state = ParserState.METHOD;
		this.buffer = ByteBuffer.allocate(0);
	}

	public final ParserCode parseMessage(ByteBuffer buff) throws IOException {
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
			// throw new InvalidParserState();
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
		String prms[], cmd[], temp[];
		int idx, i, version[] = { 0, 0 };
		String line = ManageByteBuffer.readLine(this.buffer);
		Map<String, String> params = null;

		if (line == null) {
			return ParserCode.LOOP;
		}

		cmd = line.split("\\s");

		if (cmd.length != 3) {
			request.setStatusRequest(StatusRequest.BAD_REQUEST);
			return ParserCode.INVALID;
		}

		if (cmd[2].toUpperCase().indexOf("HTTP/") == 0
				&& cmd[2].indexOf('.') > 5) {
			temp = cmd[2].substring(5).split("\\.");
			try {
				version[0] = Integer.parseInt(temp[0]);
				version[1] = Integer.parseInt(temp[1]);
				if (!request.validVersion(version)) {
					parserLogger.error("Request: Invalid http version");
					return ParserCode.INVALID;
				}
			} catch (NumberFormatException nfe) {
				request.setStatusRequest(StatusRequest.BAD_REQUEST);
				parserLogger.error("Request: The version of http must be a number");
				return ParserCode.INVALID;
			}
		} else {
			parserLogger
			.error("Request: The protocol name mas be HTTP/ and the version (HTTP/1.1, HTTP/1.0)");
			request.setStatusRequest(StatusRequest.BAD_REQUEST);
			return ParserCode.INVALID;
		}

		if (request.validMethod(cmd[0])) {
			request.setMethod(cmd[0]);
			String uri;
			idx = cmd[1].indexOf('?');
			if (idx < 0) {
				uri = cmd[1];
			} else {
				uri = URLDecoder.decode(cmd[1].substring(0, idx), "ISO-8859-1");
				prms = cmd[1].substring(idx + 1).split("&");
				params = new HashMap<String, String>();
				for (i = 0; i < prms.length; i++) {
					temp = prms[i].split("=");
					if (temp.length == 2) {
						params.put(URLDecoder.decode(temp[0], "ISO-8859-1"),
								URLDecoder.decode(temp[1], "ISO-8859-1"));
					} else if (temp.length == 1
							&& prms[i].indexOf('=') == prms[i].length() - 1) {
						params.put(URLDecoder.decode(temp[0], "ISO-8859-1"), "");
					}
				}
			}
			request.setUri(uri);
			if (params != null) {
				request.setParams(params);
			}
		} else {
			return ParserCode.INVALID;
		}
		request.setVersion(version);
		this.state = ParserState.HEADERS;
		return ParserCode.CONTINUE;
	}

	private ParserCode parseHeaders() throws IOException {
		int idx;

		String line = ManageByteBuffer.readLine(this.buffer);
		;
		if (line == null) {
			return ParserCode.LOOP;
		}
		while (!line.trim().equals("")) {
			idx = line.indexOf(':');
			if (idx < 0) {
				parserLogger.error("Request: The header field is not well formed");
				request.setStatusRequest(StatusRequest.CONFLICT);
				return ParserCode.INVALID;
			} else {
				String headerType = line.substring(0, idx).toLowerCase();
				String headerValue = line.substring(idx + 1).trim();
				request.addHeader(headerType, headerValue);
			}
			line = ManageByteBuffer.readLine(this.buffer);
			;
			if (line == null) {
				return ParserCode.LOOP;
			}
		}

		if (request.getHost() == null) {
			request.setStatusRequest(StatusRequest.MISSING_HOST);
			return ParserCode.INVALID;
		}
		if (!request.bodyEnable()) {
			if(request.getMethod().equals("POST")){
				request.setStatusRequest(StatusRequest.LENGTH_REQUIRED);
				return ParserCode.INVALID;
			}
			state = ParserState.END;
			return ParserCode.VALID;
		} else {
			state = ParserState.DATA;
			return ParserCode.CONTINUE;
		}
	}

	private ParserCode parseData() {
		Integer bytes = Integer.parseInt(request.getHeader("content-length"));
		if (!readBuffer(bytes)) {
			return ParserCode.LOOP;
		}
		request.setBody(this.buffer);
		this.state = ParserState.END;
		return ParserCode.VALID;
	}

	private boolean readBuffer(Integer contentLength) {
		if (this.buffer.capacity() >= contentLength) {
			return true;
		}
		return false;
	}

	public String getState() {
		return this.state.toString();
	}

	public ByteBuffer getBuffer() {
		return this.buffer;
	}

}
