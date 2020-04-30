import { SearchBarContentType } from './reducers';
import { SearchPageReduxStateType, SET_SEARCH_BAR } from './constants';

export const setSearchBarAction = (data: SearchBarContentType): SearchPageReduxStateType => ({
    type: SET_SEARCH_BAR,
    payload: data,
});
