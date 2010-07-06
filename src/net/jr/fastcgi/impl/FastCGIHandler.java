/*
 * (c) 2009 Julien Rialland, and the jFastCGI project developpers.
 * 
 * Released under BSD License (see license.txt)
 *  
 *   $Id$ 
 */
package net.jr.fastcgi.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import net.jr.fastcgi.ConnectionFactory;
import net.jr.utils.logging.StreamLogger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 */
public class FastCGIHandler {

	public ConnectionFactory getConnectionFactory() {
		return connectionFactory;
	}

	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	private static final long serialVersionUID = 7733797250176758803L;

	private static final Log log = LogFactory.getLog(FastCGIHandler.class);

	private static final int FCGI_BEGIN_REQUEST = 1;

	private static final int FCGI_END_REQUEST = 3;

	private static final int FCGI_PARAMS = 4;

	private static final int FCGI_STDIN = 5;

	private static final int FCGI_STDOUT = 6;

	private static final int FCGI_STDERR = 7;

	private static final int FCGI_RESPONDER = 1;

	private static final int FCGI_VERSION = 1;

	private static final int FCGI_KEEP_CONN = 1;

	private static final int FCGI_REQUEST_COMPLETE = 0;

	private ConnectionFactory connectionFactory;

	private static final long READ_TIMEOUT = 120000;

	private Process process;

	private Thread processLogThread;

	private boolean keepAlive = false;
	
	public void startProcess(String cmd) throws IOException {
		ProcessBuilder pb = new ProcessBuilder(cmd.split(" "));
		process = pb.start();
		
		if(log.isTraceEnabled())
		{
		
			processLogThread = new Thread(new StreamLogger(
					process.getErrorStream(), log));
			
			processLogThread.setDaemon(true);
			
			processLogThread.start();
		}
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		destroy();
	}

	public void service(RequestAdapter request, ResponseAdapter response)
			throws ServletException, IOException {

		OutputStream out = response.getOutputStream();

		Socket fcgiSocket = connectionFactory.getConnection();
		fcgiSocket.setSoTimeout((int) READ_TIMEOUT);

		try {
			synchronized(fcgiSocket){
				handleRequest(request, response, fcgiSocket, out, keepAlive);
			}
		} finally {
			if (fcgiSocket != null)
				connectionFactory.releaseConnection(fcgiSocket);
			fcgiSocket.close();
		}
	}

	private boolean handleRequest(RequestAdapter req, ResponseAdapter res,
			Socket fcgiSocket, OutputStream out, boolean keepalive)
			throws ServletException, IOException {
		OutputStream ws = fcgiSocket.getOutputStream();

		writeHeader(fcgiSocket, ws, FCGI_BEGIN_REQUEST, 8);

		int role = FCGI_RESPONDER;

		ws.write(role >> 8);
		ws.write(role);
		ws.write(keepalive ? FCGI_KEEP_CONN : 0); // flags
		for (int i = 0; i < 5; i++)
			ws.write(0);

		setEnvironment(fcgiSocket, ws, req);

		InputStream in = req.getInputStream();
		byte[] buf = new byte[4096];
		int len = buf.length;
		int sublen;

		writeHeader(fcgiSocket, ws, FCGI_PARAMS, 0);

		boolean hasStdin = false;
		while ((sublen = in.read(buf, 0, len)) > 0) {
			hasStdin = true;
			writeHeader(fcgiSocket, ws, FCGI_STDIN, sublen);
			ws.write(buf, 0, sublen);
		}

		if (hasStdin)
			writeHeader(fcgiSocket, ws, FCGI_STDIN, 0);

		FastCGIInputStream is = new FastCGIInputStream(fcgiSocket);

		int ch = parseHeaders(res, is);

		if (ch >= 0)
			out.write(ch);

		while ((ch = is.read()) >= 0)
			out.write(ch);

		return !is.isDead() && keepalive;
	}

