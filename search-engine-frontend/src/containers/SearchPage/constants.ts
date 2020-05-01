import { SearchBarContentType, SearchResultType } from './reducers';

export const SET_SEARCH_BAR = 'SET_SEARCH_BAR';
export const SET_SEARCH_RESULT = 'SET_SEARCH_RESULT';

interface SetSearchBarAction {
    type: typeof SET_SEARCH_BAR,
    payload: SearchBarContentType,
}

interface SetSearchResultAction {
    type: typeof SET_SEARCH_RESULT,
    payload: SearchResultType,
}

export type SearchPageReduxStateType = SetSearchBarAction | SetSearchResultAction
