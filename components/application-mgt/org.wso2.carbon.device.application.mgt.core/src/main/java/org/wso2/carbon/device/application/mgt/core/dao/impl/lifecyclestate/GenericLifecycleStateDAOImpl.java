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
package org.wso2.carbon.device.application.mgt.core.dao.impl.lifecyclestate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.common.AppLifecycleState;
import org.wso2.carbon.device.application.mgt.common.LifecycleState;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.core.dao.LifecycleStateDAO;
import org.wso2.carbon.device.application.mgt.core.dao.common.Util;
import org.wso2.carbon.device.application.mgt.core.dao.impl.AbstractDAOImpl;
import org.wso2.carbon.device.application.mgt.core.exception.LifeCycleManagementDAOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Concrete implementation for Lifecycle related DB operations.
 */
public class GenericLifecycleStateDAOImpl extends AbstractDAOImpl implements LifecycleStateDAO {

    private static final Log log = LogFactory.getLog(GenericLifecycleStateDAOImpl.class);
    @Override
    public LifecycleState getLatestLifeCycleStateByReleaseID(int applicationReleaseId) throws LifeCycleManagementDAOException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getDBConnection();
            String sql = "SELECT ID, CURRENT_STATE, PREVIOUS_STATE, TENANT_ID, UPDATED_AT, UPDATED_BY FROM "
                    + "AP_APP_LIFECYCLE_STATE WHERE AP_APP_RELEASE_ID=? ORDER BY UPDATED_AT DESC;";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, applicationReleaseId);
            rs = stmt.executeQuery();
            return constructLifecycle(rs);
        } catch (SQLException e) {
            throw new LifeCycleManagementDAOException("Error occurred while getting application List", e);
        }  catch (DBConnectionException e) {
            throw new LifeCycleManagementDAOException("Error occurred while obtaining the DB connection to get latest"
                    + " lifecycle state for a specific application", e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
    }

    public LifecycleState getLatestLifeCycleState(int appId, String uuid) throws LifeCycleManagementDAOException{
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getDBConnection();
            String sql = "SELECT ID, CURRENT_STATE, PREVIOUS_STATE, TENANT_ID, UPDATED_AT, UPDATED_BY FROM "
                    + "AP_APP_LIFECYCLE_STATE WHERE AP_APP_ID=? AND AP_APP_RELEASE_ID=(SELECT ID FROM AP_APP_RELEASE "
                    + "WHERE UUID=?) ORDER BY UPDATED_AT DESC;";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, appId);
            stmt.setString(2, uuid);
            rs = stmt.executeQuery();
            return constructLifecycle(rs);
        } catch (SQLException e) {
            throw new LifeCycleManagementDAOException("Error occurred while getting application List", e);
        }  catch (DBConnectionException e) {
            throw new LifeCycleManagementDAOException("Error occurred while obtaining the DB connection to get latest"
                    + " lifecycle state for a specific application", e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
    }


    public String getAppReleaseCreatedUsername(int appId, String uuid, int tenantId) throws LifeCycleManagementDAOException{
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getDBConnection();
            String sql = "SELECT UPDATED_BY FROM AP_APP_LIFECYCLE_STATE WHERE AP_APP_ID=? AND "
                    + "AP_APP_RELEASE_ID=(SELECT ID FROM AP_APP_RELEASE WHERE UUID=?) AND CURRENT_STATE = ? AND TENANT_ID = ?;";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, appId);
            stmt.setString(2, uuid);
            stmt.setString(3, AppLifecycleState.CREATED.toString());
            stmt.setInt(4, tenantId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("UPDATED_BY");

            }
            return null;
        } catch (SQLException e) {
            throw new LifeCycleManagementDAOException("Error occurred while getting application List", e);
        }  catch (DBConnectionException e) {
            throw new LifeCycleManagementDAOException("Error occurred while obtaining the DB connection to get latest"
                    + " lifecycle state for a specific application", e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
    }

    @Override
    public List<LifecycleState> getLifecycleStates(int appReleaseId, int tenantId) throws LifeCycleManagementDAOException {
        List<LifecycleState> lifecycleStates = new ArrayList<>();
        Connection conn ;
        try {
            conn = this.getDBConnection();
            String sql = "SELECT "
                    + "CURRENT_STATE, "
                    + "PREVIOUS_STATE, "
                    + "UPDATED_AT, "
                    + "UPDATED_BY "
                    + "FROM AP_APP_LIFECYCLE_STATE "
                    + "WHERE AP_APP_RELEASE_ID = ?  AND "
                    + "TENANT_ID = ? "
                    + "ORDER BY UPDATED_AT ASC;";
            try (PreparedStatement stmt = conn.prepareStatement(sql)){
                stmt.setInt(1,appReleaseId);
                stmt.setInt(2, tenantId);
                try (ResultSet rs = stmt.executeQuery()){
                    while (rs.next()) {
                        LifecycleState lifecycleState = new LifecycleState();
                        lifecycleState.setCurrentState(rs.getString("CURRENT_STATE"));
                        lifecycleState.setPreviousState(rs.getString("PREVIOUS_STATE"));
                        lifecycleState.setUpdatedAt(rs.getTimestamp("UPDATED_AT"));
                        lifecycleState.setUpdatedBy(rs.getString("UPDATED_BY"));
                        lifecycleStates.add(lifecycleState);
                    }
                }
            }
        } catch (DBConnectionException e) {
            throw new LifeCycleManagementDAOException("Error occurred while obtaining the DB connection when getting "
                    + "lifecycle states for an application", e);
        } catch (SQLException e) {
            throw new LifeCycleManagementDAOException("Error occurred while retrieving lifecycle states.", e);
        }
        return lifecycleStates;
    }

    @Override
    public void addLifecycleState(LifecycleState state, int appReleaseId, int tenantId) throws LifeCycleManagementDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = this.getDBConnection();
            String sql = "INSERT INTO AP_APP_LIFECYCLE_STATE "
                    + "(CURRENT_STATE, "
                    + "PREVIOUS_STATE, "
                    + "TENANT_ID, "
                    + "UPDATED_BY, "
                    + "UPDATED_AT, "
                    + "AP_APP_RELEASE_ID, "
                    + "AP_APP_ID) "
                    + "VALUES (?, ?, ?, ?, ?, ?, (SELECT AP_APP_ID FROM AP_APP_RELEASE WHERE ID = ?));";

            Calendar calendar = Calendar.getInstance();
            Timestamp timestamp = new Timestamp(calendar.getTime().getTime());

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, state.getCurrentState().toUpperCase());
            stmt.setString(2, state.getPreviousState().toUpperCase());
            stmt.setInt(3, tenantId);
            stmt.setString(4, state.getUpdatedBy());
            stmt.setTimestamp(5, timestamp);
            stmt.setInt(6, appReleaseId);
            stmt.setInt(7, appReleaseId);
            stmt.executeUpdate();
        } catch (DBConnectionException e) {
            log.error("Error occurred while obtaining the DB connection.", e);
            throw new LifeCycleManagementDAOException("Error occurred while obtaining the DB connection.", e);
        } catch (SQLException e) {
            log.error("Error occurred while adding lifecycle: " + state.getCurrentState(), e);
            throw new LifeCycleManagementDAOException("Error occurred while adding lifecycle: " + state.getCurrentState(), e);
        } finally {
            Util.cleanupResources(stmt, null);
        }
    }

    @Override
    public void deleteLifecycleStateByReleaseId(int releaseId) throws LifeCycleManagementDAOException {
        Connection conn;
        try {
            conn = this.getDBConnection();
            String sql = "DELETE FROM " +
                    "AP_APP_LIFECYCLE_STATE " +
                    "WHERE AP_APP_RELEASE_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, releaseId);
                stmt.executeUpdate();
            }
        } catch (DBConnectionException e) {
            throw new LifeCycleManagementDAOException("Error occurred while obtaining the DB connection.", e);
        } catch (SQLException e) {
            throw new LifeCycleManagementDAOException("Error occurred while deleting lifecycle for application "
                    + "release ID: " + releaseId, e);
        }
    }

    @Override
    public void deleteLifecycleStates(List<Integer> appReleaseIds) throws LifeCycleManagementDAOException{
        Connection conn;
        try {
            conn = this.getDBConnection();
            String sql = "DELETE FROM " +
                    "AP_APP_LIFECYCLE_STATE " +
                    "WHERE AP_APP_RELEASE_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (Integer releaseId : appReleaseIds) {
                    stmt.setInt(1, releaseId);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        } catch (DBConnectionException e) {
            throw new LifeCycleManagementDAOException("Error occurred while obtaining the DB connection for deleting "
                    + "application life-cycle states for given application Ids.", e);
        } catch (SQLException e) {
            throw new LifeCycleManagementDAOException("Error occurred while deleting life-cycle states for given "
                    + "application releases.", e);
        }
    }


    private LifecycleState constructLifecycle(ResultSet rs) throws LifeCycleManagementDAOException {
        LifecycleState lifecycleState = null;
        try {
            if (rs !=null && rs.next()) {
                lifecycleState = new LifecycleState();
                lifecycleState.setCurrentState(rs.getString("CURRENT_STATE"));
                lifecycleState.setPreviousState(rs.getString("PREVIOUS_STATE"));
                lifecycleState.setUpdatedAt(rs.getTimestamp("UPDATED_AT"));
                lifecycleState.setUpdatedBy(rs.getString("UPDATED_BY"));
            }
        } catch (SQLException e) {
            throw new LifeCycleManagementDAOException(
                    "Error occurred while construct lifecycle state by retrieving data from SQL query", e);
        }
        return lifecycleState;
    }
}
