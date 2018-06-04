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
  View, Button, ImageBackground, Image
} from 'react-native';
import _ from 'lodash';

import t from 'tcomb-form-native'; // 0.6.9
t.form.Form.stylesheet.textbox.normal.borderColor = '#f6f200';
t.form.Form.stylesheet.textbox.normal.color = '#f6f200';
t.form.Form.stylesheet.controlLabel.normal.color = '#f6f200';

t.form.Form.stylesheet.textbox.normal.borderWidth = 0;
t.form.Form.stylesheet.textbox.error.borderWidth = 0;
t.form.Form.stylesheet.textbox.normal.marginBottom = 0;
t.form.Form.stylesheet.textbox.error.marginBottom = 0;

t.form.Form.stylesheet.textboxView.normal.borderWidth = 0;
t.form.Form.stylesheet.textboxView.error.borderWidth = 0;
t.form.Form.stylesheet.textboxView.normal.borderRadius = 0;
t.form.Form.stylesheet.textboxView.error.borderRadius = 0;
t.form.Form.stylesheet.textboxView.normal.borderBottomWidth = 1;
t.form.Form.stylesheet.textboxView.error.borderBottomWidth = 1;
t.form.Form.stylesheet.textboxView.normal.borderColor = '#f6f200';
t.form.Form.stylesheet.textbox.normal.borderBottomColor = '#f6f200';

const Form = t.form.Form;



const User = t.struct({
  username: t.String,
  password: t.String
});

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
    const resizeMode = "stretch";
    return (
      <ImageBackground source={require('./yellowjerseybackground.png')} imageStyle={{
        resizeMode,
      }}
        style={{
          width: 420,
          height: 720,
          alignItems: 'center',
          justifyContent: 'center',
          padding: 30,
        }}>
      <View style={{
        flex: 1,
        flexDirection: 'column',
        justifyContent: 'center',
        alignItems: 'center',
      }}>
        <View style={{ width: 300 }}>
          <Form type={User} />
        </View>    
        <View style={{height:50}}>   
        <View style={styles.login}>
              <Image
                style={{ width: 52, height: 50,marginRight:10 }}
                source={require('./FB.png')}
              /><Image
                style={{ width: 50, height: 50 }}
                source={require('./GOOGLE.png')}
              />
            </View>        
            </View>
      </View>

</ImageBackground>

    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center'
  },
  login: {
    flex: 1,
    flexDirection: 'row',
    height:70
  }
});

export default Home;