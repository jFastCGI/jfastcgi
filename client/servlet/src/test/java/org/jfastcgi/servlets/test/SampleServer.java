/*
 * (c) 2009-2012 Julien Rialland, and the jFastCGI project developers.
 * Released under BSD License, see LICENSE_JRIALLAND.txt
 */
/*
 Copyright (c) 2013 - the jFastCGI project developers.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/
package org.jfastcgi.servlets.test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A dummy fastcgi application in java.
 *
 * @author jrialland
 */
public class SampleServer implements Runnable {

    private int port;

    private static enum PacketType {
        FCGI_STDOUT(6), FCGI_END_REQUEST(3);

        int code;

        PacketType(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    public SampleServer(int port) {
        this.port = port;

    }

    public int getPort() {
        return port;
    }

    /**
     * <pre>
     *         typedef struct {
     *             unsigned char version;
     *             unsigned char type;
     *             unsigned char requestIdB1;
     *             unsigned char requestIdB0;
     *             unsigned char contentLengthB1;
     *             unsigned char contentLengthB0;
     *             unsigned char paddingLength;
     *             unsigned char reserved;
     *         } FCGI_Header;
     * </pre>
     * 
     * @param os
     */
    private void writeHeader(DataOutputStream os, PacketType pkt, int requestId, int contentLength) throws IOException {
        os.write(1);// version
        os.write(pkt.getCode());// type
        os.writeShort(requestId);
        os.writeShort(contentLength);// contentLength
        os.write(0);// paddingLength
        os.write(0);// reserved
    }

    private void writeStdout(DataOutputStream os, int requestId, String content) throws IOException {
        writeHeader(os, PacketType.FCGI_STDOUT, requestId, content.length());
        os.write(content.getBytes());
    }

    /**
     * <pre>
     * typedef struct {
     *     unsigned char appStatusB3;
     *     unsigned char appStatusB2;
     *     unsigned char appStatusB1;
     *     unsigned char appStatusB0;
     *     unsigned char protocolStatus;
     *     unsigned char reserved[3];
     * } FCGI_EndRequestBody;
     * </pre>
     * 
     * @param os
     */
    private void writeEndRequest(DataOutputStream os, int requestId, int status) throws IOException {
        writeHeader(os, PacketType.FCGI_END_REQUEST, requestId, 8);
        os.write(0);// appStatusB3
        os.write(0);// appStatusB2
        os.write(0);// appStatusB1
        os.write(0);// appStatusB0
        os.write(status);// protocolStatus
        os.write(new byte[3]);// reserved
    }

    public void run() {
        ServerSocket ss = null;
        try {
            ss = new ServerSocket();
            ss.bind(new InetSocketAddress("localhost", port));

            Socket client;
            while ((client = ss.accept()) != null) {

                DataInputStream in = new DataInputStream(client.getInputStream());
                in.skip(2);
                int requestId = in.readShort();

                final DataOutputStream os = new DataOutputStream(client.getOutputStream());

                StringWriter sw = new StringWriter();
                sw.append("Content-type: text/html\r\n\r\n");
                sw.append("<html>");
                sw.append("<head><title>FastCGI-Hello</title></head>");
                sw.append("<body>");
                sw.append("<h3>Hello from fcgi !</h3>");
                sw.append("</body>");
                sw.append("<html>");

                writeStdout(os, requestId, sw.toString());
                writeStdout(os, requestId, "");
                writeEndRequest(os, requestId, 0);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (ss != null) {
                    ss.close();
                }
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        }
    }

    public Thread start() {
        Thread t = new Thread(this);
        t.setDaemon(true);
        t.start();
        return t;
    }

    public static void main(String[] args) {
        new SampleServer(8541).run();
    }
}
