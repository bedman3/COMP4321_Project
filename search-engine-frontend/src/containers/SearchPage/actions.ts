import { SearchBarContentType, SearchResultType } from './reducers';
import { SearchPageReduxStateType, SET_SEARCH_BAR, SET_SEARCH_RESULT } from './constants';

export const setSearchBarAction = (data: SearchBarContentType): SearchPageReduxStateType => ({
    type: SET_SEARCH_BAR,
    payload: data,
});

export const setSearchResultAction = (data: SearchResultType): SearchPageReduxStateType => ({
    type: SET_SEARCH_RESULT,
    payload: data,
});

export type setSearchBarActionType = typeof setSearchBarAction;
