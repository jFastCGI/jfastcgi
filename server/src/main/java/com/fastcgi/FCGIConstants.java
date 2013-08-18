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

/* This class contains FCGI global definitions corresponding to
 * the #defs in the C version.
 */
public enum FCGIConstants {

    ;

    public static final int MAX_BUFFER_LENGTH = 0xffff;


    /*
    * Define Length of FCGI message bodies in bytes
    */
    public static final int BUFFER_HEADER_LENGTH = 8;
    public static final int BUFFER_END_REQUEST_BODY_LENGTH = 8;
    public static final int BUFFER_BEGIN_REQUEST_BODY_LENGTH = 8;
    public static final int BUFFER_UNKNOWN_BODY_TYPE_LENGTH = 8;


    /*
    * Header defines
    */
    public final static int FASTCGI_VERSION_ONE = 1;

    /* FCGI Record Types */
    public static final int TYPE_BEGIN_REQUEST = 1;
    public static final int TYPE_ABORT_REQUEST = 2;
    public static final int TYPE_END_REQUEST = 3;
    public static final int TYPE_PARAMS = 4;
    public static final int TYPE_STDIN = 5;
    public static final int TYPE_STDOUT = 6;
    public static final int TYPE_STDERR = 7;
    public static final int TYPE_DATA = 8;
    public static final int TYPE_GET_VALUES = 9;
    public static final int TYPE_GET_VALUES_RESULT = 10;
    public static final int TYPE_UNKNOWN_TYPE = 11;
    public static final int TYPE_LAST_TYPE = TYPE_UNKNOWN_TYPE;

    /* Request ID Values */
    public static final int REQUEST_ID_NULL_VALUE = 0;


    /*
    * Begin Request defines
    */

    /* Mask flags */
    public final static int MASK_KEEP_CONNECTION = 1;


    /* Roles */
    public static final int ROLE_RESPONDER = 1;
    public static final int ROLE_AUTHORIZER = 2;
    public static final int ROLE_FILTER = 3;

    /*
    * End Request defines
    */


    /* Protocol status */
    public static final int PROTOCOL_STATUS_REQUEST_COMPLETE = 0;
    public static final int PROTOCOL_STATUS_CANT_MULTIPLEX_CONNECTION = 1;
    public static final int PROTOCOL_STATUS_OVERLOAD = 2;
    public static final int PROTOCOL_STATUS_UNKNOWN_ROLE = 3;


    /*
    * Get Values, Get Values Results  defines
    */

    /**
     * FCGI_MAX_CONNS: The maximum number of concurrent transport connections this application will accept, e.g. "1" or "10".
     */
    public static final String MAX_TRANSPORT_CONNECTIONS_CONSTANT = "FCGI_MAX_CONNS";

    /**
     * FCGI_MAX_REQS: The maximum number of concurrent requests this application will accept, e.g. "1" or "50".
     */
    public static final String MAX_CONCURRENT_REQUESTS_CONSTANT = "FCGI_MAX_REQS";

    /**
     * FCGI_MPXS_CONNS: "0" if this application does not multiplex connections
     * (i.e. handle concurrent requests over each connection), "1" otherwise.
     */
    public static final String MULTIPLEX_CONNECTIONS_CONSTANT = "FCGI_MPXS_CONNS";


    /*
    * Return codes for Process* functions
    */
    public static final int HEADER_STREAM_RECORD = 0;
    public static final int HEADER_SKIP = 1;
    public static final int HEADER_BEGIN_RECORD = 2;
    public static final int HEADER_MANAGEMENT_RECORD = 3;


    /*
    * Error Codes
    */
    public static final int ERROR_UNSUPPORTED_VERSION = -2;
    public static final int ERROR_PROTOCOL_ERROR = -3;
    public static final int ERROR_PARAMS_ERROR = -4;
    public static final int ERROR_CALL_SEQUENCE_ERROR = -5;
}
