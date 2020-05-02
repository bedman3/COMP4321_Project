import { Dispatch } from 'redux';
import { SearchPageReduxStateType, SET_SEARCH_BAR, SET_SEARCH_RESULT } from './constants';
import { commonHeader } from '../../api';
import { setSearchResultAction } from './actions';

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

export interface ISearchPageStore {
    searchBarContent: SearchBarContentType,
    searchResult: SearchResultType,
}

const searchPageInitialStore: ISearchPageStore = {
    searchBarContent: undefined,
    searchResult: undefined,
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
    default:
        return state;
    }
};

interface ISearchResultResponse {
    totalNumOfResult: number,
    searchResults: Array<ISearchResultRecord>,
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
                response.json().then((responseJson: SearchResultType) => {
                    dispatch(setSearchResultAction(responseJson));
                    callback();
                });
            }
        }).catch((fetchAPIError) => {
            console.log({ fetchAPIError });
        });
};
