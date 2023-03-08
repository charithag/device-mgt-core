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

import io.entgra.device.mgt.subtype.mgt.dao.util.ConnectionManagerUtil;
import io.entgra.device.mgt.subtype.mgt.dto.DeviceSubType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class TestUtils {
    private static final Log log = LogFactory.getLog(TestUtils.class);

    public static String createNewDeviceSubType(int subtypeId) {
        return "{\"make\": \"TestSubType\", \"model\": \"ATx-Mega SIM800\", " +
                "\"subTypeId\": " + subtypeId + ", \"hasSMSSupport\": true, \"hasICMPSupport\": true, " +
                "\"socketServerPort\": 8071}";
    }

    public static String createUpdateDeviceSubType(int subtypeId) {
        return "{\"make\": \"TestSubType\", \"model\": \"ATx-Mega SIM900\", " +
                "\"subTypeId\": " + subtypeId + ", \"hasSMSSupport\": false, \"hasICMPSupport\": true, " +
                "\"socketServerPort\": 8071}";
    }

    public static void verifyDeviceSubType(DeviceSubType deviceSubType) {
        String typeDefExpected = TestUtils.createNewDeviceSubType(1);
        Assert.assertEquals(deviceSubType.getSubTypeId(), 1);
        Assert.assertEquals(deviceSubType.getDeviceType(), DeviceSubType.DeviceType.valueOf("METER"));
        Assert.assertEquals(deviceSubType.getSubTypeName(), "TestSubType");
        Assert.assertEquals(deviceSubType.getTypeDefinition(), typeDefExpected);
    }

    public static void verifyDeviceSubTypeDAO(DeviceSubType deviceSubType) {
        String typeDefExpected = TestUtils.createNewDeviceSubType(1);
        Assert.assertEquals(deviceSubType.getSubTypeId(), 1);
        Assert.assertEquals(deviceSubType.getDeviceType(), DeviceSubType.DeviceType.valueOf("COM"));
        Assert.assertEquals(deviceSubType.getSubTypeName(), "TestSubType");
        Assert.assertEquals(deviceSubType.getTypeDefinition(), typeDefExpected);
    }

    public static void verifyUpdatedDeviceSubType(DeviceSubType deviceSubType) {
        String typeDefExpected = TestUtils.createUpdateDeviceSubType(1);
        Assert.assertEquals(deviceSubType.getSubTypeId(), 1);
        Assert.assertEquals(deviceSubType.getDeviceType(), DeviceSubType.DeviceType.valueOf("METER"));
        Assert.assertEquals(deviceSubType.getSubTypeName(), "TestSubType");
        Assert.assertEquals(deviceSubType.getTypeDefinition(), typeDefExpected);
    }

    public static void verifyUpdatedDeviceSubTypeDAO(DeviceSubType deviceSubType) {
        String typeDefExpected = TestUtils.createUpdateDeviceSubType(1);
        Assert.assertEquals(deviceSubType.getSubTypeId(), 1);
        Assert.assertEquals(deviceSubType.getDeviceType(), DeviceSubType.DeviceType.valueOf("COM"));
        Assert.assertEquals(deviceSubType.getSubTypeName(), "TestSubType");
        Assert.assertEquals(deviceSubType.getTypeDefinition(), typeDefExpected);
    }

    public static void cleanupResources(Connection conn, Statement stmt, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.warn("Error occurred while closing result set", e);
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                log.warn("Error occurred while closing prepared statement", e);
            }
        }
        if (conn != null) {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

}
