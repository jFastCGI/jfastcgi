/*
 Copyright (c) 1996 Open Market, Inc.
 (see LICENSE_OPEN_MARKET.txt)
 */
/*

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
package com.fastcgi;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

/* This class handles reading and building the fastcgi messages.
 * For reading incoming mesages, we pass the input
 * stream as a param to the constructor rather than to each method.
 * Methods that build messages use and return internal buffers, so they
 * dont need a stream.
 */

public class FCGIMessage {

    /*
     * Instance variables
     */
    /*
     * FCGI Message Records
     * The logical structures of the FCGI Message Records.
     * Fields are originally 1 unsigned byte in message
     * unless otherwise noted.
     */
    /*
     * FCGI Header
     */
    private int h_version;
    private int h_type;
    private int h_requestID;       // 2 bytes
    private int h_contentLength;   // 2 bytes
    private int h_paddingLength;
    /*
     * FCGI BeginRequest body.
     */
    private int br_role;      // 2 bytes
    private int br_flags;

    private FCGIInputStream in;

    /*
     * constructor - Java would do this implicitly.
     */
    public FCGIMessage() {
        super();
    }

    /*
     * constructor - get the stream.
     */
    public FCGIMessage(FCGIInputStream instream) {
        in = instream;
    }

    /*
     * Message Reading Methods
     */

    /*
     * Interpret the FCGI Message Header. Processes FCGI
     * BeginRequest and Management messages. Param header is the header.
     * The calling routine has to keep track of the stream reading
     * management or use FCGIInputStream.fill() which does just that.
     */
    public int processHeader(byte[] header) throws IOException {
        processHeaderBytes(header);
        if (h_version != FCGIConstants.FASTCGI_VERSION_ONE) {
            return (FCGIConstants.ERROR_UNSUPPORTED_VERSION);
        }
        in.setContentLen(h_contentLength);
        in.setPaddingLen(h_paddingLength);
        if (h_type == FCGIConstants.TYPE_BEGIN_REQUEST) {
            return processBeginRecord(h_requestID);
        }
        if (h_requestID == FCGIConstants.REQUEST_ID_NULL_VALUE) {
            return processManagementRecord(h_type);
        }
        if (h_requestID != in.getRequest().getId()) {
            return (FCGIConstants.HEADER_SKIP);
        }
        if (h_type != in.getType()) {
            return (FCGIConstants.ERROR_PROTOCOL_ERROR);
        }
        return (FCGIConstants.HEADER_STREAM_RECORD);
    }

    /* Put the unsigned bytes in the incoming FCGI header into
     * integer form for Java, concatinating bytes when needed.
     * Because Java has no unsigned byte type, we have to be careful
     * about signed numeric promotion to int.
     */
    private void processHeaderBytes(byte[] hdrBuf) {
        h_version = hdrBuf[0] & 0xFF;
        h_type = hdrBuf[1] & 0xFF;
        h_requestID = ((hdrBuf[2] & 0xFF) << 8) | (hdrBuf[3] & 0xFF);
        h_contentLength = ((hdrBuf[4] & 0xFF) << 8) | (hdrBuf[5] & 0xFF);
        h_paddingLength = hdrBuf[6] & 0xFF;
    }

    /*
     * Reads FCGI Begin Request Record.
     */
    public int processBeginRecord(int requestID) throws IOException {
        byte[] beginReqBody;
        byte[] endReqMsg;
        if (requestID == 0 ||
                in.getContentLen() != FCGIConstants.BUFFER_END_REQUEST_BODY_LENGTH) {
            return FCGIConstants.ERROR_PROTOCOL_ERROR;
        }
        /*
         * If the webserver is multiplexing the connection,
         * this library can't deal with it, so repond with
         * FCGIEndReq message with protocolStatus FCGICantMpxConn
         */
        if (in.getRequest().isBeginProcessed()) {
            endReqMsg = new byte[FCGIConstants.BUFFER_HEADER_LENGTH
                    + FCGIConstants.BUFFER_END_REQUEST_BODY_LENGTH];
            System.arraycopy(makeHeader(
                    FCGIConstants.TYPE_END_REQUEST,
                    requestID,
                    FCGIConstants.BUFFER_END_REQUEST_BODY_LENGTH,
                    0), 0, endReqMsg, 0,
                    FCGIConstants.BUFFER_HEADER_LENGTH);
            System.arraycopy(makeEndrequestBody(0,
                    FCGIConstants.PROTOCOL_STATUS_CANT_MULTIPLEX_CONNECTION), 0,
                    endReqMsg,
                    FCGIConstants.BUFFER_HEADER_LENGTH,
                    FCGIConstants.BUFFER_END_REQUEST_BODY_LENGTH);
            /*
             * since isBeginProcessed is first set below,this
             * can't be out first call, so request.out is properly set
             */
            try {
                in.getRequest().getOutputStream().write(endReqMsg, 0,
                        FCGIConstants.BUFFER_HEADER_LENGTH
                                + FCGIConstants.BUFFER_END_REQUEST_BODY_LENGTH);
            }
            catch (IOException e) {
                in.getRequest().getOutputStream().setException(e);
                return -1;
            }
        }
        /*
         * Accept this  new request. Read the record body
         */
        in.getRequest().setRequestID(requestID);
        beginReqBody = new byte[FCGIConstants.BUFFER_BEGIN_REQUEST_BODY_LENGTH];
        if (in.read(beginReqBody, 0,
                FCGIConstants.BUFFER_BEGIN_REQUEST_BODY_LENGTH) !=
                FCGIConstants.BUFFER_BEGIN_REQUEST_BODY_LENGTH) {
            return FCGIConstants.ERROR_PROTOCOL_ERROR;
        }
        br_flags = beginReqBody[2] & 0xFF;
        in.getRequest().setKeepConnection((br_flags & FCGIConstants.MASK_KEEP_CONNECTION) != 0);
        br_role = ((beginReqBody[0] & 0xFF) << 8) | (beginReqBody[1] & 0xFF);
        in.getRequest().setRole(br_role);
        in.getRequest().setBeginProcessed(true);
        return FCGIConstants.HEADER_BEGIN_RECORD;
    }

