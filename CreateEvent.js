import React, { Component } from 'react';
import { connect } from 'react-redux';
import { handleFormChangeEventName } from './actions';

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
    convertToDateString(year, month, day) {
        return day.toString() + "-" + month.toString() + "-" + year.toString();
    }
    async openAndroidDatePicker() {
        try {
            const { action, year, month, day } = await DatePickerAndroid.open({
                date: new Date()
            });
            if (action !== DatePickerAndroid.dismissedAction) {
                console.log("year", year)
                this.props.handleFormChangeEventName({startingDate:this.convertToDateString(year, month, day) });
            }
        } catch ({ code, message }) {
            console.warn('Cannot open date picker', message);
        }
    }
    render() {
        const { navigate } = this.props.navigation;
        const {eventName, fromLocation,toLocation,startingDate,time,handleFormChangeEventName} =this.props;
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
                Event Name
                </Text>
            <TextInput value={eventName}  
            onChangeText={(text) => handleFormChangeEventName({eventName:text})}
                style={{ height: 40, borderColor: 'gray', borderWidth: 1 }}
            />
            <Text>
                From
                </Text>
            <TextInput value={fromLocation} 
            onChangeText={(text) => handleFormChangeEventName({fromLocation:text})}
                style={{ height: 40, borderColor: 'gray', borderWidth: 1 }}
            />
            <Text>
                To
            </Text>
            <TextInput value={toLocation}  onChangeText={(text) => handleFormChangeEventName({toLocation:text})}
                style={{ height: 40, borderColor: 'gray', borderWidth: 1 }}
            />
            <Text>
                Date
            </Text>
            <TextInput
                style={{ height: 40, borderColor: 'gray', borderWidth: 1 }}
                onChangeText={(text) => handleFormChangeEventName({startingDate:text})}
                value={startingDate}
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
            <TextInput  value={time}
                            onChangeText={(text) => handleFormChangeEventName({time:text})}

                style={{ height: 40, borderColor: 'gray', borderWidth: 1 ,    alignSelf: 'stretch'            }}
            />
        </View>
        )
    }
}
const mapStateToProps = (state, ownProps) => {
    return {
      eventName: state.createEvent.eventName,
      fromLocation: state.createEvent.fromLocation,
      toLocation:state.createEvent.toLocation,
      startingDate:state.createEvent.startingDate,
      time:state.createEvent.time
    }
  };
const mapDispatchToProps = {handleFormChangeEventName};
export default connect(mapStateToProps,mapDispatchToProps)(CreateEvent);