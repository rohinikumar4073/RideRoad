import React from "react";
import configureStore from "./store/configureStore";
import { Provider } from "react-redux";
import App  from "./App";
import LaunchScreen  from "./LaunchScreen";


class Root extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
          storeCreated: false,
          storeRehydrated: false,
          store: null
        };
      }
  
    componentDidMount() {
    }
    render(){
   
        return (  
            <Provider store={configureStore()}>
            <App />
          </Provider>
    );
    }
  
}
export default Root;