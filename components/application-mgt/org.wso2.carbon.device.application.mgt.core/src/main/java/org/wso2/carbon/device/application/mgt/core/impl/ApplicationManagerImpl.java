/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.device.application.mgt.core.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.application.mgt.common.AppLifecycleState;
import org.wso2.carbon.device.application.mgt.common.Application;
import org.wso2.carbon.device.application.mgt.common.ApplicationList;
import org.wso2.carbon.device.application.mgt.common.ApplicationRelease;
import org.wso2.carbon.device.application.mgt.common.ApplicationSubscriptionType;
import org.wso2.carbon.device.application.mgt.common.ApplicationType;
import org.wso2.carbon.device.application.mgt.common.Filter;
import org.wso2.carbon.device.application.mgt.common.LifecycleState;
import org.wso2.carbon.device.application.mgt.common.Tag;
import org.wso2.carbon.device.application.mgt.common.UnrestrictedRole;
import org.wso2.carbon.device.application.mgt.common.User;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationManager;
import org.wso2.carbon.device.application.mgt.core.dao.ApplicationDAO;
import org.wso2.carbon.device.application.mgt.core.dao.ApplicationReleaseDAO;
import org.wso2.carbon.device.application.mgt.core.dao.LifecycleStateDAO;
import org.wso2.carbon.device.application.mgt.core.dao.VisibilityDAO;
import org.wso2.carbon.device.application.mgt.core.dao.common.ApplicationManagementDAOFactory;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagementDAOException;
import org.wso2.carbon.device.application.mgt.core.exception.LifeCycleManagementDAOException;
import org.wso2.carbon.device.application.mgt.core.exception.NotFoundException;
import org.wso2.carbon.device.application.mgt.core.exception.ValidationException;
import org.wso2.carbon.device.application.mgt.core.internal.DataHolder;
import org.wso2.carbon.device.application.mgt.core.lifecycle.LifecycleStateManger;
import org.wso2.carbon.device.application.mgt.core.util.ConnectionManagerUtil;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Default Concrete implementation of Application Management related implementations.
 */
public class ApplicationManagerImpl implements ApplicationManager {

    private static final Log log = LogFactory.getLog(ApplicationManagerImpl.class);
    private VisibilityDAO visibilityDAO;
    private ApplicationDAO applicationDAO;
    private ApplicationReleaseDAO applicationReleaseDAO;
    private LifecycleStateDAO lifecycleStateDAO;
    private LifecycleStateManger lifecycleStateManger;


    public ApplicationManagerImpl() {
        initDataAccessObjects();
        lifecycleStateManger = DataHolder.getInstance().getLifecycleStateManager();
    }

    private void initDataAccessObjects() {
        this.visibilityDAO = ApplicationManagementDAOFactory.getVisibilityDAO();
        this.applicationDAO = ApplicationManagementDAOFactory.getApplicationDAO();
        this.lifecycleStateDAO =  ApplicationManagementDAOFactory.getLifecycleStateDAO();
        this.applicationReleaseDAO = ApplicationManagementDAOFactory.getApplicationReleaseDAO();
    }

    @Override
    public Application createApplication(Application application) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        application.setUser(new User(userName, tenantId));
        if (log.isDebugEnabled()) {
            log.debug("Create Application received for the tenant : " + tenantId + " From" + " the user : " + userName);
        }

