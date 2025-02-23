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

package io.entgra.device.mgt.core.apimgt.keymgt.extension.api;

import com.google.gson.Gson;
import io.entgra.device.mgt.core.apimgt.keymgt.extension.DCRResponse;
import io.entgra.device.mgt.core.apimgt.keymgt.extension.TokenRequest;
import io.entgra.device.mgt.core.apimgt.keymgt.extension.TokenResponse;
import io.entgra.device.mgt.core.apimgt.keymgt.extension.exception.BadRequestException;
import io.entgra.device.mgt.core.apimgt.keymgt.extension.exception.KeyMgtException;
import io.entgra.device.mgt.core.apimgt.keymgt.extension.service.KeyMgtService;
import io.entgra.device.mgt.core.apimgt.keymgt.extension.service.KeyMgtServiceImpl;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.UnAuthorizedException;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Base64;

public class KeyManagerServiceImpl implements KeyManagerService {

    Gson gson = new Gson();

    @Override
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/dynamic-client-registration")
    public Response dynamicClientRegistration(DCRRequest dcrRequest) {
        try {
            KeyMgtService keyMgtService = new KeyMgtServiceImpl();
            DCRResponse resp = keyMgtService.dynamicClientRegistration(dcrRequest.getApplicationName(), dcrRequest.getUsername(),
                    dcrRequest.getGrantTypes(), dcrRequest.getCallBackUrl(), dcrRequest.getTags(),
                    dcrRequest.getIsSaasApp(), dcrRequest.getValidityPeriod(), dcrRequest.getPassword(),
                    dcrRequest.getSupportedGrantTypes(), dcrRequest.getCallbackUrl());
            return Response.status(Response.Status.CREATED).entity(gson.toJson(resp)).build();
        } catch (KeyMgtException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("/token")
    public Response generateAccessToken(@HeaderParam("Authorization") String basicAuthHeader,
                                        @FormParam("refresh_token") String refreshToken,
                                        @FormParam("scope") String scope,
                                        @FormParam("grant_type") String grantType,
                                        @FormParam("assertion") String assertion,
                                        @FormParam("admin_access_token") String admin_access_token,
                                        @FormParam("username") String username,
                                        @FormParam("password") String password,
                                        @FormParam("validityPeriod") int validityPeriod) {
        try {
            if (basicAuthHeader == null) {
                String msg = "Invalid credentials. Make sure your API call is invoked with a Basic Authorization header.";
                throw new UnAuthorizedException(msg);
            }
            String encodedClientCredentials = new String(Base64.getDecoder().decode(basicAuthHeader.split(" ")[1]));
            KeyMgtService keyMgtService = new KeyMgtServiceImpl();
            TokenResponse resp = keyMgtService.generateAccessToken(
                    new TokenRequest(encodedClientCredentials.split(":")[0],
                            encodedClientCredentials.split(":")[1], refreshToken, scope,
                            grantType, assertion, admin_access_token, username, password, validityPeriod));
            return Response.status(Response.Status.OK).entity(gson.toJson(resp)).build();
        } catch (KeyMgtException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (UnAuthorizedException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        }
    }
}
