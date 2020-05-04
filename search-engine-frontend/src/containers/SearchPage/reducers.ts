import { Dispatch } from 'redux';
import {
    SearchPageReduxStateType, SET_IS_FETCHING_FLAG, SET_SEARCH_BAR, SET_SEARCH_RESULT, SET_STEMMED_KEYWORDS_LIST,
} from './constants';
import { commonHeader } from '../../api';
import { setSearchResultAction, setStemmedKeywordsListAction } from './actions';

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

export type SearchBarContentType = string | undefined;
export type SearchResultType = ISearchResultResponse | undefined;
export type StemmedKeywordListType = string[] | undefined;

export interface ISearchPageStore {
    searchBarContent: SearchBarContentType,
    searchResult: SearchResultType,
    stemmedKeywordsList: StemmedKeywordListType,
    isFetching: boolean,
}

const searchPageInitialStore: ISearchPageStore = {
    searchBarContent: undefined,
    searchResult: undefined,
    stemmedKeywordsList: undefined,
    isFetching: false,
    // searchResult: tempSearchResult,
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
    fetch('http://localhost:8080/search', {
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
            }
        }).catch((fetchAPIError) => {
            console.log({ fetchAPIError });
        });
};

export const fetchStemmedKeyword = (dispatch: Dispatch) => {
    fetch('http://localhost:8080/stemmed-keywords', {
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
        }
    });
};
