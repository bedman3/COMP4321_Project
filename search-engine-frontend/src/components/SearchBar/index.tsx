import React, { useState } from 'react';
import { TextField } from '@material-ui/core';
import { useDispatch } from 'react-redux';
import { SearchBarContentType } from '../../containers/SearchPage/reducers';
import { setSearchBarActionType } from '../../containers/SearchPage/actions';

interface SearchBarProps {
    searchBarContent: SearchBarContentType,
    setSearchBarContent: setSearchBarActionType,
}

const SearchBar = (props: SearchBarProps) => {
    const dispatch = useDispatch();
    const [state, setState] = useState(0);

    const { searchBarContent } = props;

    return (
        <div>
            <TextField />
        </div>
    );
};
export default SearchBar;
