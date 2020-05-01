import React from 'react';
import { withRouter } from 'react-router-dom';
import SearchPage from './containers/SearchPage';

function App() {
    return (
        <div>
            <link rel='stylesheet' href='https://fonts.googleapis.com/css?family=Roboto:300,400,500,700&display=swap' />
            <link rel='stylesheet' href='https://fonts.googleapis.com/icon?family=Material+Icons' />
            <SearchPage />
        </div>
    );
}

export default withRouter(App);