        ConnectionManagerUtil.openDBConnection();
        validateAppCreatingRequest(application);
        validateAppReleasePayload(application.getApplicationReleases().get(0));
        DeviceType deviceType;
        ApplicationRelease applicationRelease;
        List<ApplicationRelease> applicationReleases = new ArrayList<>();
        try {
            ConnectionManagerUtil.beginDBTransaction();
            MAMDeviceConnectorImpl mamDeviceConnector = new MAMDeviceConnectorImpl();
            // Getting the device type details to get device type ID for internal mappings
            deviceType = mamDeviceConnector.getDeviceManagementService().getDeviceType(application.getDeviceType());

            if (deviceType == null) {
                log.error("Device type is not matched with application type");
                ConnectionManagerUtil.rollbackDBTransaction();
                return null;
            }
            // Insert to application table
            int appId = this.applicationDAO.createApplication(application, deviceType.getId());

            if (appId == -1) {
                log.error("Application creation Failed");
                ConnectionManagerUtil.rollbackDBTransaction();
                return null;
            } else {
                if(log.isDebugEnabled()){
                    log.debug("New Application entry added to AP_APP table. App Id:" + appId);
                }
                if (!application.getTags().isEmpty()) {
                    this.applicationDAO.addTags(application.getTags(), appId, tenantId);
                    if(log.isDebugEnabled()){
                        log.debug("New tags entry added to AP_APP_TAG table. App Id:" + appId);
                    }
                }
                if (!application.getUnrestrictedRoles().isEmpty()) {
                    application.setIsRestricted(true);
                    this.visibilityDAO.addUnrestrictedRoles(application.getUnrestrictedRoles(), appId, tenantId);
                    if(log.isDebugEnabled()){
                        log.debug("New restricted roles to app ID mapping added to AP_UNRESTRICTED_ROLE table." +
                                " App Id:" + appId);
                    }
                } else {
                    if(log.isDebugEnabled()){
                        log.debug("App is not restricted to role. App Id:" + appId);
                    }
                    application.setIsRestricted(false);
                }
                if (application.getApplicationReleases().size() > 1 ){
                    throw new ApplicationManagementException(
                            "Invalid payload. Application creating payload should contains one application release, but "
                                    + "the payload contains more than one");
                }

                if(log.isDebugEnabled()){
                    log.debug("Creating a new release. App Id:" + appId);
                }
                applicationRelease = application.getApplicationReleases().get(0);
                applicationRelease = this.applicationReleaseDAO
                        .createRelease(applicationRelease, appId, tenantId);

                if(log.isDebugEnabled()){
                    log.debug("Changing lifecycle state. App Id:" + appId);
                }
                LifecycleState lifecycleState = new LifecycleState();
                lifecycleState.setCurrentState(AppLifecycleState.CREATED.toString());
                lifecycleState.setPreviousState(AppLifecycleState.CREATED.toString());
                changeLifecycleState(appId, applicationRelease.getUuid(), lifecycleState, false);

                applicationRelease.setLifecycleState(lifecycleState);
                applicationReleases.add(applicationRelease);
                application.setApplicationReleases(applicationReleases);

                ConnectionManagerUtil.commitDBTransaction();
            }

            return application;

        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while adding application";
            log.error(msg, e);
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new ApplicationManagementException(msg, e);
        } catch (DeviceManagementException e) {

            String msg = "Error occurred while getting device type id of " + application.getType();
            log.error(msg, e);
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new ApplicationManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Unknown exception while creating application.";
            log.error(msg, e);
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new ApplicationManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public ApplicationList getApplications(Filter filter) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        ApplicationList applicationList;
        List<ApplicationRelease> applicationReleases;

        filter = validateFilter(filter);
        if (filter == null) {
            throw new ApplicationManagementException("Filter validation failed, Please verify the request payload");
        }

        try {
            ConnectionManagerUtil.getDBConnection();
            applicationList = applicationDAO.getApplications(filter, tenantId);
            if(applicationList != null && applicationList.getApplications() != null && !applicationList
                    .getApplications().isEmpty()) {
                if (!isAdminUser(userName, tenantId, CarbonConstants.UI_ADMIN_PERMISSION_COLLECTION)) {
                    applicationList = getRoleRestrictedApplicationList(applicationList, userName);
                }
                for (Application application : applicationList.getApplications()) {
                    applicationReleases = getReleases(application, filter.isRequirePublishedRelease());
                    application.setApplicationReleases(applicationReleases);
                }
            }
            return applicationList;
        } catch (UserStoreException e) {
            throw new ApplicationManagementException(
                    "User-store exception while checking whether the user " + userName + " of tenant " + tenantId
                            + " has the publisher permission", e);
        } catch (ApplicationManagementDAOException e) {
            throw new ApplicationManagementException(
                    "DAO exception while getting applications for the user " + userName + " of tenant " + tenantId, e);
        }
    }

