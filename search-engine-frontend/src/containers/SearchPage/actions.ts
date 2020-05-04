import { SearchBarContentType, SearchResultType, StemmedKeywordListType } from './reducers';
import {
    SearchPageReduxStateType,
    SET_IS_FETCHING_FLAG,
    SET_SEARCH_BAR,
    SET_SEARCH_RESULT,
    SET_STEMMED_KEYWORDS_LIST,
} from './constants';

export const setSearchBarAction = (data: SearchBarContentType): SearchPageReduxStateType => ({
    type: SET_SEARCH_BAR,
    payload: data,
});

export const setSearchResultAction = (data: SearchResultType): SearchPageReduxStateType => ({
    type: SET_SEARCH_RESULT,
    payload: data,
});

export const setStemmedKeywordsListAction = (data: StemmedKeywordListType) : SearchPageReduxStateType => ({
    type: SET_STEMMED_KEYWORDS_LIST,
    payload: data,
});

export const setIsFetchingFlagAction = (data: boolean) : SearchPageReduxStateType => ({
    type: SET_IS_FETCHING_FLAG,
    payload: data,
});

export type setSearchBarActionType = typeof setSearchBarAction;
