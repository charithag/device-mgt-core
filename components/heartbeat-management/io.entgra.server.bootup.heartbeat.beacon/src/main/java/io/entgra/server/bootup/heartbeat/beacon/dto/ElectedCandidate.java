/*
 * Copyright (c) 2020, Entgra Pvt Ltd. (http://www.wso2.org) All Rights Reserved.
 *
 * Entgra Pvt Ltd. licenses this file to you under the Apache License,
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

package io.entgra.server.bootup.heartbeat.beacon.dto;

import java.sql.Timestamp;
import java.util.List;

public class ElectedCandidate {

    private String serverUUID;
    private Timestamp timeOfElection;
    private List<String> acknowledgedTaskList = null;

    public List<String> getAcknowledgedTaskList() {
        return acknowledgedTaskList;
    }

    public void setAcknowledgedTaskList(List<String> acknowledgedTaskList) {
        this.acknowledgedTaskList = acknowledgedTaskList;
    }

    public String getServerUUID() {
        return serverUUID;
    }

    public void setServerUUID(String serverUUID) {
        this.serverUUID = serverUUID;
    }

    public Timestamp getTimeOfElection() {
        return timeOfElection;
    }

    public void setTimeOfElection(Timestamp timeOfElection) {
        this.timeOfElection = timeOfElection;
    }

}