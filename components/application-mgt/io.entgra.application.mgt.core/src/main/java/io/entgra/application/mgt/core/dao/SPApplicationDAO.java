/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.application.mgt.core.dao;

import io.entgra.application.mgt.common.IdentityServer;
import io.entgra.application.mgt.common.dto.ApplicationDTO;
import io.entgra.application.mgt.core.exception.ApplicationManagementDAOException;

import java.util.List;

public interface SPApplicationDAO {

    /**
     *
     * @param identityServerId Id of identity server in which the service provider is in
     * @param spUID Service provider uid of which the applications to be retrieved
     * @return the service provider applications for the given service provider
     * @throws ApplicationManagementDAOException if any db error occurred
     */
    List<ApplicationDTO> getSPApplications(int identityServerId, String spUID, int tenantId) throws ApplicationManagementDAOException;

    /**
     *
     * @param identityServerId Id of identity server in which the service provider is in
     * @param spUID Id of the service provider to which the application should be mapped
     * @param appId Id of the application that should be mapped
     * @return Primary key of the new service provider and application mapping entry
     * @throws ApplicationManagementDAOException if any db error occurred
     */
    int attachSPApplication(int identityServerId, String spUID, int appId, int tenantId) throws ApplicationManagementDAOException;

    /**
     *
     * @param identityServerId Id of identity server in which the service provider is in
     * @param spUID Id of the service provider from which the application should be removed
     * @param appId Id of the application that should be removed
     * @throws ApplicationManagementDAOException if any db error occurred
     */
    void detachSPApplication(int identityServerId, String spUID, int appId, int tenantId) throws ApplicationManagementDAOException;

    /**
     *
     * @return All available identity servers
     * @throws ApplicationManagementDAOException if any db error occurred
     */
    List<IdentityServer> getIdentityServers(int tenantId) throws ApplicationManagementDAOException;

    /**
     *
     * @param id Id of the Identity Server to be retrieved
     * @return Identity Server of the given id
     * @throws ApplicationManagementDAOException if any db error occurred
     */
    IdentityServer getIdentityServerById(int id, int tenantId) throws ApplicationManagementDAOException;

    /**
     * Verify whether application exist for given identity server id, service provider id and application id.
     * Because if an application does not exist for those, it should not be mapped
     *
     * @param appId Id of the application.
     * @param identityServerId Id of the identity server.
     * @param spUID UID of the service provider.
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    boolean isSPApplicationExist(int identityServerId, String spUID, int appId, int tenantId) throws ApplicationManagementDAOException;

    /**
     * Delete application from all service providers if exists. When an application is deleted from the database
     * it shoulbe be deleted from mapping table as well
     *
     * @param applicationId Id of the application to be deleted
     * @throws ApplicationManagementDAOException if any db error occurred
     */
    void deleteApplicationFromServiceProviders(int applicationId, int tenantId) throws ApplicationManagementDAOException;

}