    @Override
    public ApplicationRelease createRelease(int applicationId, ApplicationRelease applicationRelease)
            throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        Application application = getApplicationIfAccessible(applicationId);
        validateAppReleasePayload(applicationRelease);
        if (log.isDebugEnabled()) {
            log.debug("Application release request is received for the application " + application.toString());
        }
        try {
            ConnectionManagerUtil.beginDBTransaction();
            applicationRelease = this.applicationReleaseDAO
                    .createRelease(applicationRelease, application.getId(), tenantId);
            LifecycleState lifecycleState = new LifecycleState();
            lifecycleState.setCurrentState(AppLifecycleState.CREATED.toString());
            lifecycleState.setPreviousState(AppLifecycleState.CREATED.toString());
            changeLifecycleState(application.getId(), applicationRelease.getUuid(), lifecycleState, true);

            ConnectionManagerUtil.commitDBTransaction();
            return applicationRelease;
        } catch (ApplicationManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new ApplicationManagementException(
                    "Error occurred while adding application release into IoTS app management Application id of the "
                            + "application release: " + applicationId, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public String getUuidOfLatestRelease(int appId) throws ApplicationManagementException {
        try {
            ConnectionManagerUtil.openDBConnection();
            return applicationDAO.getUuidOfLatestRelease(appId);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }

    }

    @Override
    public Application getApplicationById(int id, boolean requirePublishedReleases) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        Application application;
        boolean isAppAllowed = false;
        List<ApplicationRelease> applicationReleases;
        try {
            ConnectionManagerUtil.openDBConnection();
            application = this.applicationDAO.getApplicationById(id, tenantId);
            if (isAdminUser(userName, tenantId, CarbonConstants.UI_ADMIN_PERMISSION_COLLECTION)) {
                applicationReleases = getReleases(application, requirePublishedReleases);
                application.setApplicationReleases(applicationReleases);
                return application;
            }

            if (!application.getUnrestrictedRoles().isEmpty()) {
                if (isRoleExists(application.getUnrestrictedRoles(), userName)) {
                    isAppAllowed = true;
                }
            } else {
                isAppAllowed = true;
            }

            if (!isAppAllowed) {
                return null;
            }

            applicationReleases = getReleases(application, requirePublishedReleases);
            application.setApplicationReleases(applicationReleases);
            return application;
        } catch (UserStoreException e) {
            throw new ApplicationManagementException(
                    "User-store exception while getting application with the application id " + id);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    private boolean isRoleExists(Collection<UnrestrictedRole> unrestrictedRoleList, String userName)
            throws UserStoreException {
        String[] roleList;
        roleList = getRolesOfUser(userName);
        for (UnrestrictedRole unrestrictedRole : unrestrictedRoleList) {
            for (String role : roleList) {
                if (unrestrictedRole.getRole().equals(role)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String[] getRolesOfUser(String userName) throws UserStoreException {
        UserRealm userRealm = CarbonContext.getThreadLocalCarbonContext().getUserRealm();
        String[] roleList = {};
        if (userRealm != null) {
            roleList = userRealm.getUserStoreManager().getRoleListOfUser(userName);
        } else {
            log.error("role list is empty of user :" + userName);
        }
        return roleList;
    }

    public Application getApplication(String appType, String appName) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        Application application;
        boolean isAppAllowed = false;
        List<ApplicationRelease> applicationReleases;
        try {
            ConnectionManagerUtil.openDBConnection();
            application = this.applicationDAO.getApplication(appName, appType, tenantId);
            if (isAdminUser(userName, tenantId, CarbonConstants.UI_ADMIN_PERMISSION_COLLECTION)) {
                applicationReleases = getReleases(application, false);
                application.setApplicationReleases(applicationReleases);
                return application;
            }

            if (!application.getUnrestrictedRoles().isEmpty()) {
                if (isRoleExists(application.getUnrestrictedRoles(), userName)) {
                    isAppAllowed = true;
                }
            } else {
                isAppAllowed = true;
            }

            if (!isAppAllowed) {
                return null;
            }

            applicationReleases = getReleases(application, false);
            application.setApplicationReleases(applicationReleases);
            return application;
        } catch (UserStoreException e) {
            throw new ApplicationManagementException(
                    "User-store exception while getting application with the " + "application name " + appName);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public Application getApplicationByRelease(String appReleaseUUID) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        Application application;
        try {
            ConnectionManagerUtil.openDBConnection();
            application = this.applicationDAO.getApplicationByRelease(appReleaseUUID, tenantId);

            if (application.getUnrestrictedRoles().isEmpty() || isRoleExists(application.getUnrestrictedRoles(),
                                                                             userName)) {
                return application;
            }
            return null;
        } catch (UserStoreException e) {
            throw new ApplicationManagementException(
                    "User-store exception while getting application with the application UUID " + appReleaseUUID);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    public boolean verifyApplicationExistenceById(int appId) throws ApplicationManagementException {
        try {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
            boolean isAppExist;
            ConnectionManagerUtil.openDBConnection();
            isAppExist = this.applicationDAO.verifyApplicationExistenceById(appId, tenantId);
            return isAppExist;
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    public Boolean isUserAllowable(List<UnrestrictedRole> unrestrictedRoles, String userName)
            throws ApplicationManagementException {
        try {
            return isRoleExists(unrestrictedRoles, userName);
        } catch (UserStoreException e) {
            throw new ApplicationManagementException(
                    "User-store exception while verifying whether user have assigned" + "unrestricted roles or not", e);
        }
    }

    private List<ApplicationRelease> getReleases(Application application, boolean requirePublishedRelease)
            throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        List<ApplicationRelease> applicationReleases;
        List<ApplicationRelease> filteredApplicationReleases = new ArrayList<>();
        if (log.isDebugEnabled()) {
            log.debug("Request is received to retrieve all the releases related with the application " + application
                    .toString());
        }
        ConnectionManagerUtil.getDBConnection();
        applicationReleases = this.applicationReleaseDAO.getReleases(application.getId(), tenantId);
        for (ApplicationRelease applicationRelease : applicationReleases) {
            LifecycleState lifecycleState = null;
            try {
                lifecycleState = ApplicationManagementDAOFactory.getLifecycleStateDAO().
                        getLatestLifeCycleStateByReleaseID(applicationRelease.getId());
            } catch (LifeCycleManagementDAOException e) {
                throw new ApplicationManagementException(
                        "Error occurred while getting the latest lifecycle state for the application release UUID: "
                                + applicationRelease.getUuid(), e);
            }
            if (lifecycleState != null) {
                applicationRelease.setLifecycleState(lifecycleState);

                if (!AppLifecycleState.REMOVED.toString()
                        .equals(applicationRelease.getLifecycleState().getCurrentState())) {
                    if (requirePublishedRelease){
                        if (AppLifecycleState.PUBLISHED.toString()
                                .equals(applicationRelease.getLifecycleState().getCurrentState())){
                            filteredApplicationReleases.add(applicationRelease);
                        }
                    }else{
                        filteredApplicationReleases.add(applicationRelease);
                    }
                }
            }
        }

        if (requirePublishedRelease && filteredApplicationReleases.size() > 1) {
            log.error("There are more than one published application releases for application ID: " + application
                    .getId());
        }
        return filteredApplicationReleases;

    }



    @Override
    public List<String> deleteApplication(int applicationId) throws ApplicationManagementException {
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        List<String> storedLocations = new ArrayList<>();
        Application application;

        try {
            if (!isAdminUser(userName, tenantId, CarbonConstants.UI_ADMIN_PERMISSION_COLLECTION)) {
                throw new ApplicationManagementException(
                        "You don't have permission to delete this application. In order to delete an application you " +
                                "need to have admin permission");
            }

            application = getApplicationIfAccessible(applicationId);
            if ( application == null) {
                throw new ApplicationManagementException("Invalid Application");
            }
            List<ApplicationRelease> applicationReleases = getReleases(application, false);
            if (log.isDebugEnabled()) {
                log.debug("Request is received to delete applications which are related with the application id " +
                                  applicationId);
            }
            for (ApplicationRelease applicationRelease : applicationReleases) {
                LifecycleState appLifecycleState = getLifecycleState(applicationId, applicationRelease.getUuid());
                LifecycleState newAppLifecycleState = new LifecycleState();
                newAppLifecycleState.setPreviousState(appLifecycleState.getCurrentState());
                newAppLifecycleState.setCurrentState(AppLifecycleState.REMOVED.toString());
                changeLifecycleState(applicationId, applicationRelease.getUuid(), newAppLifecycleState, true);
                storedLocations.add(applicationRelease.getAppHashValue());
            }
            ConnectionManagerUtil.openDBConnection();
            this.applicationDAO.deleteApplication(applicationId);
        } catch (UserStoreException e) {
            String msg = "Error occured while check whether current user has the permission to delete an application";
            log.error(msg);
            throw new ApplicationManagementException(msg,e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
        return storedLocations;
    }

    @Override
    public String deleteApplicationRelease(int applicationId, String releaseUuid)
            throws ApplicationManagementException {
        Application application = getApplicationIfAccessible(applicationId);
        if (application == null) {
            throw new ApplicationManagementException("Invalid Application ID is received");
        }
        ApplicationRelease applicationRelease = getAppReleaseIfExists(applicationId, releaseUuid);
        LifecycleState appLifecycleState = getLifecycleState(applicationId, applicationRelease.getUuid());
        String currentState = appLifecycleState.getCurrentState();
        if (AppLifecycleState.DEPRECATED.toString().equals(currentState) || AppLifecycleState
                .REJECTED.toString().equals(currentState) || AppLifecycleState.UNPUBLISHED.toString().equals
                (currentState) ) {
            LifecycleState newAppLifecycleState = new LifecycleState();
            newAppLifecycleState.setPreviousState(appLifecycleState.getCurrentState());
            newAppLifecycleState.setCurrentState(AppLifecycleState.REMOVED.toString());
            changeLifecycleState(applicationId, applicationRelease.getUuid(), newAppLifecycleState, true);
        }else{
            throw new ApplicationManagementException("Can't delete the application release, You have to move the " +
                                                             "lifecycle state from "+ currentState + " to acceptable " +
                                                             "state") ;
        }
        return applicationRelease.getAppHashValue();
    }

    /**
     * To check whether current user has the permission to do some secured operation.
     *
     * @param username   Name of the User.
     * @param tenantId   ID of the tenant.
     * @param permission Permission that need to be checked.
     * @return true if the current user has the permission, otherwise false.
     * @throws UserStoreException UserStoreException
     */
    private boolean isAdminUser(String username, int tenantId, String permission) throws UserStoreException {
        UserRealm userRealm = DataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId);
        return userRealm != null && userRealm.getAuthorizationManager() != null && userRealm.getAuthorizationManager()
                .isUserAuthorized(MultitenantUtils.getTenantAwareUsername(username), permission,
                                  CarbonConstants.UI_PERMISSION_ACTION);
    }

    /**
     * To validate the application
     *
     * @param application Application that need to be created
     * @throws ValidationException Validation Exception
     */
    private void validateAppCreatingRequest(Application application) throws ValidationException {

        Boolean isValidApplicationType;
        try {
            if (application.getName() == null) {
                throw new ValidationException("Application name cannot be empty");
            }
            if (application.getUser() == null || application.getUser().getUserName() == null
                    || application.getUser().getTenantId() == -1) {
                throw new ValidationException("Username and tenant Id cannot be empty");
            }
            if (application.getAppCategory() == null) {
                throw new ValidationException("Application category can't be empty");
            }

            isValidApplicationType = isValidAppType(application);

            if (!isValidApplicationType) {
                throw new ValidationException(
                        "App Type contains in the application creating payload doesn't match with supported app types");
            }

            validateApplicationExistence(application);
        } catch (ApplicationManagementException e) {
            throw new ValidationException("Error occured while validating whether there is already an application "
                                                  + "registered with same name.", e);
        }
    }

    private Boolean isValidAppType(Application application) {
        for (ApplicationType applicationType : ApplicationType.values()) {
            if (applicationType.toString().equals(application.getType())) {
                return true;
            }
        }
        return false;
    }

    /**
     * To validate the application existence
     *
     * @param application Application that need to be validated
     * @throws ValidationException Validation Exception
     */
    private void validateApplicationExistence(Application application) throws ApplicationManagementException {
        Filter filter = new Filter();
        filter.setFullMatch(true);
        filter.setAppName(application.getName().trim());
        filter.setOffset(0);
        filter.setLimit(1);

        ApplicationList applicationList = getApplications(filter);
        if (applicationList != null && applicationList.getApplications() != null && !applicationList.getApplications()
                .isEmpty()) {
            throw new ApplicationManagementException(
                    "Already an application registered with same name - " + applicationList.getApplications().get(0)
                            .getName());
        }
    }

    /**
     * Get the application if application is an accessible one.
     *
     * @param applicationId ID of the Application.
     * @return Application related with the UUID
     */
    public Application getApplicationIfAccessible(int applicationId) throws ApplicationManagementException {
        if (applicationId <= 0) {
            throw new ApplicationManagementException("Application id could,t be a negative integer. Hence please add " +
                                                             "valid application id.");
        }
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        Application application;
        boolean isAppAllowed = false;
        try {
            application = this.applicationDAO.getApplicationById(applicationId, tenantId);
            if (isAdminUser(userName, tenantId, CarbonConstants.UI_ADMIN_PERMISSION_COLLECTION)) {
                return application;
            }

            if (application != null && !application.getUnrestrictedRoles().isEmpty()) {
                if (isRoleExists(application.getUnrestrictedRoles(), userName)) {
                    isAppAllowed = true;
                }
            } else {
                isAppAllowed = true;
            }

            if (!isAppAllowed) {
                throw new NotFoundException("Application of the " + applicationId
                        + " does not exist. Please check whether user have permissions to access the application.");
            }
            return application;
        } catch (UserStoreException e) {
            throw new ApplicationManagementException(
                    "User-store exception while getting application with the " + "application id " + applicationId, e);
        }
    }

    /**
     * Get the application release for given UUID if application release is exists and application id is valid one.
     *
     * @param applicationUuid UUID of the Application.
     * @return Application related with the UUID
     */
    public ApplicationRelease getAppReleaseIfExists(int applicationId, String applicationUuid) throws
                                                                                                    ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        ApplicationRelease applicationRelease;

        if (applicationId <= 0) {
            throw new ApplicationManagementException(
                    "Application id could,t be a negative integer. Hence please add " +
                            "valid application id.");
        }
        if (applicationUuid == null) {
            throw new ApplicationManagementException("Application UUID is null. Application UUID is a required "
                    + "parameter to get the relevant application.");
        }
        ConnectionManagerUtil.getDBConnection();
        applicationRelease = this.applicationReleaseDAO.getReleaseByIds(applicationId, applicationUuid, tenantId);
        if (applicationRelease == null) {
            throw new ApplicationManagementException("Doesn't exist a application release for application ID: " +
                    applicationId + "and application UUID: " +
                    applicationUuid);
        }
        return applicationRelease;

    }

    @Override
    public ApplicationRelease updateRelease(int appId, ApplicationRelease applicationRelease) throws
                                                                                              ApplicationManagementException {
        validateAppReleasePayload(applicationRelease);
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        if (log.isDebugEnabled()) {
            log.debug("Updating the Application release. UUID: " + applicationRelease.getUuid() + ", " +
                              "Application Id: " + appId);
        }
        try {
            ConnectionManagerUtil.openDBConnection();
            applicationRelease = this.applicationReleaseDAO.updateRelease(appId, applicationRelease, tenantId);
            return applicationRelease;
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public boolean isAcceptableAppReleaseUpdate(int appId, String appReleaseUuid)
            throws ApplicationManagementException {
        LifecycleState lifecycleState = getLifecycleState(appId, appReleaseUuid);
        return AppLifecycleState.CREATED.toString().equals(lifecycleState.getCurrentState()) || AppLifecycleState
                .IN_REVIEW.toString().equals(lifecycleState.getCurrentState()) ||
                AppLifecycleState.REJECTED.toString().equals(lifecycleState.getCurrentState());
    }

    /**
     * To get role restricted application list.
     *
     * @param applicationList list of applications.
     * @param userName        user name
     * @return Application related with the UUID
     */
    private ApplicationList getRoleRestrictedApplicationList(ApplicationList applicationList, String userName)
            throws ApplicationManagementException {
        ApplicationList roleRestrictedApplicationList = new ApplicationList();
        ArrayList<Application> unRestrictedApplications = new ArrayList<>();
        for (Application application : applicationList.getApplications()) {
            if (application.getUnrestrictedRoles().isEmpty()) {
                unRestrictedApplications.add(application);
            } else {
                try {
                    if (isRoleExists(application.getUnrestrictedRoles(), userName)) {
                        unRestrictedApplications.add(application);
                    }
                } catch (UserStoreException e) {
                    throw new ApplicationManagementException("Role restriction verifying is failed");
                }
            }
        }
        roleRestrictedApplicationList.setApplications(unRestrictedApplications);
        return roleRestrictedApplicationList;
    }

    /**
     * To validate a app release creating request and app updating request to make sure all the pre-conditions satisfied.
     *
     * @param applicationRelease ApplicationRelease that need to be created.
     * @throws ApplicationManagementException Application Management Exception.
     */
    private void validateAppReleasePayload(ApplicationRelease applicationRelease)
            throws ApplicationManagementException {

        if (applicationRelease.getVersion() == null) {
            throw new ApplicationManagementException("ApplicationRelease version name is a mandatory parameter for "
                                                             + "creating release. It cannot be found.");
        }
    }

    @Override
    public LifecycleState getLifecycleState(int applicationId, String releaseUuid) throws
                                                                                       ApplicationManagementException {
        LifecycleState lifecycleState;
        try {
            ConnectionManagerUtil.openDBConnection();
            lifecycleState = this.lifecycleStateDAO.getLatestLifeCycleState(applicationId, releaseUuid);
            if (lifecycleState == null) {
                throw new NotFoundException(
                        "Couldn't find the lifecycle data for appid: " + applicationId + " and app release UUID: "
                                + releaseUuid);

            }
            lifecycleState.setNextStates(new ArrayList<>(getLifecycleManagementService().
                    getNextLifecycleStates(lifecycleState.getCurrentState())));
        } catch (LifeCycleManagementDAOException e) {
            throw new ApplicationManagementException("Failed to get lifecycle state from database", e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
        return lifecycleState;
    }

    @Override
    public void changeLifecycleState(int applicationId, String releaseUuid, LifecycleState state, Boolean checkExist)
            throws ApplicationManagementException {
        try {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
            if (checkExist) {
                ConnectionManagerUtil.openDBConnection();
                if (!this.applicationDAO.verifyApplicationExistenceById(applicationId, tenantId)){
                    throw new NotFoundException(
                            "Couldn't found application for the application Id: " + applicationId);
                }
                if (!this.applicationReleaseDAO.verifyReleaseExistence(applicationId, releaseUuid, tenantId)){
                    throw new NotFoundException(
                            "Couldn't found application release for the application Id: " + applicationId
                                    + " application release uuid: " + releaseUuid);
                }
            }

            String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
            state.setUpdatedBy(userName);

            if (state.getCurrentState() != null && state.getPreviousState() != null) {
                if (getLifecycleManagementService()
                        .isValidStateChange(state.getPreviousState(), state.getCurrentState())) {
                    ConnectionManagerUtil.beginDBTransaction();
                    //todo if current state of the adding lifecycle state is PUBLISHED, need to check whether is there
                    //todo any other application release in PUBLISHED state for the application( i.e for the appid)
                    this.lifecycleStateDAO.addLifecycleState(state, applicationId, releaseUuid, tenantId);
                    ConnectionManagerUtil.commitDBTransaction();
                } else {
                    log.error("Invalid lifecycle state transition from '" + state.getPreviousState() + "'" + " to '"
                            + state.getCurrentState() + "'");
                    throw new ApplicationManagementException("Lifecycle State Validation failed. Application Id: " +
                            applicationId + " Application release UUID: "  + releaseUuid);
                }
            }
        } catch (LifeCycleManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new ApplicationManagementException(
                    "Failed to add lifecycle state. Application Id: " + applicationId + " Application release UUID: "
                            + releaseUuid, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public Application updateApplication(Application application) throws ApplicationManagementException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        Application existingApplication = getApplicationIfAccessible(application.getId());
        List<UnrestrictedRole> addingRoleList;
        List<UnrestrictedRole> removingRoleList;
        List<Tag> addingTags;
        List<Tag> removingTags;


        if (existingApplication == null) {
            throw new NotFoundException("Tried to update Application which is not in the publisher, " +
                                                "Please verify application details");
        }
        if (!existingApplication.getType().equals(application.getType())) {
            throw new ApplicationManagementException("You are trying to change the application type and it is not " +
                                                             "possible after you create an application. Therefore " +
                                                             "please remove this application and publish " +
                                                             "new application with type: " + application.getType());
        }
        if (!existingApplication.getSubType().equals(application.getSubType())) {
            if (ApplicationSubscriptionType.PAID.toString().equals(existingApplication.getSubType()) && (
                    !"".equals(application.getPaymentCurrency()) || application.getPaymentCurrency() != null)) {
                throw new ApplicationManagementException("If you are going to change Non-Free app as Free app, "
                        + "currency attribute in the application updating " + "payload should be null or \"\"");
            } else if (ApplicationSubscriptionType.FREE.toString().equals(existingApplication.getSubType()) && (
                    application.getPaymentCurrency() == null || "".equals(application.getPaymentCurrency()))) {
                throw new ApplicationManagementException("If you are going to change Free app as Non-Free app, "
                        + "currency attribute in the application payload " + "should not be null or \"\"");
            }
        }
        if (existingApplication.getIsRestricted() != application.getIsRestricted()) {
            if (!existingApplication.getIsRestricted() && existingApplication.getUnrestrictedRoles() == null) {
                if (application.getUnrestrictedRoles() == null || application.getUnrestrictedRoles().isEmpty()) {
                    throw new ApplicationManagementException("If you are going to add role restriction for non role "
                            + "restricted Application, Unrestricted role list " + "won't be empty or null");
                }
                visibilityDAO.addUnrestrictedRoles(application.getUnrestrictedRoles(), application.getId(), tenantId);
            } else if (existingApplication.getIsRestricted() && existingApplication.getUnrestrictedRoles() != null) {
                if (application.getUnrestrictedRoles() != null && !application.getUnrestrictedRoles().isEmpty()) {
                    throw new ApplicationManagementException("If you are going to remove role restriction from role "
                            + "restricted Application, Unrestricted role list should be empty or null");
                }
                visibilityDAO.deleteUnrestrictedRoles(existingApplication.getUnrestrictedRoles(), application.getId(),
                        tenantId);
            }
        } else if (existingApplication.getIsRestricted() == application.getIsRestricted()
                && existingApplication.getIsRestricted()) {
            addingRoleList = getDifference(application.getUnrestrictedRoles(),
                    existingApplication.getUnrestrictedRoles());
            removingRoleList = getDifference(existingApplication.getUnrestrictedRoles(),
                    application.getUnrestrictedRoles());
            if (!addingRoleList.isEmpty()) {
                visibilityDAO.addUnrestrictedRoles(addingRoleList, application.getId(), tenantId);

            }
            if (!removingRoleList.isEmpty()) {
                visibilityDAO.deleteUnrestrictedRoles(removingRoleList, application.getId(), tenantId);
            }
        }
        addingTags = getDifference(existingApplication.getTags(), application.getTags());
        removingTags = getDifference(application.getTags(), existingApplication.getTags());
        if (!addingTags.isEmpty()) {
            applicationDAO.addTags(addingTags, application.getId(), tenantId);
        }
        if (!removingTags.isEmpty()) {
            applicationDAO.deleteTags(removingTags, application.getId(), tenantId);
        }

        return applicationDAO.editApplication(application, tenantId);
    }

    private Filter validateFilter(Filter filter) {
        if (filter != null) {
            if (filter.getAppType() != null) {
                boolean isValidRequest = false;
                for (ApplicationType applicationType : ApplicationType.values()) {
                    if (applicationType.toString().equals(filter.getAppType())) {
                        isValidRequest = true;
                        break;
                    }
                }
                if (!isValidRequest) {
                    return null;
                }
            }
        }
        return filter;
    }

    private <T> List<T> getDifference(List<T> list1, Collection<T> list2) {
        List<T> list = new ArrayList<>();
        for (T t : list1) {
            if(!list2.contains(t)) {
                list.add(t);
            }
        }
        return list;
    }

    public LifecycleStateManger getLifecycleManagementService() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        LifecycleStateManger deviceManagementProviderService =
                (LifecycleStateManger) ctx.getOSGiService(LifecycleStateManger.class, null);
        if (deviceManagementProviderService == null) {
            String msg = "DeviceImpl Management provider service has not initialized.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return deviceManagementProviderService;
    }
}
