import {
    QueryHistoryType, SearchBarContentType, SearchResultType, StemmedKeywordListType,
} from './reducers';

export const SET_SEARCH_BAR = 'SET_SEARCH_BAR';
export const SET_SEARCH_RESULT = 'SET_SEARCH_RESULT';
export const SET_STEMMED_KEYWORDS_LIST = 'SET_STEMMED_KEYWORDS_LIST';
export const SET_IS_FETCHING_FLAG = 'SET_IS_FETCHING_FLAG';
export const SET_QUERY_HISTORY = 'SET_QUERY_HISTORY';

interface SetSearchBarAction {
    type: typeof SET_SEARCH_BAR,
    payload: SearchBarContentType,
}

interface SetSearchResultAction {
    type: typeof SET_SEARCH_RESULT,
    payload: SearchResultType,
}

interface SetStemmedKeywordsListAction {
    type: typeof SET_STEMMED_KEYWORDS_LIST,
    payload: StemmedKeywordListType,
}

interface SetIsFetchingFlagAction {
    type: typeof SET_IS_FETCHING_FLAG,
    payload: boolean,
}

interface setQueryHistoryAction {
    type: typeof SET_QUERY_HISTORY,
    payload: QueryHistoryType
}

export type SearchPageReduxStateType = SetSearchBarAction | SetSearchResultAction | SetStemmedKeywordsListAction
    | SetIsFetchingFlagAction | setQueryHistoryAction
