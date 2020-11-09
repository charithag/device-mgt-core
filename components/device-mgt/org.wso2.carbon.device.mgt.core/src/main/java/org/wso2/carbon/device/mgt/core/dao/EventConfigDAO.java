/*
 * Copyright (c) 2020, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.core.dao;

import org.wso2.carbon.device.mgt.common.event.config.EventConfig;

import java.util.List;

public interface EventConfigDAO {
    /**
     * Create event configuration entries of the db for a selected tenant
     * @param eventConfigList event list to be created
     * @param tenantId corresponding tenant id of the events
     * @return generated event ids while storing geofence data
     * @throws EventManagementDAOException error occurred while creating event records
     */
    List<Integer> storeEventRecords(List<EventConfig> eventConfigList, int tenantId) throws EventManagementDAOException;

    /**
     * Cerate even-group mapping records
     * @param eventIds event ids to be mapped with groups
     * @param groupIds group ids of the event attached with
     * @return true for the successful creation
     * @throws EventManagementDAOException error occurred while creating event-group mapping records
     */
    boolean addEventGroupMappingRecords(List<Integer> eventIds, List<Integer> groupIds) throws EventManagementDAOException;

    /**
     * Get events owned by a specific device group
     * @param groupIds group ids of the events
     * @param tenantId tenant of the events owning
     * @return list of event configuration filtered by tenant id and group ids
     * @throws EventManagementDAOException error occurred while reading event records
     */
    List<EventConfig> getEventsOfGroups(List<Integer> groupIds, int tenantId) throws EventManagementDAOException;

    /**
     * Delete event group mapping records using the group ids
     * @param groupIdsToDelete id of groups
     * @throws EventManagementDAOException error occurred while deleting event-group mapping records
     */
    void deleteEventGroupMappingRecordsByGroupIds(List<Integer> groupIdsToDelete) throws EventManagementDAOException;

    /**
     * Update event records of the tenant
     * @param eventsToUpdate updating event records
     * @param tenantId event owning tenant id
     * @throws EventManagementDAOException error occurred while updating events
     */
    void updateEventRecords(List<EventConfig> eventsToUpdate, int tenantId) throws EventManagementDAOException;

    /**
     * Delete events using event ids
     * @param eventsIdsToDelete ids of the events which should be deleted
     * @param tenantId event owning tenant id
     * @throws EventManagementDAOException error occurred while deleting event records
     */
    void deleteEventRecords(List<Integer> eventsIdsToDelete, int tenantId) throws EventManagementDAOException;

    /**
     * Get event records by event ids
     * @param eventIds filtering event ids
     * @param tenantId tenant id of the events
     * @return filtered event configuration list
     * @throws EventManagementDAOException error occurred while reading events
     */
    List<EventConfig> getEventsById(List<Integer> eventIds, int tenantId) throws EventManagementDAOException;

    List<Integer> getGroupsOfEvents(List<Integer> updateEventIdList) throws EventManagementDAOException;

    void deleteEventGroupMappingRecordsByEventIds(List<Integer> removedEventIdList) throws EventManagementDAOException;
}