    /*
     * Reads and Responds to a Management Message. The only type of
     * management message this library understands is FCGIGetValues.
     * The only variables that this library's FCGIGetValues understands
     * are def_FCGIMaxConns, def_FCGIMaxReqs, and def_FCGIMpxsConns.
     * Ignore the other management variables, and repsond to other
     * management messages with FCGIUnknownType.
     */
    public int processManagementRecord(int type) throws IOException {

        byte[] response = new byte[64];
        int wrndx = response[FCGIConstants.BUFFER_HEADER_LENGTH];
        int value = 0;
        int len = 0;
        int plen = 0;


        if (type == FCGIConstants.TYPE_GET_VALUES) {
            Properties tmpProps = new Properties();
            readParams(tmpProps);

            if (in.getFCGIError() != 0 || in.getContentLen() != 0) {
                return FCGIConstants.ERROR_PROTOCOL_ERROR;
            }
            if (tmpProps.containsKey(
                    FCGIConstants.MAX_TRANSPORT_CONNECTIONS_CONSTANT)) {
                makeNameVal(
                        FCGIConstants.MAX_TRANSPORT_CONNECTIONS_CONSTANT, "1",
                        response, wrndx);
            }
            else {
                if (tmpProps.containsKey(
                        FCGIConstants.MAX_CONCURRENT_REQUESTS_CONSTANT)) {
                    makeNameVal(
                            FCGIConstants.MAX_CONCURRENT_REQUESTS_CONSTANT, "1",
                            response, wrndx);
                }
                else {
                    if (tmpProps.containsKey(
                            FCGIConstants.MAX_TRANSPORT_CONNECTIONS_CONSTANT)) {
                        makeNameVal(
                                FCGIConstants.MULTIPLEX_CONNECTIONS_CONSTANT, "0",
                                response, wrndx);
                    }
                }
            }
            plen = 64 - wrndx;
            len = wrndx - FCGIConstants.BUFFER_HEADER_LENGTH;
            System.arraycopy(makeHeader(
                    FCGIConstants.TYPE_GET_VALUES_RESULT,
                    FCGIConstants.REQUEST_ID_NULL_VALUE,
                    len, plen), 0,
                    response, 0,
                    FCGIConstants.BUFFER_HEADER_LENGTH);
        }
        else {
            len = FCGIConstants.BUFFER_UNKNOWN_BODY_TYPE_LENGTH;
            plen = FCGIConstants.BUFFER_UNKNOWN_BODY_TYPE_LENGTH;

            System.arraycopy(makeHeader(
                    FCGIConstants.TYPE_UNKNOWN_TYPE,
                    FCGIConstants.REQUEST_ID_NULL_VALUE,
                    len, 0), 0,
                    response, 0,
                    FCGIConstants.BUFFER_HEADER_LENGTH);
            System.arraycopy(makeUnknownTypeBodyBody(h_type), 0,
                    response,
                    FCGIConstants.BUFFER_HEADER_LENGTH,
                    FCGIConstants.BUFFER_UNKNOWN_BODY_TYPE_LENGTH);
        }

        /*
         * No guarantee that we have a request yet, so
         * dont use fcgi output stream to reference socket, instead
         * use the FileInputStream that refrences it. Also
         * nowhere to save exception, since this is not FCGI stream.
         */

        try {
            in.getRequest().getSocket().getOutputStream().write(response, 0,
                    FCGIConstants.BUFFER_HEADER_LENGTH +
                            FCGIConstants.BUFFER_UNKNOWN_BODY_TYPE_LENGTH);

        }
        catch (IOException e) {
            return -1;
        }
        return FCGIConstants.HEADER_MANAGEMENT_RECORD;
    }

