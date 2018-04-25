import React, { Component } from 'react';

import {
  StackNavigator,
} from 'react-navigation';

import Home from './Home'
import CreateEvent from "./CreateEvent"
const App = StackNavigator({
  Home: { screen: Home }, CreateEvent: { screen: CreateEvent }
});

export default App;