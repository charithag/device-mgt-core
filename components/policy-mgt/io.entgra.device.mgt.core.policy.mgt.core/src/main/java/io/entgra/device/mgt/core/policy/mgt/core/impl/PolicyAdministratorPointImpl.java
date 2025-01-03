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

package io.entgra.device.mgt.core.policy.mgt.core.impl;

import io.entgra.device.mgt.core.device.mgt.common.PolicyPaginationRequest;
import io.entgra.device.mgt.core.device.mgt.extensions.logger.spi.EntgraLogger;
import io.entgra.device.mgt.core.notification.logger.PolicyLogContext;
import io.entgra.device.mgt.core.notification.logger.impl.EntgraPolicyLoggerImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import io.entgra.device.mgt.core.device.mgt.common.DeviceIdentifier;
import io.entgra.device.mgt.core.device.mgt.common.PaginationRequest;
import io.entgra.device.mgt.core.device.mgt.common.policy.mgt.Policy;
import io.entgra.device.mgt.core.device.mgt.common.policy.mgt.Profile;
import io.entgra.device.mgt.core.device.mgt.core.config.DeviceConfigurationManager;
import io.entgra.device.mgt.core.device.mgt.core.config.policy.PolicyConfiguration;
import io.entgra.device.mgt.core.policy.mgt.common.PolicyAdministratorPoint;
import io.entgra.device.mgt.core.policy.mgt.common.PolicyManagementException;
import io.entgra.device.mgt.core.policy.mgt.common.ProfileManagementException;
import io.entgra.device.mgt.core.policy.mgt.core.cache.PolicyCacheManager;
import io.entgra.device.mgt.core.policy.mgt.core.cache.impl.PolicyCacheManagerImpl;
import io.entgra.device.mgt.core.policy.mgt.core.internal.PolicyManagementDataHolder;
import io.entgra.device.mgt.core.policy.mgt.core.mgt.PolicyManager;
import io.entgra.device.mgt.core.policy.mgt.core.mgt.ProfileManager;
import io.entgra.device.mgt.core.policy.mgt.core.mgt.impl.PolicyManagerImpl;
import io.entgra.device.mgt.core.policy.mgt.core.mgt.impl.ProfileManagerImpl;
import io.entgra.device.mgt.core.policy.mgt.core.util.PolicyManagementConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.ntask.core.service.TaskService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PolicyAdministratorPointImpl implements PolicyAdministratorPoint {

    PolicyLogContext.Builder policyLogContextBuilder = new PolicyLogContext.Builder();
    private static final EntgraLogger log = new EntgraPolicyLoggerImpl(PolicyAdministratorPointImpl.class);

    private PolicyManager policyManager;
    private ProfileManager profileManager;
    private PolicyConfiguration policyConfiguration;

    public PolicyAdministratorPointImpl() {
        this.policyManager = new PolicyManagerImpl();
        this.profileManager = new ProfileManagerImpl();
        this.policyConfiguration = DeviceConfigurationManager.getInstance().getDeviceManagementConfig()
                .getPolicyConfiguration();
    }

    @Override
    public Policy addPolicy(Policy policy) throws PolicyManagementException {
        Policy resultantPolicy = policyManager.addPolicy(policy);
        if (policyConfiguration.getCacheEnable()) {
            PolicyCacheManagerImpl.getInstance().rePopulateCache();
        }
        return resultantPolicy;
    }

    @Override
    public Policy updatePolicy(Policy policy) throws PolicyManagementException {
        Policy resultantPolicy = policyManager.updatePolicy(policy);
        if (policyConfiguration.getCacheEnable()) {
            PolicyCacheManagerImpl.getInstance().rePopulateCache();
        }
        return resultantPolicy;
    }

    @Override
    public boolean updatePolicyPriorities(List<Policy> policies) throws PolicyManagementException {
        boolean bool = policyManager.updatePolicyPriorities(policies);
        if (policyConfiguration.getCacheEnable()) {
            PolicyCacheManagerImpl.getInstance().rePopulateCache();
        }
        return bool;
    }

    @Override
    public void activatePolicy(int policyId) throws PolicyManagementException {
        policyManager.activatePolicy(policyId);
        if (policyConfiguration.getCacheEnable()) {
            PolicyCacheManagerImpl.getInstance().rePopulateCache();
        }
    }

    @Override
    public void inactivatePolicy(int policyId) throws PolicyManagementException {
        policyManager.inactivatePolicy(policyId);
        if (policyConfiguration.getCacheEnable()) {
            PolicyCacheManagerImpl.getInstance().rePopulateCache();
        }
    }

    @Override
    public boolean deletePolicy(Policy policy) throws PolicyManagementException {
        boolean bool = policyManager.deletePolicy(policy);
        if (policyConfiguration.getCacheEnable()) {
            PolicyCacheManager policyCacheManager = PolicyCacheManagerImpl.getInstance();
            policyCacheManager.rePopulateCache();
        }
        return bool;
    }

    @Override
    public boolean deletePolicy(int policyId) throws PolicyManagementException {
        boolean bool = policyManager.deletePolicy(policyId);
        if (policyConfiguration.getCacheEnable()) {
            PolicyCacheManager policyCacheManager = PolicyCacheManagerImpl.getInstance();
            policyCacheManager.rePopulateCache();
        }
        return bool;
    }

    @Override
    public void publishChanges() throws PolicyManagementException {

        try {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
            TaskService taskService = PolicyManagementDataHolder.getInstance().getTaskService();

            if (log.isDebugEnabled()) {
                log.debug("Policy delegations task is started for the tenant id " + tenantId);
            }

            TaskManager taskManager = taskService.getTaskManager(PolicyManagementConstants.DELEGATION_TASK_TYPE);
            TaskInfo.TriggerInfo triggerInfo = new TaskInfo.TriggerInfo();
            triggerInfo.setRepeatCount(0);
            Map<String, String> properties = new HashMap<>();
            properties.put(PolicyManagementConstants.TENANT_ID, String.valueOf(tenantId));
            String taskName = PolicyManagementConstants.DELEGATION_TASK_NAME + "_" + String.valueOf(tenantId);

            Set<String> registeredTaskTypes = taskService.getRegisteredTaskTypes();
            //Check whether the TaskType is already registered. If not we'll register it here.
            if (!registeredTaskTypes.contains(PolicyManagementConstants.DELEGATION_TASK_TYPE)) {
                taskService.registerTaskType(PolicyManagementConstants.DELEGATION_TASK_TYPE);
                // Delete init task if exists
                if (taskManager.isTaskScheduled(taskName)) {
                    taskManager.deleteTask(taskName);
                }
            }

            TaskInfo registeredTaskInfo = null;
            // getTask method will throw a TaskException if the task is not registered. Hence we'll handle the
            // exception and register the task.
            try {
                registeredTaskInfo = taskManager.getTask(taskName);
            } catch (TaskException e) {
                // No need of any specific logic to handle this exception as it is thrown if the task is not registered.
            } finally {
                // If registeredTaskInfo is null that means there's no registered delegation-task.
                if (registeredTaskInfo == null) {
                    TaskInfo taskInfo = new TaskInfo(taskName, PolicyManagementConstants.DELEGATION_TASK_CLAZZ,
                            properties, triggerInfo);
                    taskManager.registerTask(taskInfo);
                    taskManager.scheduleTask(taskInfo.getName());
                    log.info("Apply changes to device", policyLogContextBuilder.setActionTag("PUBLISH_CHANGES").setUserName(userName).setTenantID(String.valueOf(tenantId)).setTenantDomain(tenantDomain).build());
                } else {
                    if (!taskManager.isTaskScheduled(taskName)) {
                        TaskInfo taskInfo = new TaskInfo(taskName, PolicyManagementConstants.DELEGATION_TASK_CLAZZ,
                                properties, triggerInfo);
                        taskManager.scheduleTask(taskInfo.getName());
                        log.info("Apply changes to device", policyLogContextBuilder.setActionTag("PUBLISH_CHANGES").setUserName(userName).setTenantID(String.valueOf(tenantId)).setTenantDomain(tenantDomain).build());
                    } else {
                        throw new PolicyManagementException("There is a task already running for policy changes. Please try " +
                                "to apply " +
                                "changes after few minutes.");
                    }
                }
            }
        } catch (TaskException e) {
            String msg = "Error occurred while creating the policy delegation task for tenant " +
                    PrivilegedCarbonContext.
                            getThreadLocalCarbonContext().getTenantId();
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        }
    }

    @Override
    public Policy addPolicyToDevice(List<DeviceIdentifier> deviceIdentifierList, Policy policy) throws
            PolicyManagementException {
        policy = policyManager.addPolicyToDevice(deviceIdentifierList, policy);
        if (policyConfiguration.getCacheEnable()) {
            PolicyCacheManagerImpl.getInstance().rePopulateCache();
        }
        return policy;
    }

    @Override
    public Policy addPolicyToRole(List<String> roleNames, Policy policy) throws PolicyManagementException {
        policy = policyManager.addPolicyToRole(roleNames, policy);
        if (policyConfiguration.getCacheEnable()) {
            PolicyCacheManagerImpl.getInstance().rePopulateCache();
        }
        return policy;
    }

    @Override
    public List<Policy> getPolicies() throws PolicyManagementException {
        if (policyConfiguration.getCacheEnable()) {
            return PolicyCacheManagerImpl.getInstance().getAllPolicies();
        } else {
            return policyManager.getPolicies();
        }
    }

    @Override
    public Policy getPolicy(int policyId) throws PolicyManagementException {
        if (policyConfiguration.getCacheEnable()) {
            return PolicyCacheManagerImpl.getInstance().getPolicy(policyId);
        } else {
            return policyManager.getPolicy(policyId);
        }
    }

    @Override
    public List<Policy> getPoliciesOfDevice(DeviceIdentifier deviceIdentifier) throws PolicyManagementException {
        return policyManager.getPoliciesOfDevice(deviceIdentifier);
    }

    @Override
    public List<Policy> getPoliciesOfDeviceType(String deviceType) throws PolicyManagementException {
        return policyManager.getPoliciesOfDeviceType(deviceType);
    }

    @Override
    public List<Policy> getPoliciesOfRole(String roleName) throws PolicyManagementException {
        return policyManager.getPoliciesOfRole(roleName);
    }

    @Override
    public List<Policy> getPoliciesOfUser(String username) throws PolicyManagementException {
        return policyManager.getPoliciesOfUser(username);
    }

    @Override
    public boolean isPolicyAvailableForDevice(DeviceIdentifier deviceIdentifier) throws PolicyManagementException {
        return policyManager.checkPolicyAvailable(deviceIdentifier);
    }

    @Override
    public boolean isPolicyApplied(DeviceIdentifier deviceIdentifier) throws PolicyManagementException {
        return policyManager.setPolicyApplied(deviceIdentifier);
    }

    @Override
    public void setPolicyUsed(DeviceIdentifier deviceIdentifier, Policy policy) throws PolicyManagementException {
        policyManager.addAppliedPolicyToDevice(deviceIdentifier, policy);
    }

    @Override
    public void removePolicyUsed(DeviceIdentifier deviceIdentifier) throws PolicyManagementException {
        policyManager.removeAppliedPolicyToDevice(deviceIdentifier);
    }

    @Override
    public Profile addProfile(Profile profile) throws PolicyManagementException {
        try {
            return profileManager.addProfile(profile);
        } catch (ProfileManagementException e) {
            String msg = "Error occurred while persisting the policy.";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        }
    }

    @Override
    public boolean deleteProfile(Profile profile) throws PolicyManagementException {
        try {
            return profileManager.deleteProfile(profile);
        } catch (ProfileManagementException e) {
            String msg = "Error occurred while deleting the profile.";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        }
    }

    @Override
    public Profile updateProfile(Profile profile) throws PolicyManagementException {
        try {
            return profileManager.updateProfile(profile);
        } catch (ProfileManagementException e) {
            String msg = "Error occurred while persisting the profile.";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        }
    }

    @Override
    public Profile getProfile(int profileId) throws PolicyManagementException {
        try {
            return profileManager.getProfile(profileId);
        } catch (ProfileManagementException e) {
            String msg = "Error occurred while retrieving profile";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        }
    }

    @Override
    public List<Profile> getProfiles() throws PolicyManagementException {
        try {
            return profileManager.getAllProfiles();
        } catch (ProfileManagementException e) {
            String msg = "Error occurred while obtaining list of profiles.";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        }
    }

    @Override
    public int getPolicyCount() throws PolicyManagementException {
        if (policyConfiguration.getCacheEnable()) {
            return PolicyCacheManagerImpl.getInstance().getAllPolicies().size();
        } else {
            return policyManager.getPolicyCount();
        }
    }

    @Override
    public List<Policy> getPolicies(String policyType) throws PolicyManagementException {
        if (policyConfiguration.getCacheEnable()) {
            return PolicyCacheManagerImpl.getInstance().getAllPolicies(policyType);
        } else {
            return policyManager.getPolicies(policyType);
        }
    }

    @Override
    public List<Policy> getPolicyList(PolicyPaginationRequest request) throws PolicyManagementException {
        return policyManager.getPolicyList(request);
    }
}
