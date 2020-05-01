import React, { useState } from 'react';
import { TypedUseSelectorHook, useDispatch, useSelector as useReduxSelector } from 'react-redux';
import SearchBar from '../../components/SearchBar';
import { RootState } from '../../rootReducer';
import { SearchBarContentType } from './reducers';
import { setSearchBarAction } from './actions';

export const useSelector: TypedUseSelectorHook<RootState> = useReduxSelector;

const SearchPage = () => {
    const dispatch = useDispatch();
    const searchBarContent = useSelector<SearchBarContentType>((state) => state.searchPageReducer.searchBarContent);


    return (
        <div>
            <SearchBar
                searchBarContent={searchBarContent}
                setSearchBarContent={setSearchBarAction}
            />
        </div>
    );
};

export default SearchPage;
