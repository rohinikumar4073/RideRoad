
import { applyMiddleware, createStore } from "redux";
import thunk from "redux-thunk";
import promise from "./promise";
import array from "./array";
import reducers from "../reducers";
import { createLogger } from "redux-logger";
import { AsyncStorage } from "react-native";
import { persistStore, persistReducer } from 'redux-persist'
import storage from 'redux-persist/lib/storage' // defaults to localStorage for web and AsyncStorage for react-native


const isDebuggingInChrome = false;

const logger = createLogger({
  predicate: (getState, action) => isDebuggingInChrome,
  collapsed: true,
  duration: true
});
const persistConfig = {
  key: 'root',
  storage,
}
const createRideRoadStore = applyMiddleware(thunk, promise, array, logger)(
  createStore
);
function configureStore(onComplete) {
  const persistedReducer = persistReducer(persistConfig, reducers)
  let store = createRideRoadStore(persistedReducer)

  return store;
}

module.exports = configureStore;