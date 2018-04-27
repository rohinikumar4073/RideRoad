const initialState = {
    eventName: "",
    fromLocation: "",
    toLocation: "",
    startingDate: "",
    time: ""
}
const createEvent = (state = initialState, action) => {
    switch (action.type) {
        case "HANDLE_FORM_CHANGE":
            return {...state,
              ... action.data};
            break;

        default:
            return state;
    }
}

export default createEvent;