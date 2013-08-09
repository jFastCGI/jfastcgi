/*
 * (c) 2009-2012 Julien Rialland, and the jFastCGI project developpers.
 * 
 * Released under BSD License (see license.txt)
 *  
 *   $Id$ 
 */
package org.jfastcgi.fastcgi.impl;

import org.jfastcgi.fastcgi.ConnectionFactory;
import org.jfastcgi.utils.logging.StreamLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Enumeration;
import java.util.regex.Pattern;

/**
 * handling of the fastCGI protocol.
 */
public class FastCGIHandler {

    private static interface HeaderFilter {
        public boolean isFiltered(String header);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(FastCGIHandler.class);

    private static final int HTTP_ERROR_BAD_GATEWAY = 502;

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

    private static final long READ_TIMEOUT = 120000;

    private ConnectionFactory connectionFactory;

    private Process process;

    private Thread processLogThread;

    private boolean keepAlive = false;







    /**
     * by default, no header is filtered.
     */
    private HeaderFilter headerFilter = new HeaderFilter() {
        public boolean isFiltered(String header) {
            return false;
        }

        ;
    };

    private static Logger getLog() {
        return LOGGER;
    }

    /**
     * Some http headers have sometimes to be filtered for security reasons, so this methods allows to tell
     * which http headers we do not want to pass to the fastcgi app. For example :
     * <code>
     * handler.setFilteredHeaders(new String[]{"Authorization"});
     * </code>
     * <p/>
     * will remove all the HTTP_AUTHORIZATION headers from the transmitted requests.
     *
     * @param filteredHeaders an array of http header keys that will not be transmitted to
     *                        the fastcgi responder app.
     */
    public void setFilteredHeaders(String[] filteredHeaders) {

        if (filteredHeaders.length > 0) {
            StringBuffer regex = new StringBuffer();
            for (String header : filteredHeaders) {
                regex.append("|");
                regex.append(Pattern.quote(header));
            }

            getLog().trace("regular expression for filtered headers : " + regex);

            final Pattern pattern = Pattern.compile(regex.toString().substring(1), Pattern.CASE_INSENSITIVE);

            this.headerFilter = new HeaderFilter() {
                public boolean isFiltered(String header) {
                    return pattern.matcher(header).matches();
                }
            };
        }
    }

    /**
     * @param header the name of a http header (case insensitive)
     * @return true if the header should be filtered.
     */
    protected boolean isHeaderFiltered(String header) {
        return headerFilter.isFiltered(header);
    }

    public void startProcess(String cmd) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(cmd.split(" "));
        process = pb.start();

        if (getLog().isTraceEnabled()) {

            processLogThread = new Thread(new StreamLogger(process.getErrorStream(), getLog()));

            processLogThread.setDaemon(true);

            processLogThread.start();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        destroy();
    }

    public void service(RequestAdapter request, ResponseAdapter response) throws IOException {

        OutputStream out = response.getOutputStream();

        Socket fcgiSocket = connectionFactory.getConnection();
        fcgiSocket.setSoTimeout((int) READ_TIMEOUT);

        try {
            synchronized (fcgiSocket) {
                handleRequest(request, response, fcgiSocket, out, keepAlive);
            }
        }
        finally {
            if (fcgiSocket != null) {
                connectionFactory.releaseConnection(fcgiSocket);
            }
            fcgiSocket.close();
        }
    }

