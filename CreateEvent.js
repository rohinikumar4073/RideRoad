import React, { Component } from 'react';
import { connect } from 'react-redux';
import { handleFormChangeEventName } from './actions';
import DatePicker from 'react-native-datepicker'

import {
    Platform,
    StyleSheet,
    Text,
    View,
    TextInput,
    Button,
    DatePickerAndroid
} from 'react-native';
const styles = StyleSheet.create({
    headerStyle: {
        fontWeight: 'bold',
        fontSize: 20
    },
    labelStyle: {
        fontWeight: 'bold',
        fontSize: 16
    },
    textStyle:{
        height: 40
    }
});
class CreateEvent extends Component<Props> {
    constructor(props) {
        super(props);
        this.state = {
            date: ""
        };
    }
    
    render() {
        const { navigate } = this.props.navigation;
        const { eventName, fromLocation, toLocation, startingDate, time, handleFormChangeEventName } = this.props;
        return (<View style={{
            flex: 1,
            flexDirection: 'column',
            padding: 10
        }}>
            <Text style={styles.headerStyle}>
                Create Event
                    </Text>
            <Text style={styles.labelStyle}>
                Event Name
                </Text>
            <TextInput value={eventName}
                onChangeText={(text) => handleFormChangeEventName({ eventName: text })}
                style={styles.textStyle}
            />
            <Text style={styles.labelStyle}>
                From
                </Text>
            <TextInput value={fromLocation}
                onChangeText={(text) => handleFormChangeEventName({ fromLocation: text })}
                style={styles.textStyle}
            />
            <Text  style={styles.labelStyle}>
                To
            </Text>
            <TextInput value={toLocation} 
            onChangeText={(text) => handleFormChangeEventName({ toLocation: text })}
                 style={styles.textStyle}
            />
            <Text style={styles.labelStyle}>
                Date
            </Text>
            <DatePicker
                style={{ width: "100%" }}
                date={startingDate}
                mode="date"
                placeholder="Select date"
                format="YYYY-MM-DD"
                minDate="2016-05-01"
                confirmBtnText="Confirm"
                cancelBtnText="Cancel"
                customStyles={{
                    dateIcon: {
                        position: 'absolute',
                        right: 0,
                        top: 4,
                        marginRight: 0
                    },
                    dateInput: {
                        marginRight: 36
                    }
                }}
                onDateChange={(date) => { handleFormChangeEventName({ startingDate: date }) }}
            />
            <Text style={styles.labelStyle}>
                Time
            </Text>
            <TextInput   style={styles.textStyle} value={time}
                onChangeText={(text) => handleFormChangeEventName({ time: text })}
            />
        </View>
        )
    }
}
const mapStateToProps = (state, ownProps) => {
    return {
        eventName: state.createEvent.eventName,
        fromLocation: state.createEvent.fromLocation,
        toLocation: state.createEvent.toLocation,
        startingDate: state.createEvent.startingDate,
        time: state.createEvent.time
    }
};
const mapDispatchToProps = { handleFormChangeEventName };
export default connect(mapStateToProps, mapDispatchToProps)(CreateEvent);