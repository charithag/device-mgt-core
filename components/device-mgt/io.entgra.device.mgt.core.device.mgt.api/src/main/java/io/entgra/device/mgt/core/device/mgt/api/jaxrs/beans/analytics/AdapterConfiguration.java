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
package io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.analytics;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * Adapter main configurations
 * Attributes : Adapter properties list, custom-mapping flag, mapping configurations
 */
public class AdapterConfiguration {
    @ApiModelProperty(value = "Adapter properties list")
    private List<AdapterProperty> adapterProperties;
    @ApiModelProperty(value = "Custom-mapping flag")
    private boolean isCustomMappingEnabled;
    @ApiModelProperty(value = "Mapping configurations")
    private AdapterMappingConfiguration adapterMappingConfiguration;

    public List<AdapterProperty> getAdapterProperties() {
        return adapterProperties;
    }

    public void setAdapterProperties(
            List<AdapterProperty> adapterProperties) {
        this.adapterProperties = adapterProperties;
    }

    public boolean isCustomMappingEnabled() {
        return isCustomMappingEnabled;
    }

    public void setCustomMappingEnabled(boolean customMappingEnabled) {
        isCustomMappingEnabled = customMappingEnabled;
    }

    public AdapterMappingConfiguration getAdapterMappingConfiguration() {
        return adapterMappingConfiguration;
    }

    public void setAdapterMappingConfiguration(
            AdapterMappingConfiguration adapterMappingConfiguration) {
        this.adapterMappingConfiguration = adapterMappingConfiguration;
    }
}
