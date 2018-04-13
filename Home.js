/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 * @flow
 */

import React, { Component } from 'react';
import {
  Platform,
  StyleSheet,
  Text,
  View, Button
} from 'react-native';


const FBSDK = require('react-native-fbsdk');
const {
  LoginButton, AccessToken
} = FBSDK;

type Props = {};
class Home extends Component<Props> {
  constructor(props) {
    super(props);
    this.state = {
      name: ""
    }
  }
  initUser = (token) => {
    let _this = this;
    fetch('https://graph.facebook.com/v2.5/me?fields=email,name,friends&access_token=' + token)
      .then((response) => response.json())
      .then((json) => {
        // Some user object has been set up somewhere, build that user here
        let user = {}
        user.name = json.name
        user.id = json.id
        user.user_friends = json.friends
        user.email = json.email
        user.username = json.name
        user.loading = false
        user.loggedIn = true
        _this.setState({ name: json.name })
      })
      .catch((error) => {
        console.log("error", error)
        alert('ERROR GETTING DATA FROM FACEBOOK')
      })
  }
  render() {
    const { navigate } = this.props.navigation;

    return (
      <View style={styles.container}>

        {this.state.name ? <Text style={styles.welcome}>{this.state.name}</Text>
          : <Text style={styles.welcome}> Please Login </Text>}
        <LoginButton
          publishPermissions={["publish_actions"]}
          onLoginFinished={
            (error, result) => {
              if (error) {
                alert("Login failed with error: " + result.error);
              } else if (result.isCancelled) {
                alert("Login was cancelled");
              } else {
                AccessToken.getCurrentAccessToken().then((data) => {
                  const { accessToken } = data
                  this.initUser(accessToken)
                })


                alert("Login was successful with permissions: " + result.grantedPermissions)
              }
            }
          }
          onLogoutFinished={() => alert("User logged out")} />

        <Text style={styles.instructions}>
        </Text>
        <Button
          title="Create Event"
          onPress={() =>
            navigate('CreateEvent')
          }
        />
        
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
  instructions: {
    textAlign: 'center',
    color: '#333333',
    marginBottom: 5,
  },
});

export default Home;