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


package io.entgra.device.mgt.core.device.mgt.common.device.details;

import io.entgra.device.mgt.core.device.mgt.common.DeviceIdentifier;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Date;

@ApiModel(value = "DeviceLocation", description = "This class carries all information related to the device location " +
                                                  "details provided by a device.")
public class DeviceLocation implements Serializable {

    private static final long serialVersionUID = 1998101722L;

    @ApiModelProperty(name = "deviceId", value = "Device id", required = true)
    private int deviceId;
    @ApiModelProperty(name = "deviceIdentifier", value = "Device identifier used to identify a device uniquely.",
                      required = true)
    private DeviceIdentifier deviceIdentifier;
    @ApiModelProperty(name = "latitude", value = "Device GPS latitude.", required = true)
    private Double latitude;
    @ApiModelProperty(name = "longitude", value = "Device GPS longitude.", required = true)
    private Double longitude;
    @ApiModelProperty(name = "street1", value = "First line of the address.", required = true)
    private String street1;
    @ApiModelProperty(name = "street2", value = "Second part of the address.", required = true)
    private String street2;
    @ApiModelProperty(name = "city", value = "City of the device location.", required = true)
    private String city;
    @ApiModelProperty(name = "state", value = "State of the device address.", required = true)
    private String state;
    @ApiModelProperty(name = "zip", value = "Zip code of the device address.", required = true)
    private String zip;
    @ApiModelProperty(name = "country", value = "Country of the device address.", required = true)
    private String country;
    @ApiModelProperty(name = "updatedTime", value = "Update time of the device.", required = true)
    private Date updatedTime;
    @ApiModelProperty(name = "altitude", value = "Device altitude.", required = true)
    private double altitude;
    @ApiModelProperty(name = "speed", value = "Device speed.", required = true)
    private float speed;
    @ApiModelProperty(name = "bearing", value = "Device bearing.", required = true)
    private float bearing;
    @ApiModelProperty(name = "distance", value = "Device distance.", required = true)
    private double distance;

    public double getAltitude() { return altitude; }

    public void setAltitude(double altitude) { this.altitude = altitude; }

    public float getSpeed() { return speed; }

    public void setSpeed(float speed) { this.speed = speed; }

    public float getBearing() { return bearing; }

    public void setBearing(float bearing) { this.bearing = bearing; }

    public double getDistance() { return distance; }

    public void setDistance(double distance) { this.distance = distance; }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public void setDeviceIdentifier(DeviceIdentifier deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getStreet1() {
        return street1;
    }

    public void setStreet1(String street1) {
        this.street1 = street1;
    }

    public String getStreet2() {
        return street2;
    }

    public void setStreet2(String street2) {
        this.street2 = street2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Date getUpdatedTime() {
        if(updatedTime == null ){
            updatedTime = new Date();
        }
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }
}

