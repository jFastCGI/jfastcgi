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

import java.net.Socket;
import java.util.Properties;

public class FCGIRequest {

    /*
    * This class has no methods. Right now we are single threaded
    * so there is only one request object at any given time which
    * is referenced by an FCGIInterface class variable . All of this
    * object's data could just as easily be declared directly there.
    * When we thread, this will change, so we might as well use a
    * separate class. In line with this thinking, though somewhat
    * more perversely, we kept the socket here.
    */

    /*
     * class variables
     */
    /*public static Socket  socket; */
    // same for all requests

    /*
     * instance variables
     */
    private Socket socket;
    private boolean isBeginProcessed;
    private int requestID;
    private boolean keepConnection;
    private int role;
    private int appStatus;
    private int numWriters;
    private FCGIInputStream inStream;
    private FCGIOutputStream outStream;
    private FCGIOutputStream errStream;
    private Properties params;

    public int getId() {
        return getRequestID();
    }


    public Socket getSocket() {
        return socket;
    }

    public void setSocket(final Socket socket) {
        this.socket = socket;
    }

    public boolean isBeginProcessed() {
        return isBeginProcessed;
    }

    public void setBeginProcessed(final boolean beginProcessed) {
        isBeginProcessed = beginProcessed;
    }

    public int getRequestID() {
        return requestID;
    }

    public void setRequestID(final int requestID) {
        this.requestID = requestID;
    }

    public boolean isKeepConnection() {
        return keepConnection;
    }

    public void setKeepConnection(final boolean keepConnection) {
        this.keepConnection = keepConnection;
    }

    public int getRole() {
        return role;
    }

    public void setRole(final int role) {
        this.role = role;
    }

    public int getAppStatus() {
        return appStatus;
    }

    public void setAppStatus(final int appStatus) {
        this.appStatus = appStatus;
    }

    public int getNumWriters() {
        return numWriters;
    }

    public void setNumWriters(final int numWriters) {
        this.numWriters = numWriters;
    }

    public FCGIInputStream getInStream() {
        return inStream;
    }

    public void setInStream(final FCGIInputStream inStream) {
        this.inStream = inStream;
    }

    public FCGIOutputStream getOutStream() {
        return outStream;
    }

    public void setOutStream(final FCGIOutputStream outStream) {
        this.outStream = outStream;
    }

    public FCGIOutputStream getErrStream() {
        return errStream;
    }

    public void setErrStream(final FCGIOutputStream errStream) {
        this.errStream = errStream;
    }

    public Properties getParams() {
        return params;
    }

    public void setParams(final Properties params) {
        this.params = params;
    }
}


