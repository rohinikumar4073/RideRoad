import React, { Component } from 'react';

import {
    Platform,
    StyleSheet,
    Text,
    View
} from 'react-native';
class CreateEvent extends Component<Props> {
    render() {
        const { navigate } = this.props.navigation;
        return (<View>
            <Text >

                Create Event
            </Text>
        </View>
        )
    }
}
export default CreateEvent;