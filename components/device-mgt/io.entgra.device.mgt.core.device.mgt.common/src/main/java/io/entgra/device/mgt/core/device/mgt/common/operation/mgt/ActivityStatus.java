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


package io.entgra.device.mgt.core.device.mgt.common.operation.mgt;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.entgra.device.mgt.core.device.mgt.common.DeviceIdentifier;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

@ApiModel(value = "ActivityStatus", description = "Status of an activity is described in this class.")
public class ActivityStatus {

    public enum Status {
        IN_PROGRESS, PENDING, COMPLETED, ERROR, REPEATED, INVALID, UNAUTHORIZED, NOTNOW, REQUIRED_CONFIRMATION, CONFIRMED
    }

    @ApiModelProperty(
            name = "deviceIdentifier",
            value = "Device identifier of the device.",
            required = true)
    @JsonProperty("deviceIdentifier")
    private DeviceIdentifier deviceIdentifier;

    @ApiModelProperty(
            name = "status",
            value = "Status of the activity performed.",
            required = true,
            example = "PENDING")
    @JsonProperty("status")
    private Status status;

    @ApiModelProperty(
            name = "responses",
            value = "Responses received from devices.",
            required = true)
    @JsonProperty("responses")
    private List<OperationResponse> responses;

    @ApiModelProperty(
            name = "updatedTimestamp    ",
            value = "Last updated time of the activity.",
            required = true,
            example = "Thu Oct 06 11:18:47 IST 2016")
    @JsonProperty("updatedTimestamp")
    private String updatedTimestamp;

    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public void setDeviceIdentifier(DeviceIdentifier deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<OperationResponse> getResponses() {
        return responses;
    }

    public void setResponses(List<OperationResponse> responses) {
        this.responses = responses;
    }

    public String getUpdatedTimestamp() {
        return updatedTimestamp;
    }

    public void setUpdatedTimestamp(String updatedTimestamp) {
        this.updatedTimestamp = updatedTimestamp;
    }
}

