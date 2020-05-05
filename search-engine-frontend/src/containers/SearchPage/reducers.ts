import { Dispatch } from 'redux';
import {
    SearchPageReduxStateType,
    SET_IS_FETCHING_FLAG,
    SET_QUERY_HISTORY,
    SET_SEARCH_BAR,
    SET_SEARCH_RESULT,
    SET_STEMMED_KEYWORDS_LIST,
} from './constants';
import { commonHeader } from '../../api';
import { setQueryHistoryAction, setSearchResultAction, setStemmedKeywordsListAction } from './actions';

export interface ISearchResultRecord {
    pageTitle: string,
    url: string,
    lastModifiedDate: string,
    sizeOfPage: string,
    score: number,
    keywordFrequencyModelList: string[][],
    childLinks: string[],
    parentLinks: string[],
}

export interface IQueryHistory {
    rawQuery: string,
    queryResponse: ISearchResultResponse,
}

export type SearchBarContentType = string | undefined;
export type SearchResultType = ISearchResultResponse | undefined;
export type StemmedKeywordListType = string[] | undefined;
export type QueryHistoryType = IQueryHistory[] | undefined

export interface ISearchPageStore {
    searchBarContent: SearchBarContentType,
    searchResult: SearchResultType,
    stemmedKeywordsList: StemmedKeywordListType,
    isFetching: boolean,
    queryHistory: QueryHistoryType,
}

const searchPageInitialStore: ISearchPageStore = {
    searchBarContent: undefined,
    searchResult: undefined,
    stemmedKeywordsList: undefined,
    isFetching: false,
    queryHistory: undefined,
};

export const searchPageReducer = (
    state: ISearchPageStore = searchPageInitialStore,
    action: SearchPageReduxStateType,
): ISearchPageStore => {
    const { payload, type } = action;

    switch (type) {
    case SET_SEARCH_BAR:
        return {
            ...state,
            searchBarContent: payload as SearchBarContentType,
        };
    case SET_SEARCH_RESULT:
        return {
            ...state,
            searchResult: payload as SearchResultType,
        };
    case SET_STEMMED_KEYWORDS_LIST:
        return {
            ...state,
            stemmedKeywordsList: payload as StemmedKeywordListType,
        };
    case SET_IS_FETCHING_FLAG:
        return {
            ...state,
            isFetching: payload as boolean,
        };
    case SET_QUERY_HISTORY:
        return {
            ...state,
            queryHistory: payload as QueryHistoryType,
        };
    default:
        return state;
    }
};

interface ISearchResultResponse {
    totalNumOfResult: number,
    totalTimeUsed: number,
    searchResults: Array<ISearchResultRecord>,
}

interface IStemmedKeywordResponse {
    stemmedKeywordList: StemmedKeywordListType,
}

export const fetchSearchResult = (searchBarContent: SearchBarContentType, dispatch: Dispatch, callback: () => void) => {
    fetch(`${process.env.REACT_APP_BACKEND_URL}/search`, {
        ...commonHeader,
        method: 'POST',
        body: JSON.stringify({
            query: searchBarContent ?? '',
        }),
    })
        .then((response) => {
            if (response.status === 200) {
                response.json().then((responseJson: ISearchResultResponse) => {
                    dispatch(setSearchResultAction(responseJson));
                    callback?.();
                });
            } else {
                console.log({ fetchAPIError: response.json() });
            }
        }).catch((fetchAPIError) => {
            console.log({ fetchAPIError });
        });
};

export const fetchStemmedKeyword = (dispatch: Dispatch) => {
    fetch(`${process.env.REACT_APP_BACKEND_URL}/stemmed-keywords`, {
        ...commonHeader,
        method: 'GET',
    }).then((response) => {
        if (response.status === 200) {
            response.json().then((responseJson: IStemmedKeywordResponse) => {
                if (responseJson?.stemmedKeywordList?.[0] === '') {
                    responseJson?.stemmedKeywordList.shift();
                }
                dispatch(setStemmedKeywordsListAction(responseJson?.stemmedKeywordList));
            });
        } else {
            console.log({ fetchAPIError: response.json() });
        }
    }).catch((fetchAPIError) => {
        console.log({ fetchAPIError });
    });
};

export const fetchQueryHistory = (dispatch: Dispatch) => {
    fetch(`${process.env.REACT_APP_BACKEND_URL}/query-history`, {
        ...commonHeader,
        method: 'GET',
    }).then((response) => {
        if (response.status === 200) {
            response.json().then((responseJson: IQueryHistory[]) => {
                dispatch(setQueryHistoryAction(responseJson));
            });
        } else {
            console.log({ fetchAPIError: response.json() });
        }
    }).catch((fetchAPIError) => {
        console.log({ fetchAPIError });
    });
};
