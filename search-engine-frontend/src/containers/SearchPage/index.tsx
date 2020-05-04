import React, { useEffect, useMemo, useState } from 'react';
import { TypedUseSelectorHook, useDispatch, useSelector as useReduxSelector } from 'react-redux';
import {
    createStyles, fade, makeStyles, Theme,
} from '@material-ui/core/styles';
import SearchIcon from '@material-ui/icons/Search';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import InputBase from '@material-ui/core/InputBase';
import Container from '@material-ui/core/Container';
import Typography from '@material-ui/core/Typography';
import Link from '@material-ui/core/Link';
import { ExpansionPanel } from '@material-ui/core';
import ExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary';
import ExpansionPanelDetails from '@material-ui/core/ExpansionPanelDetails';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import LinearProgress from '@material-ui/core/LinearProgress';
import Grid from '@material-ui/core/Grid';
import { RootState } from '../../rootReducer';
import { fetchSearchResult, SearchBarContentType, SearchResultType } from './reducers';
import theme from '../../theme';
import { setSearchBarAction } from './actions';

export const useSelector: TypedUseSelectorHook<RootState> = useReduxSelector;
const useStyles = makeStyles((theme: Theme) => createStyles({
    root: {
        flexGrow: 1,
    },
    search: {
        position: 'relative',
        borderRadius: theme.shape.borderRadius,
        backgroundColor: fade(theme.palette.common.white, 0.15),
        '&:hover': {
            backgroundColor: fade(theme.palette.common.white, 0.25),
        },
        marginRight: theme.spacing(2),
        marginLeft: 0,
        width: '100%',
        [theme.breakpoints.up('sm')]: {
            marginLeft: theme.spacing(3),
            width: 'auto',
        },
    },
    searchIcon: {
        padding: theme.spacing(0, 2),
        height: '100%',
        position: 'absolute',
        pointerEvents: 'none',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
    },
    inputRoot: {
        color: 'inherit',
    },
    inputInput: {
        padding: theme.spacing(1, 1, 1, 0),
        // vertical padding + font size from searchIcon
        paddingLeft: `calc(1em + ${theme.spacing(4)}px)`,
        transition: theme.transitions.create('width'),
        width: '100%',
        [theme.breakpoints.up('md')]: {
            width: '60vw',
        },
    },
    heading: {
        fontSize: theme.typography.pxToRem(15),
        fontWeight: theme.typography.fontWeightLight,
    },
}));

