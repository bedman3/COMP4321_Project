import React from 'react';
import ReactDOM from 'react-dom';
import { Provider } from 'react-redux';
import {
    applyMiddleware, compose, createStore, Middleware,
} from 'redux';
import thunk from 'redux-thunk';
import { createBrowserHistory, History } from 'history';
import { ConnectedRouter, routerMiddleware } from 'connected-react-router';
import { ThemeProvider } from '@material-ui/core';
import App from './App';
import * as serviceWorker from './serviceWorker';
import createRootReducer from './rootReducer';
import theme from './theme';


const history: History = createBrowserHistory();
const middlewares = [thunk];
const enhancers: Array<Middleware> = [];

const composeEnhancers: typeof compose = (window as any).__REDUX_DEVTOOLS_EXTENSION_COMPOSE__ || compose;

function configureStore() {
    return createStore(
        createRootReducer(history), // root reducer with router state
        // preloadedState,
        composeEnhancers(
            applyMiddleware(
                routerMiddleware(history), // for dispatching history actions
                ...middlewares,
                ...enhancers,
            ),
        ),
    );
}

const store = configureStore();
const target = document.querySelector('#root');
// document.body.style.overflow = 'hidden';
// document.getElementById('root'),


const Index = () => (
    <Provider store={store}>
        <ConnectedRouter history={history}>
            <link rel='stylesheet' href='https://fonts.googleapis.com/css?family=Roboto:300,400,500,700&display=swap' />
            <link rel='stylesheet' href='https://fonts.googleapis.com/icon?family=Material+Icons' />
            <ThemeProvider theme={theme}>

                <App />
            </ThemeProvider>
        </ConnectedRouter>
    </Provider>
);

ReactDOM.render(<Index />, target);

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister();
