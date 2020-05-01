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
    keywordFrequencyModelList: string,
    childLinks: string[],
    parentLinks: string[],
}

export type SearchBarContentType = string | undefined;
export type SearchResultType = Array<ISearchResultRecord> | undefined;

export interface ISearchPageStore {
    searchBarContent: SearchBarContentType,
    searchResult: SearchResultType,
}

const tempSearchResult = [
    {
        pageTitle: 'Interactive Visual Analysis of Human Behavior-Oriented Videos | HKUST CSE',
        url: 'www.cse.ust.hk/pg/defenses/S20/hzengac-04-05-2020.html',
        lastModifiedDate: 'Fri, 01 May 2020 15:01:00 GMT',
        sizeOfPage: '4035 characters',
        score: 0.03793415466952129,
        keywordFrequencyModelList: 'video 17;visual 13;research 9;interact 6;postgradu 7;',
        childLinks: [
            'www.cse.ust.hk/admin/people/faculty',
            'www.cse.ust.hk/admin/contact',
            'www.cse.ust.hk/admin/people/staff',
            'www.cse.ust.hk/News',
            'www.cse.ust.hk/admin/about',
            'www.cse.ust.hk/admin/people/pg',
            'www.cse.ust.hk/ug/enrichment',
            'www.cse.ust.hk/admin/recruitment',
            'www.cse.ust.hk/admin/facilities',
            'www.cse.ust.hk/pg/admissions',
            'www.cse.ust.hk/admin/search',
            'www.cse.ust.hk/ug',
            'www.cse.ust.hk',
            'www.cse.ust.hk/admin/qa',
            'www.cse.ust.hk/admin/industry_collaboration',
            'www.cse.ust.hk/admin/intranet',
            'www.cse.ust.hk/pg/ourgraduates',
            'www.cse.ust.hk/admin/sitemap',
            'www.cse.ust.hk/pg/research/areas',
            'www.cse.ust.hk/Restricted',
            'www.cse.ust.hk/admin/mission',
            'www.cse.ust.hk/pg/research/projects',
            'www.cse.ust.hk/pg/admissions/recruiting',
            'www.cse.ust.hk/admin/factsheet',
            'www.cse.ust.hk/ug/admissions',
            'www.cse.ust.hk/pg',
            'www.cse.ust.hk/admin/people/alumni',
            'www.cse.ust.hk/pg/research/labs',
            'www.cse.ust.hk/admin/welcome',
        ],
        parentLinks: [
            'www.cse.ust.hk/pg/defenses',
        ],
    },
    {
        pageTitle: 'Visual Analytics of Big Data | HKUST CSE',
        url: 'www.cse.ust.hk/ug/fyt/19-20/hua2.html',
        lastModifiedDate: 'Fri, 01 May 2020 15:01:02 GMT',
        sizeOfPage: '1505 characters',
        score: 0.026674188503804066,
        keywordFrequencyModelList: 'research 8;faculti 4;postgradu 7;univers 4;hong 3;',
        childLinks: [
            'www.cse.ust.hk/admin/people/faculty',
            'www.cse.ust.hk/admin/contact',
            'www.cse.ust.hk/admin/people/staff',
            'www.cse.ust.hk/News',
            'www.cse.ust.hk/admin/about',
            'www.cse.ust.hk/admin/people/pg',
            'www.cse.ust.hk/ug/enrichment',
            'www.cse.ust.hk/admin/recruitment',
            'www.cse.ust.hk/admin/facilities',
            'www.cse.ust.hk/pg/admissions',
            'www.cse.ust.hk/admin/search',
            'www.cse.ust.hk/ug',
            'www.cse.ust.hk',
            'www.cse.ust.hk/admin/qa',
            'www.cse.ust.hk/admin/industry_collaboration',
            'www.cse.ust.hk/admin/intranet',
            'www.cse.ust.hk/pg/ourgraduates',
            'www.cse.ust.hk/admin/sitemap',
            'www.cse.ust.hk/pg/research/areas',
            'www.cse.ust.hk/Restricted',
            'www.cse.ust.hk/admin/mission',
            'www.cse.ust.hk/pg/research/projects',
            'www.cse.ust.hk/pg/admissions/recruiting',
            'www.cse.ust.hk/admin/factsheet',
            'www.cse.ust.hk/ug/admissions',
            'www.cse.ust.hk/pg',
            'www.cse.ust.hk/admin/people/alumni',
            'www.cse.ust.hk/pg/research/labs',
            'www.cse.ust.hk/admin/welcome',
        ],
        parentLinks: [
            'www.cse.ust.hk/ug/fyt',
        ],
    },
];

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

export const fetchSearchResult = (searchBarContent: SearchBarContentType, dispatch: Dispatch) => {
    fetch('http://localhost:8080/search', {
        ...commonHeader,
        method: 'POST',
        body: JSON.stringify({
            query: searchBarContent ?? '',
        }),
    })
        .then((response) => {
            if (response.status === 200) {
                // do sth
                response.json().then((responseJson: SearchResultType) => {
                    dispatch(setSearchResultAction(responseJson));
                });
            }
        }).catch((fetchAPIError) => {
            console.log({ fetchAPIError });
        });
};
