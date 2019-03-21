import React from 'react';
import {Route} from 'react-router-dom';
class RouteWithSubRoutes extends React.Component{
    props;
    constructor(props){
        super(props);
        console.log(props);
        this.props = props;
    }
    render() {
        return(
            <Route path={this.props.path} render={(props) => (
                <this.props.component {...props} routes={this.props.routes}/>
            )}/>
        );
    }

}

export default RouteWithSubRoutes;