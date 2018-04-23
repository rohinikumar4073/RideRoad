import React, { Component } from 'react';

import {
    Platform,
    StyleSheet,
    Text,
    View,
    TextInput,
    Button,
    DatePickerAndroid
} from 'react-native';
class CreateEvent extends Component<Props> {

    constructor(props) {
        super(props);
        this.state = {
            date: ""
        };
    }
    convertToDateString(year,month,day){
        return day.toString()+"-"+month.toString()+"-"+year.toString();
    }
    async openAndroidDatePicker  () {
        try {
            const { action, year, month, day } = await DatePickerAndroid.open({
                date: new Date()
            });
            if (action !== DatePickerAndroid.dismissedAction) {
                console.log("year",year)
                this.setState({date:this.convertToDateString(year,month,day)});
            }
        } catch ({ code, message }) {
            console.warn('Cannot open date picker', message);
        }
    }
    render() {
        const { navigate } = this.props.navigation;
        return (<View style={{
            flex: 1,
            flexDirection: 'column',
            justifyContent: 'center',
            alignItems: 'center',
        }}>
            <Text>
                Create Event
                    </Text>

            <Text>
                From
                </Text>
            <TextInput
                style={{ height: 40, borderColor: 'gray', borderWidth: 1 }}

            />
            <Text>
                To
      </Text>
            <TextInput
                style={{ height: 40, borderColor: 'gray', borderWidth: 1 }}

            />
            <Text>
                Date
      </Text>
            <TextInput
                style={{ height: 40, borderColor: 'gray', borderWidth: 1 }}
                value={this.state.date}
            />
            <Button
                title="Calendar"
                onPress={() =>
                    this.openAndroidDatePicker()
                }
            />
            <Text>
                Time
            </Text>
            <TextInput
                style={{ height: 40, borderColor: 'gray', borderWidth: 1 }}

            />
        </View>
        )
    }
}
export default CreateEvent;