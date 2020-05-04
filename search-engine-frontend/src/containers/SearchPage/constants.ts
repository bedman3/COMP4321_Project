import { SearchBarContentType, SearchResultType, StemmedKeywordListType } from './reducers';

export const SET_SEARCH_BAR = 'SET_SEARCH_BAR';
export const SET_SEARCH_RESULT = 'SET_SEARCH_RESULT';
export const SET_STEMMED_KEYWORDS_LIST = 'SET_STEMMED_KEYWORDS_LIST';

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

export type SearchPageReduxStateType = SetSearchBarAction | SetSearchResultAction | SetStemmedKeywordsListAction
