/*
 * Copyright (C) 2018 - 2023 Entgra (Pvt) Ltd, Inc - All Rights Reserved.
 *
 * Unauthorised copying/redistribution of this file, via any medium is strictly prohibited.
 *
 * Licensed under the Entgra Commercial License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://entgra.io/licenses/entgra-commercial/1.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.device.mgt.subtype.mgt;

import io.entgra.device.mgt.subtype.mgt.dto.DeviceSubType;
import io.entgra.device.mgt.subtype.mgt.exception.SubTypeMgtPluginException;
import io.entgra.device.mgt.subtype.mgt.impl.DeviceSubTypeServiceImpl;
import io.entgra.device.mgt.subtype.mgt.mock.BaseDeviceSubTypePluginTest;
import io.entgra.device.mgt.subtype.mgt.spi.DeviceSubTypeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.util.List;


public class ServiceTest extends BaseDeviceSubTypePluginTest {

    private static final Log log = LogFactory.getLog(ServiceTest.class);
    private DeviceSubTypeService deviceSubTypeService;

    @BeforeClass
    public void init() {
        deviceSubTypeService = new DeviceSubTypeServiceImpl();
        log.info("Service test initialized");
    }

    @Test(dependsOnMethods = "testAddDeviceSubType")
    public void testGetDeviceType() throws SubTypeMgtPluginException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        DeviceSubType subTypeActual = deviceSubTypeService.getDeviceSubType(1, tenantId,
                DeviceSubType.DeviceType.METER);
        TestUtils.verifyDeviceSubType(subTypeActual);
    }

    @Test(dependsOnMethods = "testAddDeviceSubType")
    public void testGetAllDeviceTypes() throws SubTypeMgtPluginException {
        DeviceSubType.DeviceType deviceType = DeviceSubType.DeviceType.METER;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<DeviceSubType> subTypesActual = deviceSubTypeService.getAllDeviceSubTypes(tenantId, deviceType);
        log.info(deviceType + " sub types count should be " + subTypesActual.size());
        Assert.assertNotNull(subTypesActual, "Should not be null");
    }

    @Test
    public void testAddDeviceSubType() throws SubTypeMgtPluginException {
        int subTypeId = 1;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String subTypeName = "TestSubType";
        DeviceSubType.DeviceType deviceType = DeviceSubType.DeviceType.METER;
        String typeDefinition = TestUtils.createNewDeviceSubType(subTypeId);

        DeviceSubType deviceSubType = new DeviceSubType() {
            @Override
            public <T> DeviceSubType setDeviceSubType(T objType, String typeDef) {
                return null;
            }

            @Override
            public String parseSubTypeToJson(Object objType) {
                return null;
            }
        };
        deviceSubType.setSubTypeName(subTypeName);
        deviceSubType.setDeviceType(deviceType);
        deviceSubType.setTenantId(tenantId);
        deviceSubType.setTypeDefinition(typeDefinition);
        deviceSubTypeService.addDeviceSubType(deviceSubType);

        DeviceSubType subTypeActual = deviceSubTypeService.getDeviceSubType(subTypeId, tenantId, deviceType);
        Assert.assertNotNull(subTypeActual, "Cannot be null");
        TestUtils.verifyDeviceSubType(subTypeActual);
    }

    @Test(dependsOnMethods = "testAddDeviceSubType")
    public void testUpdateDeviceSubType() throws SubTypeMgtPluginException {
        int subTypeId = 1;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        DeviceSubType.DeviceType deviceType = DeviceSubType.DeviceType.METER;
        String subTypeName = "TestSubType";
        String subTypeExpected = TestUtils.createUpdateDeviceSubType(subTypeId);

        deviceSubTypeService.updateDeviceSubType(subTypeId, tenantId, deviceType, subTypeName, subTypeExpected);

        DeviceSubType subTypeActual = deviceSubTypeService.getDeviceSubType(subTypeId, tenantId, deviceType);

        Assert.assertNotNull(subTypeActual, "Cannot be null");
        TestUtils.verifyUpdatedDeviceSubType(subTypeActual);
    }

    @Test(dependsOnMethods = "testAddDeviceSubType")
    public void testGetDeviceTypeCount() throws SubTypeMgtPluginException {
        DeviceSubType.DeviceType deviceType = DeviceSubType.DeviceType.METER;
        int subTypeCount = deviceSubTypeService.getDeviceSubTypeCount(deviceType);
        log.info(deviceType + " Device subtypes count: " + subTypeCount);
    }
}
