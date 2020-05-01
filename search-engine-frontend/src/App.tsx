import React from 'react';
import { withRouter } from 'react-router-dom';
import { ThemeProvider } from '@material-ui/core';
import SearchPage from './containers/SearchPage';
import theme from './theme';

const App = () => (
    <div>
        <link rel='stylesheet' href='https://fonts.googleapis.com/css?family=Roboto:300,400,500,700&display=swap' />
        <link rel='stylesheet' href='https://fonts.googleapis.com/icon?family=Material+Icons' />

        <ThemeProvider theme={theme}>
            <SearchPage />
        </ThemeProvider>
    </div>
);

export default withRouter(App);