    /*
     * Makes a name/value with name = string of some length, and
     * value a 1 byte integer. Pretty specific to what we are doing
     * above.
     */
    void makeNameVal(String name, String value, byte[] dest, int pos) {
        int nameLen = name.length();
        if (nameLen < 0x80) {
            dest[pos++] = (byte) nameLen;
        }
        else {
            dest[pos++] = (byte) (((nameLen >> 24) | 0x80) & 0xff);
            dest[pos++] = (byte) ((nameLen >> 16) & 0xff);
            dest[pos++] = (byte) ((nameLen >> 8) & 0xff);
            dest[pos++] = (byte) nameLen;
        }
        int valLen = value.length();
        if (valLen < 0x80) {
            dest[pos++] = (byte) valLen;
        }
        else {
            dest[pos++] = (byte) (((valLen >> 24) | 0x80) & 0xff);
            dest[pos++] = (byte) ((valLen >> 16) & 0xff);
            dest[pos++] = (byte) ((valLen >> 8) & 0xff);
            dest[pos++] = (byte) valLen;
        }

        try {
            System.arraycopy(name.getBytes("UTF-8"), 0, dest, pos, nameLen);
            pos += nameLen;

            System.arraycopy(value.getBytes("UTF-8"), 0, dest, pos, valLen);
            pos += valLen;
        }
        catch (UnsupportedEncodingException x) {
            return;
        }
    }

    /*
     * Read FCGI name-value pairs from a stream until EOF. Put them
     * into a Properties object, storing both as strings.
     */
    public int readParams(Properties props) throws IOException {
        int nameLen = 0;
        int valueLen = 0;
        byte[] lenBuff = new byte[3];
        int i = 1;

        while ((nameLen = in.read()) != -1) {
            i++;
            if ((nameLen & 0x80) != 0) {
                if ((in.read(lenBuff, 0, 3)) != 3) {
                    in.setFCGIError(
                            FCGIConstants.ERROR_PARAMS_ERROR);
                    return -1;
                }
                nameLen = ((nameLen & 0x7f) << 24)
                        | ((lenBuff[0] & 0xFF) << 16)
                        | ((lenBuff[1] & 0xFF) << 8)
                        | (lenBuff[2] & 0xFF);
            }

            valueLen = in.read();
            if (valueLen == -1) {
                in.setFCGIError(
                        FCGIConstants.ERROR_PARAMS_ERROR);
                return -1;
            }

            if ((valueLen & 0x80) != 0) {
                if ((in.read(lenBuff, 0, 3)) != 3) {
                    in.setFCGIError(
                            FCGIConstants.ERROR_PARAMS_ERROR);
                    return -1;
                }
                valueLen = ((valueLen & 0x7f) << 24)
                        | ((lenBuff[0] & 0xFF) << 16)
                        | ((lenBuff[1] & 0xFF) << 8)
                        | (lenBuff[2] & 0xFF);
            }

            /*
             * nameLen and valueLen are now valid; read the name
             * and the value from the stream and construct a standard
             * environmental entity
             */
            byte[] name = new byte[nameLen];
            byte[] value = new byte[valueLen];
            if (in.read(name, 0, nameLen) != nameLen) {
                in.setFCGIError(
                        FCGIConstants.ERROR_PARAMS_ERROR);
                return -1;
            }

            if (in.read(value, 0, valueLen) != valueLen) {
                in.setFCGIError(
                        FCGIConstants.ERROR_PARAMS_ERROR);
                return -1;
            }
            String strName = new String(name);
            String strValue = new String(value);
            props.put(strName, strValue);
        }
        return 0;


    }
    /*
     * Message Building Methods
     */

    /*
     * Build an FCGI Message Header -
     */
    public byte[] makeHeader(int type,
                             int requestId,
                             int contentLength,
                             int paddingLength) {
        byte[] header = new byte[FCGIConstants.BUFFER_HEADER_LENGTH];
        header[0] = (byte) FCGIConstants.FASTCGI_VERSION_ONE;
        header[1] = (byte) type;
        header[2] = (byte) ((requestId >> 8) & 0xff);
        header[3] = (byte) ((requestId) & 0xff);
        header[4] = (byte) ((contentLength >> 8) & 0xff);
        header[5] = (byte) ((contentLength) & 0xff);
        header[6] = (byte) paddingLength;
        header[7] = 0;  //reserved byte
        return header;
    }

    /*
     * Build an FCGI Message End Request Body
     */
    public byte[] makeEndrequestBody(int appStatus, int protocolStatus) {
        byte[] body = new byte[FCGIConstants.BUFFER_END_REQUEST_BODY_LENGTH];
        body[0] = (byte) ((appStatus >> 24) & 0xff);
        body[1] = (byte) ((appStatus >> 16) & 0xff);
        body[2] = (byte) ((appStatus >> 8) & 0xff);
        body[3] = (byte) ((appStatus) & 0xff);
        body[4] = (byte) protocolStatus;
        for (int i = 5; i < 8; i++) {
            body[i] = 0;
        }
        return body;
    }

    /*
     * Build an FCGI Message UnknownTypeBodyBody
     */
    public byte[] makeUnknownTypeBodyBody(int type) {
        byte[] body =
                new byte[FCGIConstants.BUFFER_UNKNOWN_BODY_TYPE_LENGTH];
        body[0] = (byte) type;
        for (int i = 1;
             i < FCGIConstants.BUFFER_UNKNOWN_BODY_TYPE_LENGTH; i++) {
            body[i] = 0;
        }
        return body;
    }

}
