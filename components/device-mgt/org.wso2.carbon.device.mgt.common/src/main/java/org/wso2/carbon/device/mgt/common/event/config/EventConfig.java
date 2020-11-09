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

package org.wso2.carbon.device.mgt.common.event.config;

public class EventConfig {
    private int eventId;
    private String eventSource;
    private String eventLogic;
    private String actions;

    public String getEventLogic() {
        return eventLogic;
    }

    public void setEventLogic(String eventLogic) {
        this.eventLogic = eventLogic;
    }

    public String getActions() {
        return actions;
    }

    public void setActions(String actions) {
        this.actions = actions;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getEventSource() {
        return eventSource;
    }

    public void setEventSource(String eventSource) {
        this.eventSource = eventSource;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EventConfig) {
            EventConfig eventConfig = (EventConfig) obj;
            return this.eventSource.equalsIgnoreCase(eventConfig.getEventSource()) &&
                    this.eventLogic.equalsIgnoreCase(eventConfig.getEventLogic());
        }
        return false;
    }
}
