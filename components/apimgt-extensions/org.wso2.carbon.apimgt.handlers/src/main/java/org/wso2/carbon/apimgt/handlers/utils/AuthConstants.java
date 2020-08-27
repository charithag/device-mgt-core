/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.handlers.utils;

/**
 * This initializes the constance.
 */
public class AuthConstants {
    public static final String MDM_SIGNATURE = "mdm-signature";
    public static final String PROXY_MUTUAL_AUTH_HEADER = "proxy-mutual-auth-header";
    public static final String MUTUAL_AUTH_HEADER = "mutual-auth-header";
    public static final String ONE_TIME_TOKEN_HEADER = "one-time-token";
    public static final String ENCODED_PEM = "encoded-pem";
    public static final String CALLBACK_URL = "";
    public static final String CLIENT_NAME = "IOT-API-MANAGER";
    public static final String GRANT_TYPE = "refresh_token password client_credentials";
    public static final String TOKEN_SCOPE = "default";
    public static final String CONTENT_TYPE_HEADER = "Content-Type";
    public static final String CONTENT_TYPE = "application/json";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BASIC_AUTH_PREFIX = "Basic ";
    public static final String CLIENT_ID = "clientId";
    public static final String CLIENT_SECRET = "clientSecret";
    public static final String CLIENT_CERTIFICATE = "ssl.client.auth.cert.X509";
}