    private boolean handleRequest(RequestAdapter req, ResponseAdapter res, Socket fcgiSocket, OutputStream out, boolean keepalive) throws IOException {
        OutputStream ws = fcgiSocket.getOutputStream();

        writeHeader(ws, FCGI_BEGIN_REQUEST, 8);

        int role = FCGI_RESPONDER;

        ws.write(role >> 8);
        ws.write(role);
        ws.write(keepalive ? FCGI_KEEP_CONN : 0); // flags
        for (int i = 0; i < 5; i++) {
            ws.write(0);
        }

        setEnvironment(ws, req);

        InputStream in = req.getInputStream();
        byte[] buf = new byte[4096];
        int len = buf.length;
        int sublen;

        writeHeader(ws, FCGI_PARAMS, 0);

        boolean hasStdin = false;
        while ((sublen = in.read(buf, 0, len)) > 0) {
            hasStdin = true;
            writeHeader(ws, FCGI_STDIN, sublen);
            ws.write(buf, 0, sublen);
        }

        if (hasStdin) {
            writeHeader(ws, FCGI_STDIN, 0);
        }

        FastCGIInputStream is = new FastCGIInputStream(fcgiSocket);

        int ch = parseHeaders(res, is);

        if (ch >= 0) {
            out.write(ch);
        }

        while ((ch = is.read()) >= 0) {
            out.write(ch);
        }

        return !is.isDead() && keepalive;
    }

    private void setEnvironment(OutputStream ws, RequestAdapter req) throws IOException {
        addHeader(ws, "REQUEST_URI", req.getRequestURI());
        addHeader(ws, "REQUEST_METHOD", req.getMethod());
        addHeader(ws, "SERVER_SOFTWARE", FastCGIHandler.class.getName());
        addHeader(ws, "SERVER_NAME", req.getServerName());
        addHeader(ws, "SERVER_PORT", String.valueOf(req.getServerPort()));
        addHeader(ws, "REMOTE_ADDR", req.getRemoteAddr());
        addHeader(ws, "REMOTE_HOST", req.getRemoteAddr());
        if (req.getRemoteUser() != null) {
            addHeader(ws, "REMOTE_USER", req.getRemoteUser());
        }
        else {
            addHeader(ws, "REMOTE_USER", "");
        }
        if (req.getAuthType() != null) {
            addHeader(ws, "AUTH_TYPE", req.getAuthType());
        }
        addHeader(ws, "GATEWAY_INTERFACE", "CGI/1.1");
        addHeader(ws, "SERVER_PROTOCOL", req.getProtocol());

        if (req.getQueryString() != null) {
            addHeader(ws, "QUERY_STRING", req.getQueryString());
        }
        else {
            addHeader(ws, "QUERY_STRING", "");
        }

        String scriptPath = req.getServletPath();
        getLog().debug("FCGI file: " + scriptPath);
        addHeader(ws, "PATH_INFO", req.getContextPath() + scriptPath);
        addHeader(ws, "PATH_TRANSLATED", req.getRealPath(scriptPath));
        addHeader(ws, "SCRIPT_FILENAME", req.getRealPath(scriptPath));
        int contentLength = req.getContentLength();
        if (contentLength < 0) {
            addHeader(ws, "CONTENT_LENGTH", "0");
        }
        else {
            addHeader(ws, "CONTENT_LENGTH", String.valueOf(contentLength));
        }

        addHeader(ws, "DOCUMENT_ROOT", req.getRealPath("/"));

        Enumeration<String> e = req.getHeaderNames();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            String value = req.getHeader(key);

            if (!isHeaderFiltered(key)) {
                if (key.equalsIgnoreCase("content-length")) {
                    addHeader(ws, "CONTENT_LENGTH", value);
                }
                else if (key.equalsIgnoreCase("content-type")) {
                    addHeader(ws, "CONTENT_TYPE", value);
                }
                else {
                    addHeader(ws, convertHeader(key), value);
                }
            }
        }
    }

    private String convertHeader(String key) {
        StringBuffer sb = new StringBuffer("HTTP_");
        sb.append(key.toUpperCase().replace('-', '_'));
        return sb.toString();
    }

