/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.policy.mgt.core.mgt.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.exceptions.InvalidDeviceException;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.common.policy.mgt.Policy;
import org.wso2.carbon.device.mgt.common.policy.mgt.PolicyMonitoringManager;
import org.wso2.carbon.device.mgt.common.policy.mgt.ProfileFeature;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.ComplianceData;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.ComplianceFeature;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.NonComplianceData;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.PolicyComplianceException;
import org.wso2.carbon.device.mgt.core.operation.mgt.CommandOperation;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.policy.mgt.common.PolicyManagementException;
import org.wso2.carbon.policy.mgt.common.monitor.ComplianceDecisionPoint;
import org.wso2.carbon.policy.mgt.common.monitor.PolicyDeviceWrapper;
import org.wso2.carbon.policy.mgt.core.dao.MonitoringDAO;
import org.wso2.carbon.policy.mgt.core.dao.MonitoringDAOException;
import org.wso2.carbon.policy.mgt.core.dao.PolicyDAO;
import org.wso2.carbon.policy.mgt.core.dao.PolicyManagementDAOFactory;
import org.wso2.carbon.policy.mgt.core.dao.PolicyManagerDAOException;
import org.wso2.carbon.policy.mgt.core.impl.ComplianceDecisionPointImpl;
import org.wso2.carbon.policy.mgt.core.internal.PolicyManagementDataHolder;
import org.wso2.carbon.policy.mgt.core.mgt.MonitoringManager;
import org.wso2.carbon.policy.mgt.core.mgt.PolicyManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MonitoringManagerImpl implements MonitoringManager {

    private PolicyDAO policyDAO;
    private MonitoringDAO monitoringDAO;
    private ComplianceDecisionPoint complianceDecisionPoint;

    private static final Log log = LogFactory.getLog(MonitoringManagerImpl.class);
    private static final String OPERATION_MONITOR = "MONITOR";


    public MonitoringManagerImpl() {
        this.policyDAO = PolicyManagementDAOFactory.getPolicyDAO();
        this.monitoringDAO = PolicyManagementDAOFactory.getMonitoringDAO();
        this.complianceDecisionPoint = new ComplianceDecisionPointImpl();
    }

    @Override
    @Deprecated
    public List<ComplianceFeature> checkPolicyCompliance(DeviceIdentifier deviceIdentifier, Object deviceResponse)
            throws PolicyComplianceException {
        DeviceManagementProviderService service =
                PolicyManagementDataHolder.getInstance().getDeviceManagementService();
        Device device;
        try {
            device = service.getDevice(deviceIdentifier, false);
        } catch (DeviceManagementException e) {
            throw new PolicyComplianceException("Unable tor retrieve device data from DB for " +
                    deviceIdentifier.getId() + " - " + deviceIdentifier.getType(), e);
        }
        return checkPolicyCompliance(device, deviceResponse);
    }

    @Override
    public List<ComplianceFeature> checkPolicyCompliance(Device device, Object deviceResponse)
            throws PolicyComplianceException {
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier(device.getDeviceIdentifier(), device.getType());
        List<ComplianceFeature> complianceFeatures = new ArrayList<>();
        try {
            PolicyManager manager = PolicyManagementDataHolder.getInstance().getPolicyManager();
            Policy policy = manager.getAppliedPolicyToDevice(device);
            if (policy != null) {
                PolicyMonitoringManager monitoringService = PolicyManagementDataHolder.getInstance().
                        getDeviceManagementService().getPolicyMonitoringManager(device.getType());

                NonComplianceData complianceData;
                // This was retrieved from database because compliance id must be present for other dao operations to
                // run.
                try {
                    PolicyManagementDAOFactory.openConnection();
                    NonComplianceData cmd = monitoringDAO.getCompliance(device.getId(),
                            device.getEnrolmentInfo().getId());
                    complianceData = monitoringService.checkPolicyCompliance(deviceIdentifier, policy, deviceResponse);
                    if (cmd != null) {
                        complianceData.setId(cmd.getId());
                        complianceData.setPolicy(policy);
                        complianceFeatures = complianceData.getComplianceFeatures();
                        complianceData.setDeviceId(device.getId());
                        complianceData.setPolicyId(policy.getId());
                    }

                } catch (SQLException e) {
                    throw new PolicyComplianceException("Error occurred while opening a data source connection", e);
                } catch (MonitoringDAOException e) {
                    throw new PolicyComplianceException(
                            "Unable to add the none compliance features to database for device " +
                                    device.getDeviceIdentifier() + " - " + device.getType(), e);
                } finally {
                    PolicyManagementDAOFactory.closeConnection();
                }

                //This was added because update query below that did not return the update table primary key.

                if (complianceFeatures != null && !complianceFeatures.isEmpty()) {
                    try {
                        PolicyManagementDAOFactory.beginTransaction();
                        monitoringDAO.setDeviceAsNoneCompliance(device.getId(), device.getEnrolmentInfo().getId(),
                                policy.getId());
                        if (log.isDebugEnabled()) {
                            log.debug("Compliance status primary key " + complianceData.getId());
                        }
                        monitoringDAO.deleteNoneComplianceData(complianceData.getId());
                        monitoringDAO.addNonComplianceFeatures(complianceData.getId(), device.getId(),
                                                               complianceFeatures);

                        PolicyManagementDAOFactory.commitTransaction();
                    } catch (MonitoringDAOException e) {
                        PolicyManagementDAOFactory.rollbackTransaction();
                        throw new PolicyComplianceException(
                                "Unable to add the none compliance features to database for device " +
                                device.getDeviceIdentifier() + " - " + device.getType(), e);
                    } finally {
                        PolicyManagementDAOFactory.closeConnection();
                    }
                    complianceDecisionPoint.validateDevicePolicyCompliance(deviceIdentifier, complianceData);
                    List<ProfileFeature> profileFeatures = policy.getProfile().getProfileFeaturesList();
                    for (ComplianceFeature compFeature : complianceFeatures) {
                        for (ProfileFeature profFeature : profileFeatures) {
                            if (profFeature.getFeatureCode().equalsIgnoreCase(compFeature.getFeatureCode())) {
                                compFeature.setFeature(profFeature);
                            }
                        }
                    }
                } else {
                    try {
                        PolicyManagementDAOFactory.beginTransaction();
                        monitoringDAO.setDeviceAsCompliance(device.getId(), device.getEnrolmentInfo().getId(), policy
                                .getId());
                        monitoringDAO.deleteNoneComplianceData(complianceData.getId());
                        PolicyManagementDAOFactory.commitTransaction();
                    } catch (MonitoringDAOException e) {
                        PolicyManagementDAOFactory.rollbackTransaction();
                        throw new PolicyComplianceException(
                                "Unable to remove the none compliance features from database for device " +
                                deviceIdentifier.getId() + " - " + deviceIdentifier.getType(), e);
                    } finally {
                        PolicyManagementDAOFactory.closeConnection();
                    }
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("There is no policy applied to this device, hence compliance monitoring was not called.");
                }
            }
        } catch (PolicyManagerDAOException | PolicyManagementException e) {
            throw new PolicyComplianceException("Unable tor retrieve policy data from DB for device " +
                                                deviceIdentifier.getId() + " - " + deviceIdentifier.getType(), e);
        }
        return complianceFeatures;
    }

    @Override
    public boolean isCompliant(DeviceIdentifier deviceIdentifier) throws PolicyComplianceException {
        Device device;
        try {
            DeviceManagementProviderService service =
                    PolicyManagementDataHolder.getInstance().getDeviceManagementService();
            device = service.getDevice(deviceIdentifier, false);
        } catch (DeviceManagementException e) {
            throw new PolicyComplianceException("Unable to retrieve device data for " + deviceIdentifier.getId() +
                    " - " + deviceIdentifier.getType(), e);
        }

        try {
            PolicyManagementDAOFactory.openConnection();
            NonComplianceData complianceData = monitoringDAO.getCompliance(device.getId(), device.getEnrolmentInfo()
                    .getId());
            if (complianceData != null && !complianceData.isStatus()) {
                return false;
            }
        } catch (MonitoringDAOException e) {
            throw new PolicyComplianceException("Unable to retrieve compliance status for " + deviceIdentifier.getId() +
                    " - " + deviceIdentifier.getType(), e);
        } catch (SQLException e) {
            throw new PolicyComplianceException("Error occurred while opening a connection to the data source", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
        return true;
    }

    @Override
    @Deprecated
    public NonComplianceData getDevicePolicyCompliance(DeviceIdentifier deviceIdentifier) throws
            PolicyComplianceException {
        DeviceManagementProviderService service =
                PolicyManagementDataHolder.getInstance().getDeviceManagementService();
        Device device;
        try {
            device = service.getDevice(deviceIdentifier, false);
        } catch (DeviceManagementException e) {
            throw new PolicyComplianceException("Unable to retrieve device data for " + deviceIdentifier.getId() +
                    " - " + deviceIdentifier.getType(), e);
        }
        return getDevicePolicyCompliance(device);
    }

    @Override
    public NonComplianceData getDevicePolicyCompliance(Device device) throws PolicyComplianceException {
        NonComplianceData complianceData;
        try {
            PolicyManagementDAOFactory.openConnection();
            complianceData = monitoringDAO.getCompliance(device.getId(), device.getEnrolmentInfo().getId());
            List<ComplianceFeature> complianceFeatures =
                    monitoringDAO.getNoneComplianceFeatures(complianceData.getId());
            complianceData.setComplianceFeatures(complianceFeatures);
        } catch (MonitoringDAOException e) {
            throw new PolicyComplianceException("Unable to retrieve compliance data for " + device.getType() +
                    " device " + device.getDeviceIdentifier(), e);
        } catch (SQLException e) {
            throw new PolicyComplianceException("Error occurred while opening a connection to the data source", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
        return complianceData;
    }

    @Override
    public void addMonitoringOperation(List<Device> devices) throws PolicyComplianceException {

        //int tenantId = PolicyManagerUtil.getTenantId();
        Map<Integer, Device> deviceIds = new HashMap<>();
        List<NonComplianceData> complianceDatas = new ArrayList<>();
        HashMap<Integer, Integer> devicePolicyIdMap = new HashMap<>();

        try {
            PolicyManagementDAOFactory.openConnection();
            //TODO: Return a map from getCompliance to reduce O(n^2) -> O(n)
            List<NonComplianceData> cd = monitoringDAO.getCompliance();

            for (Device device : devices) {
                deviceIds.put(device.getId(), device);

                for (NonComplianceData data : cd) {
                    if (device.getId() == data.getDeviceId() && device.getEnrolmentInfo().getId() == data
                            .getEnrolmentId()) {
                        complianceDatas.add(data);
                    }
                }
            }
            List<Integer> deviceIDs = new ArrayList<>(deviceIds.keySet());

            HashMap<Integer, Integer> temp = policyDAO.getAppliedPolicyIds();
            for (Integer id : deviceIDs) {
                if (temp != null && !temp.isEmpty() && temp.containsKey(id)) {
                    devicePolicyIdMap.put(id, temp.get(id));
                }
            }

        } catch (SQLException e) {
            throw new PolicyComplianceException("SQL error occurred while getting monitoring details.", e);
        } catch (MonitoringDAOException e) {
            throw new PolicyComplianceException("SQL error occurred while getting monitoring details.", e);
        } catch (PolicyManagerDAOException e) {
            throw new PolicyComplianceException("SQL error occurred while getting policy details.", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }

        Map<Integer, Device> deviceIdsToAddOperation = new HashMap<>();
        Map<Integer, Device> deviceIdsWithExistingOperation = new HashMap<>();

        List<PolicyDeviceWrapper> firstTimeDevices = new ArrayList<>();

        Map<Integer, NonComplianceData> tempMap = new HashMap<>();

        try {
            if (complianceDatas != null || !complianceDatas.isEmpty()) {
                for (NonComplianceData complianceData : complianceDatas) {

                    tempMap.put(complianceData.getDeviceId(), complianceData);

                    if (complianceData.getAttempts() == 0) {
                        deviceIdsToAddOperation.put(complianceData.getDeviceId(),
                                deviceIds.get(complianceData.getDeviceId()));
                    } else {
                        deviceIdsWithExistingOperation.put(complianceData.getDeviceId(),
                                deviceIds.get(complianceData.getDeviceId()));
                    }
                }
            }

            for (Device device : devices) {
                if ((!tempMap.containsKey(device.getId())) && (devicePolicyIdMap.containsKey(device.getId()))) {
                    deviceIdsToAddOperation.put(device.getId(), device);

                    PolicyDeviceWrapper policyDeviceWrapper = new PolicyDeviceWrapper();
                    policyDeviceWrapper.setDeviceId(device.getId());
                    policyDeviceWrapper.setEnrolmentId(device.getEnrolmentInfo().getId());
                    policyDeviceWrapper.setPolicyId(devicePolicyIdMap.get(device.getId()));

                    firstTimeDevices.add(policyDeviceWrapper);

                    // firstTimeDeviceIdsWithPolicyIds.put(device.getId(), devicePolicyIdMap.get(device.getId()));
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("These devices are in the system for the first time");
                for (PolicyDeviceWrapper wrapper : firstTimeDevices) {
                    log.debug("First time device primary key : " + wrapper.getDeviceId() + " & policy id " +
                            wrapper.getPolicyId());
                }
            }

            PolicyManagementDAOFactory.beginTransaction();

            if (!deviceIdsToAddOperation.isEmpty()) {
                //                monitoringDAO.addComplianceDetails(firstTimeDeviceIdsWithPolicyIds);
                monitoringDAO.addComplianceDetails(firstTimeDevices);
                monitoringDAO.updateAttempts(new ArrayList<>(deviceIdsToAddOperation.keySet()), false);
            }

            if (!deviceIdsWithExistingOperation.isEmpty()) {
                monitoringDAO.updateAttempts(new ArrayList<>(deviceIdsWithExistingOperation.keySet()), false);
            }
            PolicyManagementDAOFactory.commitTransaction();

        } catch (MonitoringDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyComplianceException("Error occurred from monitoring dao.", e);
        } catch (PolicyManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyComplianceException("Error occurred reading the applied policies to devices.", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }

        if (!deviceIdsToAddOperation.isEmpty()) {
            try {
                this.addMonitoringOperationsToDatabase(new ArrayList<>(deviceIdsToAddOperation.values()));
            } catch (InvalidDeviceException e) {
                throw new PolicyComplianceException("Invalid Device Identifiers found.", e);
            } catch (OperationManagementException e) {
                throw new PolicyComplianceException("Error occurred while adding monitoring operation to devices", e);
            }
        }
    }

    @Override
    public List<String> getDeviceTypes() throws PolicyComplianceException {

        List<String> deviceTypes = new ArrayList<>();
        try {
            //when shutdown, it sets DeviceManagementService to null, therefore need to have a null check
            if (PolicyManagementDataHolder.getInstance().getDeviceManagementService() != null) {
                deviceTypes =
                        PolicyManagementDataHolder.getInstance().getDeviceManagementService().getPolicyMonitoringEnableDeviceTypes();
            }
        } catch (DeviceManagementException e) {
            throw new PolicyComplianceException("Error occurred while getting the device types.", e);
        }
        return deviceTypes;
    }

    @Override
    public PaginationResult getPolicyCompliance(
            PaginationRequest paginationRequest, String policyId,
            boolean complianceStatus, boolean isPending, String fromDate, String toDate)
            throws PolicyComplianceException {
        PaginationResult paginationResult = new PaginationResult();
        try {
            PolicyManagementDAOFactory.openConnection();
            List<ComplianceData> complianceDataList = monitoringDAO
                    .getAllComplianceDevices(paginationRequest, policyId, complianceStatus, isPending, fromDate, toDate);
            paginationResult.setData(complianceDataList);
            paginationResult.setRecordsTotal(complianceDataList.size());
        } catch (MonitoringDAOException e) {
            String msg = "Unable to retrieve compliance data";
            log.error(msg, e);
            throw new PolicyComplianceException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new PolicyComplianceException(msg, e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
        return paginationResult;
    }

    @Override
    public List<ComplianceFeature> getNoneComplianceFeatures(int complianceStatusId) throws PolicyComplianceException {
        List<ComplianceFeature> complianceFeatureList;
        try {
            PolicyManagementDAOFactory.openConnection();
            complianceFeatureList = monitoringDAO.getNoneComplianceFeatures(complianceStatusId);
        } catch (MonitoringDAOException e) {
            String msg = "Unable to retrieve non compliance features";
            log.error(msg, e);
            throw new PolicyComplianceException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new PolicyComplianceException(msg, e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
        return complianceFeatureList;
    }

    private void addMonitoringOperationsToDatabase(List<Device> devices)
            throws PolicyComplianceException, OperationManagementException, InvalidDeviceException {

        List<DeviceIdentifier> deviceIdentifiers = this.getDeviceIdentifiersFromDevices(devices);
        CommandOperation monitoringOperation = new CommandOperation();
        monitoringOperation.setEnabled(true);
        monitoringOperation.setType(Operation.Type.COMMAND);
        monitoringOperation.setCode(OPERATION_MONITOR);
        //	    CommandOperation infoOperation = new CommandOperation();
        //	    infoOperation.setEnabled(true);
        //	    infoOperation.setType(Operation.Type.COMMAND);\\
        //	    infoOperation.setCode(OPERATION_INFO);
        //	    CommandOperation appListOperation = new CommandOperation();
        //	    appListOperation.setEnabled(true);
        //	    appListOperation.setType(Operation.Type.COMMAND);
        //	    appListOperation.setCode(OPERATION_APP_LIST);

        //TODO: Fix this properly later adding device type to be passed in when the task manage executes "addOperations()"
        String type = null;
        if (deviceIdentifiers.size() > 0) {
            type = deviceIdentifiers.get(0).getType();
        }
        DeviceManagementProviderService service = PolicyManagementDataHolder.getInstance().getDeviceManagementService();
        service.addOperation(type, monitoringOperation, deviceIdentifiers);
        //	    service.addOperation(infoOperation, deviceIdentifiers);
        //	    service.addOperation(appListOperation, deviceIdentifiers);
    }

    private List<DeviceIdentifier> getDeviceIdentifiersFromDevices(List<Device> devices) {
        List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
        for (Device device : devices) {
            DeviceIdentifier identifier = new DeviceIdentifier();
            identifier.setId(device.getDeviceIdentifier());
            identifier.setType(device.getType());

            deviceIdentifiers.add(identifier);
        }
        return deviceIdentifiers;
    }

}
