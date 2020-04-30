import { SearchBarContentType } from './reducers';

export const SET_SEARCH_BAR = 'SET_SEARCH_BAR';

interface SetSearchBarAction {
    type: typeof SET_SEARCH_BAR,
    payload: SearchBarContentType,
}

export type SearchPageReduxStateType = SetSearchBarAction
