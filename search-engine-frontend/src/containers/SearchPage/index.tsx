import React, { useEffect } from 'react';
import { TypedUseSelectorHook, useDispatch, useSelector as useReduxSelector } from 'react-redux';
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import { setIsFetchingFlagAction } from './actions';
import { fetchSearchResult, SearchBarContentType, SearchResultType } from './reducers';
import { RootState } from '../../rootReducer';
import SearchResultView from '../../components/SearchResultView';

export const useSelector: TypedUseSelectorHook<RootState> = useReduxSelector;


const SearchPage = () => {
    const dispatch = useDispatch();
    const searchBarContent = useSelector<SearchBarContentType>((state) => state.searchPageReducer.searchBarContent);
    const searchResult = useSelector<SearchResultType>((state) => state.searchPageReducer.searchResult);
    const isFetching = useSelector((state) => state.searchPageReducer.isFetching);

    useEffect(() => {
        if (isFetching) {
            fetchSearchResult(searchBarContent, dispatch, () => dispatch(setIsFetchingFlagAction(false)));
        }
    }, [isFetching]);


    return (
        <div>
            <Grid hidden={searchResult === undefined || !searchResult?.searchResults || searchResult?.searchResults?.length === 0}>
                <Typography variant='overline'>Show {searchResult?.searchResults?.length} results out of {searchResult?.totalNumOfResult} documents ({searchResult?.totalTimeUsed} seconds)</Typography>
            </Grid>
            <SearchResultView
                searchResult={searchResult}
                enableGetSimilarPage
            />
        </div>
    );
};

export default SearchPage;
