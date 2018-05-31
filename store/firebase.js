// Initialize Firebase
import * as firebase from 'firebase';

const config = {
    apiKey: "AIzaSyC9AdabTPX84FT93LtrjdQQK-ntiAYY0_8",
    authDomain: "rideroad-ffdf0.firebaseapp.com",
    databaseURL: "https://rideroad-ffdf0.firebaseio.com",
    projectId: "rideroad-ffdf0",
    storageBucket: "rideroad-ffdf0.appspot.com",
    messagingSenderId: "197845269766"
};
const rideRoadApp = firebase.initializeApp(config)
export default rideRoadApp;