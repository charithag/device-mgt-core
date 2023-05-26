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

function onRequest(context) {
    var userModule = require("/app/modules/business-controllers/user.js")["userModule"];
    var deviceMgtProps = require("/app/modules/conf-reader/main.js")["conf"];
    var displayData = {};


    if (userModule.isAuthorized("/permission/admin/device-mgt/admin/device-type")) {
        displayData.canManage = true;
    }
	context.handlebars.registerHelper('selected', function(a, b, opts) {
		if(a == b) // Or === depending on your needs
			return "selected";
		else
			return "";
	});

	var deviceType = request.getParameter("type");
	var serviceInvokers = require("/app/modules/oauth/token-protected-service-invokers.js")["invokers"];
	var restAPIEndpoint = deviceMgtProps["httpsURL"] + devicemgtProps["backendRestEndpoints"]["deviceMgt"]
		+ "/events/" + deviceType;
	displayData.name = deviceType;
	serviceInvokers.XMLHttp.get(
		restAPIEndpoint,
		function (restAPIResponse) {
			if (restAPIResponse["status"] == 200 && restAPIResponse["responseText"]) {
				var typeData = parse(restAPIResponse["responseText"]);
				displayData.event = typeData;
			}
		}
	);

	return displayData;
}