import React, { useEffect, useMemo, useState } from 'react';
import { TypedUseSelectorHook, useDispatch, useSelector as useReduxSelector } from 'react-redux';
import { createStyles, Theme, Typography } from '@material-ui/core';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import Grid from '@material-ui/core/Grid';
import makeStyles from '@material-ui/core/styles/makeStyles';
import { Autocomplete } from '@material-ui/lab';
import TextField from '@material-ui/core/TextField';
import {
    fetchQueryHistory, IQueryHistory, QueryHistoryType, SearchResultType,
} from '../SearchPage/reducers';
import { RootState } from '../../rootReducer';
import SearchResultView from '../../components/SearchResultView';

export const useSelector: TypedUseSelectorHook<RootState> = useReduxSelector;

const useStyles = makeStyles((theme: Theme) => createStyles({
    root: {
        flexGrow: 1,
        maxWidth: 752,
    },
    demo: {
        backgroundColor: theme.palette.background.paper,
    },
    title: {
        margin: theme.spacing(4, 0, 2),
    },
}));

const QueryHistoryPage = () => {
    const dispatch = useDispatch();
    const classes = useStyles();
    const queryHistory = useSelector((state) => state.searchPageReducer.queryHistory);
    const [searchResult, setSearchResult] = useState<SearchResultType>(undefined);

    useEffect(() => {
        fetchQueryHistory(dispatch);
    }, []);

    const handleShowResult = (value: IQueryHistory | undefined) => {
        setSearchResult(value?.queryResponse);
    };

    return (
        <div>
            <Grid container spacing={1}>
                <Grid item xs={12}>
                    <Typography variant='h6' className={classes.title}>
                        Query History
                    </Typography>
                    <Autocomplete
                        id='tags-outlined'
                        options={queryHistory ?? []}
                        getOptionLabel={(option) => option.rawQuery}
                        filterSelectedOptions
                        renderInput={(params) => (
                            <TextField
                                {...params}
                                variant='outlined'
                                label='Select query history to display search result'
                            />
                        )}
                        // @ts-ignore
                        onChange={((event, value) => handleShowResult(value))}
                    />
                </Grid>
                <Grid xs={12}>
                    <SearchResultView
                        searchResult={searchResult}
                    />
                </Grid>
            </Grid>
        </div>
    );
};


export default QueryHistoryPage;
