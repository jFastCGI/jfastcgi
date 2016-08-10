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

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This stream understands the FCGI protocol.
 */

public class FCGIOutputStream extends OutputStream {
    /* Stream vars */

    private int wrNext;
    private int stop;
    private boolean isClosed;

    /* require methods to set, get and clear */
    private int errno;
    private Exception errex;

    /* data vars */

    private byte[] buff;
    private int buffLen;
    private int buffStop;
    private int type;
    private boolean isAnythingWritten;
    private boolean rawWrite;
    private FCGIRequest request;

    private OutputStream out;

    /**
     * Creates a new output stream to manage fcgi prototcol stuff
     *
     * @param outStream  the output stream
     * @param bufLen     length of buffer
     * @param streamType
     */
    public FCGIOutputStream(OutputStream outStream,
                            int bufLen, int streamType,
                            FCGIRequest inreq) {
        out = outStream;
        buffLen = Math.min(bufLen, FCGIConstants.MAX_BUFFER_LENGTH);
        buff = new byte[buffLen];
        type = streamType;
        stop = bufLen;
        buffStop = bufLen;
        buffLen = bufLen;
        isAnythingWritten = false;
        rawWrite = false;
        wrNext = FCGIConstants.BUFFER_HEADER_LENGTH;
        isClosed = false;
        request = inreq;
    }

    /**
     * Writes a byte to the output stream.
     */
    @Override
    public void write(int c) throws IOException {
        if (wrNext != stop) {
            buff[wrNext++] = (byte) c;
            return;
        }
        if (isClosed) {
            throw new EOFException();
        }
        empty(false);
        if (wrNext != stop) {
            buff[wrNext++] = (byte) c;
            return;
        }
        /* NOTE: ASSERT(stream->isClosed); */
        /* bug in emptyBuffProc if not */
        throw new EOFException();
    }