	private void setEnvironment(Socket fcgi, OutputStream ws, RequestAdapter req)
			throws IOException {
		addHeader(fcgi, ws, "REQUEST_URI", req.getRequestURI());
		addHeader(fcgi, ws, "REQUEST_METHOD", req.getMethod());
		addHeader(fcgi, ws, "SERVER_SOFTWARE", FastCGIHandler.class.getName());
		addHeader(fcgi, ws, "SERVER_NAME", req.getServerName());
		addHeader(fcgi, ws, "SERVER_PORT", String.valueOf(req.getServerPort()));
		addHeader(fcgi, ws, "REMOTE_ADDR", req.getRemoteAddr());
		addHeader(fcgi, ws, "REMOTE_HOST", req.getRemoteAddr());
		if (req.getRemoteUser() != null)
			addHeader(fcgi, ws, "REMOTE_USER", req.getRemoteUser());
		else
			addHeader(fcgi, ws, "REMOTE_USER", "");
		if (req.getAuthType() != null)
			addHeader(fcgi, ws, "AUTH_TYPE", req.getAuthType());

		addHeader(fcgi, ws, "GATEWAY_INTERFACE", "CGI/1.1");
		addHeader(fcgi, ws, "SERVER_PROTOCOL", req.getProtocol());
		if (req.getQueryString() != null)
			addHeader(fcgi, ws, "QUERY_STRING", req.getQueryString());
		else
			addHeader(fcgi, ws, "QUERY_STRING", "");

		String scriptPath = req.getServletPath();
		log.debug("FCGI file: " + scriptPath);
		addHeader(fcgi, ws, "PATH_INFO", req.getContextPath() + scriptPath);
		addHeader(fcgi, ws, "PATH_TRANSLATED", req.getRealPath(scriptPath));
		addHeader(fcgi, ws, "SCRIPT_FILENAME", req.getRealPath(scriptPath));
		int contentLength = req.getContentLength();
		if (contentLength < 0)
			addHeader(fcgi, ws, "CONTENT_LENGTH", "0");
		else
			addHeader(fcgi, ws, "CONTENT_LENGTH", String.valueOf(contentLength));

		addHeader(fcgi, ws, "DOCUMENT_ROOT", req.getRealPath("/"));

		String cb = "";

		Enumeration<String> e = req.getHeaderNames();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			String value = req.getHeader(key);

			if (key.equalsIgnoreCase("content-length"))
				addHeader(fcgi, ws, "CONTENT_LENGTH", value);
			else if (key.equalsIgnoreCase("content-type"))
				addHeader(fcgi, ws, "CONTENT_TYPE", value);
			else if (key.equalsIgnoreCase("if-modified-since")) {
			} else if (key.equalsIgnoreCase("if-none-match")) {
			} else if (key.equalsIgnoreCase("authorization")) {
			} else if (key.equalsIgnoreCase("proxy-authorization")) {
			} else
				addHeader(fcgi, ws, convertHeader(cb, key), value);
		}
	}

	private String convertHeader(String cb, String key) {

		cb += "HTTP_";

		for (int i = 0; i < key.length(); i++) {
			char ch = key.charAt(i);
			if (ch == '-')
				cb += "_";
			else if (ch >= 'a' && ch <= 'z')
				cb += new Character((char) (ch + 'A' - 'a')).toString();
			else
				cb += new Character(ch);
		}

		return cb;
	}

	private int parseHeaders(ResponseAdapter res, InputStream is)
			throws IOException {
		String key = "";
		String value = "";

		int ch = is.read();

		if (ch < 0) {
			log.error("Can't contact FastCGI");
			res.sendError(HttpServletResponse.SC_BAD_GATEWAY);
			return -1;
		}

		while (ch >= 0) {
			key = "";
			value = "";

			for (; ch >= 0 && ch != ' ' && ch != '\r' && ch != '\n'
					&& ch != ':'; ch = is.read()) {
				key += new Character((char) ch).toString();
			}

			for (; ch >= 0 && ch == ' ' || ch == ':'; ch = is.read()) {
			}

			for (; ch >= 0 && ch != '\r' && ch != '\n'; ch = is.read()) {
				value += new Character((char) ch).toString();
			}

			if (ch == '\r') {
				ch = is.read();
				if (ch == '\n')
					ch = is.read();
			}

			if (key.length() == 0)
				return ch;

			if (log.isInfoEnabled())
				log.info("fastcgi:" + key + ": " + value);

			if (key.equalsIgnoreCase("status")) {
				int status = 0;
				int len = value.length();

				for (int i = 0; i < len; i++) {
					char digit = value.charAt(i);

					if ('0' <= digit && digit <= '9')
						status = 10 * status + digit - '0';
					else
						break;
				}

				res.setStatus(status);
			} else if (key.startsWith("http") || key.startsWith("HTTP")) {
			} else if (key.equalsIgnoreCase("location")) {
				res.sendRedirect(value.toString());
			} else
				res.addHeader(key.toString(), value.toString());
		}

		return ch;
	}

	private void addHeader(Socket fcgiSocket, OutputStream ws, String key,
			String value) throws IOException {
		
		if(value != null)
		{
		
			int keyLen = key.length();
			int valLen = value.length();
	
			int len = keyLen + valLen;
	
			if (keyLen < 0x80)
				len += 1;
			else
				len += 4;
	
			if (valLen < 0x80)
				len += 1;
			else
				len += 4;
	
			writeHeader(fcgiSocket, ws, FCGI_PARAMS, len);
	
			if (keyLen < 0x80)
				ws.write(keyLen);
			else {
				ws.write(0x80 | (keyLen >> 24));
				ws.write(keyLen >> 16);
				ws.write(keyLen >> 8);
				ws.write(keyLen);
			}
	
			if (valLen < 0x80)
				ws.write(valLen);
			else {
				ws.write(0x80 | (valLen >> 24));
				ws.write(valLen >> 16);
				ws.write(valLen >> 8);
				ws.write(valLen);
			}
	
			ws.write(key.getBytes());
			ws.write(value.getBytes());
		}
	}

	private void writeHeader(Socket fcgiSocket, OutputStream ws, int type,
			int length) throws IOException {
		int id = 1;
		int pad = 0;

		ws.write(FCGI_VERSION);
		ws.write(type);
		ws.write(id >> 8);
		ws.write(id);
		ws.write(length >> 8);
		ws.write(length);
		ws.write(pad);
		ws.write(0);
	}

	public void destroy() {
		if (process != null) {
			process.destroy();
			process = null;
		}
	}

	static class FastCGIInputStream extends InputStream {
		private Socket _fcgiSocket;

		private InputStream _is;
		private int _chunkLength;
		private int _padLength;
		private boolean _isDead;

		public FastCGIInputStream() {
		}

		public FastCGIInputStream(Socket fcgiSocket) throws IOException {
			init(fcgiSocket);
		}

		public void init(Socket fcgiSocket) throws IOException {
			_fcgiSocket = fcgiSocket;

			_is = fcgiSocket.getInputStream();
			_chunkLength = 0;
			_isDead = false;
		}

		public boolean isDead() {
			return _isDead;
		}

		public int read() throws IOException {
			do {
				if (_chunkLength > 0) {
					_chunkLength--;
					return _is.read();
				}
			} while (readNext());

			return -1;
		}

		private boolean readNext() throws IOException {
			if (_is == null)
				return false;

			if (_padLength > 0) {
				_is.skip(_padLength);
				_padLength = 0;
			}

			@SuppressWarnings("unused")
			int version;

			while ((version = _is.read()) >= 0) {
				int type = _is.read();

				@SuppressWarnings("unused")
				int id = (_is.read() << 8) + _is.read();
				int length = (_is.read() << 8) + _is.read();
				int padding = _is.read();
				_is.read();

				switch (type) {
				case FCGI_END_REQUEST: {
					int appStatus = ((_is.read() << 24) + (_is.read() << 16)
							+ (_is.read() << 8) + (_is.read()));
					int pStatus = _is.read();

					if (log.isDebugEnabled()) {
						log.debug(_fcgiSocket + ": FCGI_END_REQUEST(appStatus:"
								+ appStatus + ", pStatus:" + pStatus + ")");
					}

					if (appStatus != 0)
						_isDead = true;

					if (pStatus != FCGI_REQUEST_COMPLETE)
						_isDead = true;

					_is.skip(3);
					_is = null;
					return false;
				}

				case FCGI_STDOUT:
					if (log.isDebugEnabled()) {
						log.debug(_fcgiSocket + ": FCGI_STDOUT(length:"
								+ length + ", padding:" + padding + ")");
					}

					if (length == 0) {
						if (padding > 0)
							_is.skip(padding);

						break;
					} else {
						_chunkLength = length;
						_padLength = padding;
						return true;
					}

				case FCGI_STDERR:
					if (log.isDebugEnabled()) {
						log.debug(_fcgiSocket + ": FCGI_STDERR(length:"
								+ length + ", padding:" + padding + ")");
					}

					byte[] buf = new byte[length];
					_is.read(buf, 0, length);
					log.warn(new String(buf, 0, length));

					if (padding > 0)
						_is.skip(padding);
					break;

				default:
					log.warn(_fcgiSocket + ": Unknown Protocol(" + type + ")");

					_isDead = true;
					_is.skip(length + padding);
					break;
				}
			}

			_isDead = true;

			return false;
		}
	}

	public Process getProcess() {
		return process;
	}

	public Thread getProcessLogThread() {
		return processLogThread;
	}

	public boolean isKeepAlive() {
		return keepAlive;
	}

	public void setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;
	}
}
