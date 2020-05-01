import { SearchPageReduxStateType, SET_SEARCH_BAR } from './constants';

export type SearchBarContentType = string | undefined;

export interface SearchPageStoreType {
    searchBarContent: SearchBarContentType,
}

const searchPageInitialStore: SearchPageStoreType = {
    searchBarContent: undefined,
};

export const searchPageReducer = (
    state: SearchPageStoreType = searchPageInitialStore,
    action: SearchPageReduxStateType,
) => {
    const { payload, type } = action;

    switch (type) {
    case SET_SEARCH_BAR:
        return {
            ...state,
            searchBarContent: payload as SearchBarContentType,
        };
    default:
        return state;
    }
};