    /**
     * Writes an array of bytes. This method will block until the bytes
     * are actually written.
     *
     * @param b the data to be written
     */
    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    /**
     * Writes len consecutive bytes from off in the array b
     * into the output stream.  Performs no interpretation
     * of the output bytes. Making the user convert the string to
     * bytes is in line with current Java practice.
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        int m = 0;
        int bytesMoved = 0;
        /*
        * Fast path: room for n bytes in the buffer
        */
        if (len <= (stop - wrNext)) {
            System.arraycopy(b, off, buff, wrNext, len);
            wrNext += len;
            return;
        }
        /*
        * General case: stream is closed or buffer empty procedure
        * needs to be called
        */
        bytesMoved = 0;
        while (true) {
            if (wrNext != stop) {
                m = Math.min(len - bytesMoved, stop - wrNext);
                System.arraycopy(b, off, buff, wrNext, m);
                bytesMoved += m;
                wrNext += m;
                if (bytesMoved == len) {
                    return;
                }
                off += m;
            }
            if (isClosed) {
                throw new EOFException();
            }
            empty(false);
        }
    }

    /**
     * Encapsulates any buffered stream content in a FastCGI
     * record.  If !doClose, writes the data, making the buffer
     * empty.
     */
    public void empty(boolean doClose) throws IOException {
        int cLen = 0;
        /*
        * Alignment padding omitted in Java
        */
        if (!rawWrite) {
            cLen = wrNext - FCGIConstants.BUFFER_HEADER_LENGTH;
            if (cLen > 0) {
                System.arraycopy(new FCGIMessage().makeHeader(type,
                        request.getRequestID(), cLen, 0),
                        0, buff, 0,
                        FCGIConstants.BUFFER_HEADER_LENGTH);
            }
            else {
                wrNext = 0;
            }
        }
        if (doClose) {
            writeCloseRecords();
        }
        if (wrNext != 0) {
            isAnythingWritten = true;
            try {
                out.write(buff, 0, wrNext);
            }
            catch (IOException e) {
                setException(e);
                return;
            }
            wrNext = 0;
        }
        /*
        * The buffer is empty.
        */
        if (!rawWrite) {
            wrNext += FCGIConstants.BUFFER_HEADER_LENGTH;
        }
    }

    /**
     * Close the stream.
     */
    @Override
    public void close() throws IOException {
        if (isClosed) {
            return;
        }
        empty(true);
        /*
        * if isClosed, will return with EOFException from write.
        */
        isClosed = true;
        stop = wrNext;
        return;
    }

    /**
     * Flushes any buffered output.
     * Server-push is a legitimate application of flush.
     * Otherwise, it is not very useful, since FCGIAccept
     * does it implicitly.  flush may reduce performance
     * by increasing the total number of operating system calls
     * the application makes.
     */
    @Override
    public void flush() throws IOException {
        if (isClosed) {
            return;
        }
        empty(false);
        /*
        * if isClosed, will return with EOFException from write.
        */
        return;
    }

    /**
     * An FCGI error has occurred. Save the error code in the stream
     * for diagnostic purposes and set the stream state so that
     * reads return EOF
     */
    public void setFCGIError(int errnum) {
        /*
        * Preserve only the first error.
        */
        if (errno == 0) {
            errno = errnum;
        }
        isClosed = true;
    }

    /**
     * An Exception has occurred. Save the Exception in the stream
     * for diagnostic purposes and set the stream state so that
     * reads return EOF
     */
    public void setException(Exception errexpt) {
        /*
        * Preserve only the first error.
        */
        if (errex == null) {
            errex = errexpt;
        }
        isClosed = true;
    }

    /**
     * Clear the stream error code and end-of-file indication.
     */
    public void clearFCGIError() {
        errno = 0;
        /*
        * isClosed = false;
        * XXX: should clear isClosed but work is needed to make it safe
        * to do so.
        */
    }

    /**
     * Clear the stream error code and end-of-file indication.
     */
    public void clearException() {
        errex = null;
        /*
        * isClosed = false;
        * XXX: should clear isClosed but work is needed to make it safe
        * to do so.
        */
    }

    /**
     * accessor method since var is private
     */
    public int etFCGIError() {
        return errno;
    }

    /**
     * accessor method since var is private
     */
    public Exception getException() {
        return errex;
    }

    /**
     * Writes an EOF record for the stream content if necessary.
     * If this is the last writer to close, writes an FCGI_END_REQUEST
     * record.
     */
    public void writeCloseRecords() throws IOException {
        FCGIMessage msg = new FCGIMessage();
        /*
        * Enter rawWrite mode so final records won't be
        * encapsulated as
        * stream data.
        */
        rawWrite = true;
        /*
        * Generate EOF for stream content if needed.
        */
        if (!(type == FCGIConstants.TYPE_STDERR
                && wrNext == 0
                && !isAnythingWritten)) {
            byte[] hdr =
                    new byte[FCGIConstants.BUFFER_HEADER_LENGTH];
            System.arraycopy(msg.makeHeader(type,
                    request.getRequestID(),
                    0, 0),
                    0, hdr, 0,
                    FCGIConstants.BUFFER_HEADER_LENGTH);
            write(hdr, 0, hdr.length);
        }
        /*
        * Generate FCGI_END_REQUEST record if needed.
        */
        if (request.getNumWriters() == 1) {
            byte[] endReq =
                    new byte[FCGIConstants.BUFFER_HEADER_LENGTH
                            + FCGIConstants.BUFFER_END_REQUEST_BODY_LENGTH];
            System.arraycopy(msg.makeHeader(
                    FCGIConstants.TYPE_END_REQUEST,
                    request.getRequestID(),
                    FCGIConstants.BUFFER_END_REQUEST_BODY_LENGTH, 0),
                    0, endReq, 0,
                    FCGIConstants.BUFFER_HEADER_LENGTH);
            System.arraycopy(msg.makeEndrequestBody(
                    request.getAppStatus(),
                    FCGIConstants.PROTOCOL_STATUS_REQUEST_COMPLETE),
                    0, endReq,
                    FCGIConstants.BUFFER_HEADER_LENGTH,
                    FCGIConstants.BUFFER_END_REQUEST_BODY_LENGTH);
            write(endReq, 0, FCGIConstants.BUFFER_HEADER_LENGTH
                    + FCGIConstants.BUFFER_END_REQUEST_BODY_LENGTH);
        }
        request.setNumWriters(request.getNumWriters() - 1);
    }
}

