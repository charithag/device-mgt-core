/*
 * Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 * Entgra (pvt) Ltd. licenses this file to you under the Apache License,
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

import React from "react";
import {Layout, Menu, Icon, Drawer, Button} from 'antd';
const {Header, Content, Footer} = Layout;
import {Link} from "react-router-dom";
import RouteWithSubRoutes from "../../components/RouteWithSubRoutes";
import {Switch} from 'react-router';
import axios from "axios";
import "./Dashboard.css";
import {withConfigContext} from "../../context/ConfigContext";
import Logout from "./logout/Logout";

const {SubMenu} = Menu;

class Dashboard extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            routes: props.routes,
            selectedKeys: [],
            deviceTypes: []
        };
        this.logo = this.props.context.theme.logo;
    }

    componentDidMount() {
        this.getDeviceTypes();
    }

    getDeviceTypes = () => {
        const config = this.props.context;
        axios.get(
            window.location.origin + config.serverConfig.invoker.uri + config.serverConfig.invoker.deviceMgt + "/device-types"
        ).then(res => {
            if (res.status === 200) {
                const deviceTypes = JSON.parse(res.data.data);
                this.setState({
                    deviceTypes,
                    loading: false,
                });
            }

        }).catch((error) => {
            if (error.hasOwnProperty("response") && error.response.status === 401) {
                window.location.href = window.location.origin + '/store/login';
            } else {
                notification["error"]({
                    message: "There was a problem",
                    duration: 0,
                    description:
                        "Error occurred while trying to load device types.",
                });
            }
            this.setState({
                loading: false
            });
        });
    };

    changeSelectedMenuItem = (key) => {
        this.setState({
            selectedKeys: [key]
        })
    };

    //functions for show the drawer
    state = {
        visible: false,
        collapsed: false
    };

    showDrawer = () => {
        this.setState({
            visible: true,
            collapsed: !this.state.collapsed,
        });
    };

    onClose = () => {
        this.setState({
            visible: false,
        });
    };

    render() {
        const config = this.props.context;
        const {selectedKeys, deviceTypes} = this.state;

        const DeviceTypesData = deviceTypes.map((deviceType) => {
            const platform = deviceType.name;
            const defaultPlatformIcons = config.defaultPlatformIcons;
            let icon = defaultPlatformIcons.default.icon;
            let theme = defaultPlatformIcons.default.theme;
            if (defaultPlatformIcons.hasOwnProperty(platform)) {
                icon = defaultPlatformIcons[platform].icon;
                theme = defaultPlatformIcons[platform].theme;
            }
            return (
                <Menu.Item key={platform}>
                    <Link to={"/store/" + platform}>
                        <Icon type={icon} theme={theme}/>
                        {platform}
                    </Link>
                </Menu.Item>
            );
        });

        return (
            <div>
                <Layout>
                    <Header style={{paddingLeft: 0, paddingRight: 0, backgroundColor: "white"}}>
                        <div className="logo-image">
                            <Link to="/store/android"><img alt="logo" src={this.logo}/></Link>
                        </div>

                        <div className="web-layout">
                            <Menu
                                theme="light"
                                mode="horizontal"
                                defaultSelectedKeys={selectedKeys}
                                style={{lineHeight: '64px'}}
                            >

                                {DeviceTypesData}

                                <Menu.Item key="web-clip"><Link to="/store/web-clip"><Icon type="upload"/>Web
                                    Clips</Link></Menu.Item>

                                <SubMenu className="profile"
                                         title={
                                             <span className="submenu-title-wrapper">
                                     <Icon type="user"/>
                                         Profile
                                     </span>
                                         }
                                >
                                    <Logout/>
                                </SubMenu>
                            </Menu>
                        </div>
                    </Header>
                </Layout>

                <Layout className="mobile-layout">

                    <div className="mobile-menu-button">
                        <Button type="link" onClick={this.showDrawer}>
                            <Icon type={this.state.collapsed ? 'menu-fold' : 'menu-unfold'} className="bar-icon"/>
                        </Button>
                    </div>
                    <Drawer
                        title={<Link to="/store/android">
                            <img alt="logo" src={this.logo} style={{marginLeft: 30}} width={"60%"}/>
                        </Link>}
                        placement="left"
                        closable={false}
                        onClose={this.onClose}
                        visible={this.state.visible}
                        getContainer={false}
                        style={{position: 'absolute'}}
                    >
                        <Menu
                            theme="light"
                            mode="inline"
                            defaultSelectedKeys={selectedKeys}
                            style={{lineHeight: '64px', width: 231}}
                        >

                            {DeviceTypesData}

                            <Menu.Item key="web-clip"><Link to="/store/web-clip"><Icon type="upload"/>Web
                                Clips</Link></Menu.Item>

                        </Menu>
                    </Drawer>
                    <Menu
                        mode="horizontal"
                        defaultSelectedKeys={selectedKeys}
                        style={{lineHeight: '63px', position: 'fixed', marginLeft: '80%'}}
                    >
                        <SubMenu
                            title={
                                <span className="submenu-title-wrapper">
                                     <Icon type="user"/>
                                     </span>
                            }
                        >
                            <Logout/>
                        </SubMenu>
                    </Menu>
                </Layout>

                <Layout className="dashboard-body">
                    <Content style={{padding: '0 0'}}>
                        <Switch>
                            {this.state.routes.map((route) => (
                                <RouteWithSubRoutes changeSelectedMenuItem={this.changeSelectedMenuItem}
                                                    key={route.path} {...route} />
                            ))}

                        </Switch>

                    </Content>

                    <Footer style={{textAlign: 'center'}}>
                        ©2019 entgra.io
                    </Footer>
                </Layout>
            </div>
        );
    }
}

export default withConfigContext(Dashboard);
