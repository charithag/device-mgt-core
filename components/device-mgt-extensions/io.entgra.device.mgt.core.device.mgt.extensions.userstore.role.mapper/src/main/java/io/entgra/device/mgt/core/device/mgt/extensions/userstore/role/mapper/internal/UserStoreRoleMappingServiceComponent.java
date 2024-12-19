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

package io.entgra.device.mgt.core.device.mgt.extensions.userstore.role.mapper.internal;

import io.entgra.device.mgt.core.device.mgt.extensions.userstore.role.mapper.UserStoreRoleMapper;
import io.entgra.device.mgt.core.device.mgt.extensions.userstore.role.mapper.UserStoreRoleMappingConfigManager;
import io.entgra.device.mgt.core.server.bootup.heartbeat.beacon.service.HeartBeatManagementService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

@Component(
        name = "io.entgra.device.mgt.core.device.mgt.extensions.userstore.role.mapper.internal.UserStoreRoleMappingServiceComponent",
        immediate = true)
public class UserStoreRoleMappingServiceComponent {

    private static final Log log = LogFactory.getLog(UserStoreRoleMappingServiceComponent.class);

    @Activate
    protected void activate(ComponentContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("Activating Role Management Service Component");
        }
        try {
            BundleContext bundleContext = ctx.getBundleContext();
            UserStoreRoleMapper mapper = new UserStoreRoleMapper();
            bundleContext.registerService(ServerStartupObserver.class.getName(), mapper, null);
            UserStoreRoleMappingDataHolder.getInstance().setUserStoreRoleMappingConfigManager(new UserStoreRoleMappingConfigManager());
            if (log.isDebugEnabled()) {
                log.debug("Role Management Service Component has been successfully activated");
            }
        } catch (Throwable e) {
            log.error("Error occurred while activating Role Management Service Component", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("De-activating Role Manager Service Component");
        }
    }

//    @Reference(
//            name = "user.realmservice.default",
//            service = org.apache.axis2.context.ConfigurationContext.class,
//            cardinality = ReferenceCardinality.MANDATORY,
//            policy = ReferencePolicy.DYNAMIC,
//            unbind = "unsetRealmService")
//    protected void setConfigurationContextService(ConfigurationContextService configurationContextService) {
//        if (log.isDebugEnabled()) {
//            log.debug("Setting ConfigurationContextService");
//        }
//
//        UserStoreRoleMappingDataHolder.getInstance().setConfigurationContextService(configurationContextService);
//    }
//
//    @Reference(
//            name = "config.context.service",
//            service = org.wso2.carbon.utils.ConfigurationContextService.class,
//            cardinality = ReferenceCardinality.OPTIONAL,
//            policy = ReferencePolicy.DYNAMIC,
//            unbind = "unsetConfigurationContextService")
//    protected void unsetConfigurationContextService(ConfigurationContextService configurationContextService) {
//        if (log.isDebugEnabled()) {
//            log.debug("Un-setting ConfigurationContextService");
//        }
//        UserStoreRoleMappingDataHolder.getInstance().setConfigurationContextService(null);
//    }

    /**
     * Sets Realm Service.
     *
     * @param realmService An instance of RealmService
     */
    protected void setRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting Realm Service");
        }
        UserStoreRoleMappingDataHolder.getInstance().setRealmService(realmService);
    }

    /**
     * Unsets Realm Service.
     *
     * @param realmService An instance of RealmService
     */
    protected void unsetRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting Realm Service");
        }
        UserStoreRoleMappingDataHolder.getInstance().setRealmService(null);
    }

    @SuppressWarnings("unused")
    @Reference(
            name = "entgra.heart.beat.service",
            service = io.entgra.device.mgt.core.server.bootup.heartbeat.beacon.service.HeartBeatManagementService.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetHeartBeatService")
    protected void setHeartBeatService(HeartBeatManagementService heartBeatService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting heart beat service");
        }
        UserStoreRoleMappingDataHolder.getInstance().setHeartBeatService(heartBeatService);
    }

    @SuppressWarnings("unused")
    protected void unsetHeartBeatService(HeartBeatManagementService heartBeatManagementService) {
        if (log.isDebugEnabled()) {
            log.debug("Removing heart beat service");
        }
        UserStoreRoleMappingDataHolder.getInstance().setHeartBeatService(null);
    }
}