const SearchPage = () => {
    const classes = useStyles(theme);
    const dispatch = useDispatch();
    const searchBarContent = useSelector<SearchBarContentType>((state) => state.searchPageReducer.searchBarContent);
    const searchResult = useSelector<SearchResultType>((state) => state.searchPageReducer.searchResult);
    const [isFetching, setIsFetching] = useState(false);

    useEffect(() => {
        if (isFetching) {
            fetchSearchResult(searchBarContent, dispatch, () => setIsFetching(false));
        }
    }, [isFetching]);

    const checkKeyPress = (event: React.KeyboardEvent<HTMLTextAreaElement | HTMLInputElement>) => {
        if (event.keyCode === 13) {
            // press enter
            setIsFetching(true);
        }
    };

    const mapSearchResultToView = (searchResultLocal: SearchResultType) => searchResultLocal?.searchResults?.map(((value) => {
        const keywordFreqComponent = value.keywordFrequencyModelList?.map((tuple, index) => (
            <Grid item xs={2}>
                <Typography>{index + 1}) {tuple?.[0]} {'<'}{tuple?.[1]}{'>'}</Typography>
            </Grid>
        ));

        const childLinkComponent = value?.childLinks?.length > 0 ? (
            <ul>
                {value?.childLinks?.map((link) => (
                    <li>
                        <Typography
                            variant='subtitle1'
                            display='inline'
                        >
                            {link}
                        </Typography>
                    </li>
                ))}
            </ul>
        ) : (<Typography variant='h5'>No child links for this record</Typography>);

        const parentLinkComponent = value?.parentLinks?.length > 0 ? (
            <ul>
                {value?.parentLinks?.map((link) => (
                    <li>
                        <Typography
                            variant='subtitle1'
                            display='inline'
                        >
                            {link}
                        </Typography>
                    </li>
                ))}
            </ul>
        ) : (<Typography variant='h5'>No parent links for this record</Typography>);

        return (
            <div>
                <Link target='_blank' href={`http://${value?.url}`}>
                    <Typography variant='h5'>
                        {value?.pageTitle}
                    </Typography>
                </Link>
                <Link target='_blank' href={`http://${value?.url}`}>
                    <Typography variant='caption'>{value?.url}</Typography>
                </Link>
                <div style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                }}
                >
                    <Typography variant='overline'>Last modified: {value?.lastModifiedDate},
                        Size: {value?.sizeOfPage}
                    </Typography>
                    <Typography variant='subtitle2'>Search score: {value?.score}</Typography>
                </div>
                <Grid container spacing={3}>
                    <Grid item xs={2}>
                        <Typography variant='subtitle1'>Top 5 Keywords: </Typography>
                    </Grid>
                    {keywordFreqComponent}
                </Grid>
                <ExpansionPanel>
                    <ExpansionPanelSummary
                        expandIcon={<ExpandMoreIcon />}
                        aria-controls='panel1a-content'
                        id='panel1a-header'
                    >
                        <Typography className={classes.heading}>Parent Links: </Typography>
                    </ExpansionPanelSummary>
                    <ExpansionPanelDetails>
                        {parentLinkComponent}
                    </ExpansionPanelDetails>
                </ExpansionPanel>
                <ExpansionPanel>
                    <ExpansionPanelSummary
                        expandIcon={<ExpandMoreIcon />}
                        aria-controls='panel2a-content'
                        id='panel2a-header'
                    >
                        <Typography className={classes.heading}>Child Links:</Typography>
                    </ExpansionPanelSummary>
                    <ExpansionPanelDetails>
                        {childLinkComponent}
                    </ExpansionPanelDetails>
                </ExpansionPanel>
                <br />
            </div>
        );
    }));

    const createSearchResultToView = (searchResultLocal: SearchResultType) => {
        if (searchResultLocal === undefined) return undefined;
        if (searchResultLocal?.searchResults?.length === 0 || !searchResultLocal?.searchResults) return <Typography variant='h3'>No record match this search</Typography>;
        return mapSearchResultToView(searchResultLocal);
    };

    const searchResultView = useMemo(() => createSearchResultToView(searchResult), [searchResult]);

    return (
        <div>
            <AppBar position='static'>
                <Toolbar style={{ display: 'inline-flex', justifyContent: 'space-between' }}>
                    <div className={classes.search}>
                        <div className={classes.searchIcon}>
                            <SearchIcon />
                        </div>
                        <InputBase
                            placeholder='Search…'
                            classes={{
                                root: classes.inputRoot,
                                input: classes.inputInput,
                            }}
                            value={searchBarContent ?? ''}
                            onKeyDown={checkKeyPress}
                            onChange={((event) => dispatch(setSearchBarAction(event.target.value)))}
                            inputProps={{ 'aria-label': 'search' }}
                        />
                    </div>
                    <div>
                        <Typography variant='h5'>
                            COMP4321 Search Engine
                        </Typography>
                    </div>
                </Toolbar>
                <LinearProgress variant='query' color='secondary' hidden={!isFetching} />
            </AppBar>
            <Container maxWidth='lg'>
                <div>
                    <Grid hidden={searchResult === undefined || !searchResult?.searchResults || searchResult?.searchResults?.length === 0}>
                        <Typography variant='overline'>Show {searchResult?.searchResults?.length} results out of {searchResult?.totalNumOfResult} documents ({searchResult?.totalTimeUsed} seconds)</Typography>
                    </Grid>
                    {searchResultView}
                </div>
            </Container>
        </div>
    );
};

export default SearchPage;
