/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans;

import io.entgra.device.mgt.core.device.mgt.api.jaxrs.exception.BadRequestException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.http.util.TextUtils;

public class Credential {

    private static final Log log = LogFactory.getLog(Credential.class);

    private String username;
    private String password;
    private String tenantDomain;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    public void validateRequest() {
        if (TextUtils.isEmpty(getUsername())) {
            String msg = "Error occurred while validating the user. Username is not found to validate the user";
            log.error(msg);
            throw new BadRequestException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(HttpStatus.SC_BAD_REQUEST).setMessage(msg)
                            .build());
        }
        if (TextUtils.isEmpty(getPassword())) {
            String msg = "Error occurred while validating the user. Password is not found to validate the user";
            log.error(msg);
            throw new BadRequestException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(HttpStatus.SC_BAD_REQUEST).setMessage(msg)
                            .build());
        }
    }
}
