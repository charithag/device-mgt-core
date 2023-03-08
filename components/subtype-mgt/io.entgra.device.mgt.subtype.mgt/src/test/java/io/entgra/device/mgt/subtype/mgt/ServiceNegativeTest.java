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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;

public class ServiceNegativeTest extends BaseDeviceSubTypePluginTest {

    private static final Log log = LogFactory.getLog(ServiceNegativeTest.class);
    private DeviceSubTypeService deviceSubTypeService;

    @BeforeClass
    public void init() {
        deviceSubTypeService = new DeviceSubTypeServiceImpl();
        log.info("Service test initialized");
    }

    @Test(description = "This method tests Add Device Subtype method under negative circumstances with null data",
            expectedExceptions = {NullPointerException.class})
    public void testAddDeviceSubType() throws SubTypeMgtPluginException {
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
        deviceSubTypeService.addDeviceSubType(deviceSubType);
    }

    @Test(description = "This method tests Add Device Subtype method under negative circumstances while missing " +
            "required fields",
            expectedExceptions = {SubTypeMgtPluginException.class},
            expectedExceptionsMessageRegExp = "Error occurred in the database level while adding device subtype for " +
                    "SIM subtype & subtype Id: 1")
    public void testAddDeviceSubTypes() throws SubTypeMgtPluginException {
        String subTypeName = "TestSubType";
        DeviceSubType.DeviceType deviceType = DeviceSubType.DeviceType.SIM;

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
        deviceSubTypeService.addDeviceSubType(deviceSubType);
    }

    @Test(description = "This method tests Update Device Subtype method under negative circumstances with invalid " +
            "subtype Id",
            expectedExceptions = {SubTypeMgtPluginException.class},
            expectedExceptionsMessageRegExp = "Cannot find device subtype for SIM subtype & subtype Id: 15")
    public void testUpdateDeviceSubTypes() throws SubTypeMgtPluginException {
        int subTypeId = 15;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        DeviceSubType.DeviceType deviceType = DeviceSubType.DeviceType.SIM;
        String subTypeName = "TestSubType";
        String subTypeExpected = TestUtils.createUpdateDeviceSubType(subTypeId);

        deviceSubTypeService.updateDeviceSubType(subTypeId, tenantId, deviceType, subTypeName, subTypeExpected);
    }


}