    private int parseHeaders(ResponseAdapter res, InputStream is) throws IOException {
        String key = "";
        String value = "";

        int ch = is.read();

        if (ch < 0) {
            getLog().error("Can't contact FastCGI");
            res.sendError(HTTP_ERROR_BAD_GATEWAY);
            return -1;
        }

        while (ch >= 0) {
            key = "";
            value = "";

            while (ch >= 0 && ch != ' ' && ch != '\r' && ch != '\n' && ch != ':') {
                key += Character.toString((char) ch);
                ch = is.read();
            }

            while (ch >= 0 && ch == ' ' || ch == ':') {
                ch = is.read();
            }

            while (ch >= 0 && ch != '\r' && ch != '\n') {
                value += Character.toString((char) ch);
                ch = is.read();
            }

            if (ch == '\r') {
                ch = is.read();
                if (ch == '\n') {
                    ch = is.read();
                }
            }

            if (key.length() == 0) {
                return ch;
            }

            if (getLog().isInfoEnabled()) {
                getLog().info("fastcgi:" + key + ": " + value);
            }

            if (key.equalsIgnoreCase("status")) {
                int status = 0;
                int len = value.length();

                for (int i = 0; i < len; i++) {
                    char digit = value.charAt(i);

                    if ('0' <= digit && digit <= '9') {
                        status = 10 * status + digit - '0';
                    }
                    else {
                        break;
                    }
                }

                res.setStatus(status);
            }
            else if (key.equalsIgnoreCase("location")) {
                res.sendRedirect(value);
            }
            else {
                res.addHeader(key, value);
            }
        }

        return ch;
    }

    private void addHeader(OutputStream ws, String key, String value) throws IOException {

        if (value != null) {

            int keyLen = key.length();
            int valLen = value.length();

            int len = keyLen + valLen;

            if (keyLen < 0x80) {
                len += 1;
            }
            else {
                len += 4;
            }

            if (valLen < 0x80) {
                len += 1;
            }
            else {
                len += 4;
            }

            writeHeader(ws, FCGI_PARAMS, len);

            if (keyLen < 0x80) {
                ws.write(keyLen);
            }
            else {
                ws.write(0x80 | (keyLen >> 24));
                ws.write(keyLen >> 16);
                ws.write(keyLen >> 8);
                ws.write(keyLen);
            }

            if (valLen < 0x80) {
                ws.write(valLen);
            }
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

    private void writeHeader(OutputStream ws, int type, int length) throws IOException {
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
            }
            while (readNext());

            return -1;
        }

        private boolean readNext() throws IOException {
            if (_is == null) {
                return false;
            }

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
                    case FCGI_END_REQUEST:
                        return fcgi_end_request();

                    case FCGI_STDOUT:
                        if (getLog().isDebugEnabled()) {
                            getLog().debug(_fcgiSocket + ": FCGI_STDOUT(length:" + length + ", padding:" + padding + ")");
                        }

                        if (length == 0) {
                            if (padding > 0) {
                                _is.skip(padding);
                            }

                            break;
                        }
                        else {
                            _chunkLength = length;
                            _padLength = padding;
                            return true;
                        }

                    case FCGI_STDERR:
                        if (getLog().isDebugEnabled()) {
                            getLog().debug(_fcgiSocket + ": FCGI_STDERR(length:" + length + ", padding:" + padding + ")");
                        }

                        byte[] buf = new byte[length];
                        _is.read(buf, 0, length);
                        getLog().warn(new String(buf, 0, length));

                        if (padding > 0) {
                            _is.skip(padding);
                        }
                        break;

                    default:
                        getLog().warn(_fcgiSocket + ": Unknown Protocol(" + type + ")");

                        _isDead = true;
                        _is.skip(length + padding);
                        break;
                }
            }

            _isDead = true;

            return false;
        }

        private boolean fcgi_end_request() throws IOException {
            int appStatus = ((_is.read() << 24) + (_is.read() << 16) + (_is.read() << 8) + (_is.read()));
            int pStatus = _is.read();

            if (getLog().isDebugEnabled()) {
                getLog().debug(_fcgiSocket + ": FCGI_END_REQUEST(appStatus:" + appStatus + ", pStatus:" + pStatus + ")");
            }

            if (appStatus != 0) {
                _isDead = true;
            }

            if (pStatus != FCGI_REQUEST_COMPLETE) {
                _isDead = true;
            }

            _is.skip(3);
            _is = null;
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

    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

}
