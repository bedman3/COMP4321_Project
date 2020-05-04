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
import IconButton from '@material-ui/core/IconButton';
import MenuIcon from '@material-ui/icons/Menu';
import clsx from 'clsx';
import Drawer from '@material-ui/core/Drawer';
import ChevronLeftIcon from '@material-ui/icons/ChevronLeft';
import ChevronRightIcon from '@material-ui/icons/ChevronRight';
import Divider from '@material-ui/core/Divider';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import ListItemText from '@material-ui/core/ListItemText';
import InboxIcon from '@material-ui/icons/MoveToInbox';
import MailIcon from '@material-ui/icons/Mail';
import { setSearchBarAction } from './actions';

import theme from '../../theme';
import {
    fetchSearchResult, fetchStemmedKeyword, SearchBarContentType, SearchResultType,
} from './reducers';
import { RootState } from '../../rootReducer';

const drawerWidth = 240;

export const useSelector: TypedUseSelectorHook<RootState> = useReduxSelector;
const useStyles = makeStyles((theme: Theme) => createStyles({
    appBar: {
        transition: theme.transitions.create(['margin', 'width'], {
            easing: theme.transitions.easing.sharp,
            duration: theme.transitions.duration.leavingScreen,
        }),
    },
    appBarShift: {
        width: `calc(100% - ${drawerWidth}px)`,
        marginLeft: drawerWidth,
        transition: theme.transitions.create(['margin', 'width'], {
            easing: theme.transitions.easing.easeOut,
            duration: theme.transitions.duration.enteringScreen,
        }),
    },
    root: {
        display: 'flex',
    },
    hide: {
        display: 'none',
    },
    content: {
        flexGrow: 1,
        padding: theme.spacing(3),
        transition: theme.transitions.create('margin', {
            easing: theme.transitions.easing.sharp,
            duration: theme.transitions.duration.leavingScreen,
        }),
        marginLeft: -drawerWidth,
    },
    contentShift: {
        transition: theme.transitions.create('margin', {
            easing: theme.transitions.easing.easeOut,
            duration: theme.transitions.duration.enteringScreen,
        }),
        marginLeft: 0,
    },
    drawer: {
        width: drawerWidth,
        flexShrink: 0,
    },
    drawerPaper: {
        width: drawerWidth,
    },
    drawerHeader: {
        display: 'flex',
        alignItems: 'center',
        padding: theme.spacing(0, 1),
        // necessary for content to be below app bar
        ...theme.mixins.toolbar,
        justifyContent: 'flex-end',
    },
    menuButton: {
        marginRight: theme.spacing(2),
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
            width: '50ch',
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
        fetchStemmedKeyword(dispatch);
    }, []);

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

    const [drawerOpen, setDrawerOpen] = useState(false);

    const handleDrawerOpen = () => {
        setDrawerOpen(true);
    };

    const handleDrawerClose = () => {
        setDrawerOpen(false);
    };


    return (
        <div>
            <AppBar
                position='fixed'
                className={clsx(classes.appBar, {
                    [classes.appBarShift]: drawerOpen,
                })}
            >
                <Toolbar>
                    {/* <Toolbar style={{ display: 'inline-flex', justifyContent: 'space-between' }}> */}
                    <IconButton
                        color='inherit'
                        aria-label='open drawer'
                        onClick={handleDrawerOpen}
                        edge='start'
                        className={clsx(classes.menuButton, drawerOpen && classes.hide)}
                    >
                        <MenuIcon />
                    </IconButton>
                    <Typography variant='h5'>
                        COMP4321 Search Engine
                    </Typography>
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
                </Toolbar>
                <LinearProgress variant='query' color='secondary' hidden={!isFetching} />
            </AppBar>
            <Drawer
                className={classes.drawer}
                variant='persistent'
                anchor='left'
                open={drawerOpen}
                classes={{
                    paper: classes.drawerPaper,
                }}
            >
                <div className={classes.drawerHeader}>
                    <IconButton onClick={handleDrawerClose}>
                        {theme.direction === 'ltr' ? <ChevronLeftIcon /> : <ChevronRightIcon />}
                    </IconButton>
                </div>
                <Divider />
                <List>
                    {['Inbox', 'Starred', 'Send email', 'Drafts'].map((text, index) => (
                        <ListItem button key={text}>
                            <ListItemIcon>{index % 2 === 0 ? <InboxIcon /> : <MailIcon />}</ListItemIcon>
                            <ListItemText primary={text} />
                        </ListItem>
                    ))}
                </List>
                <Divider />
                <List>
                    {['All mail', 'Trash', 'Spam'].map((text, index) => (
                        <ListItem button key={text}>
                            <ListItemIcon>{index % 2 === 0 ? <InboxIcon /> : <MailIcon />}</ListItemIcon>
                            <ListItemText primary={text} />
                        </ListItem>
                    ))}
                </List>
            </Drawer>
            <main
                className={clsx(classes.content, {
                    [classes.contentShift]: drawerOpen,
                })}
            >
                <div className={classes.drawerHeader} />
                <Container maxWidth='lg'>
                    <div>
                        <Grid hidden={searchResult === undefined || !searchResult?.searchResults || searchResult?.searchResults?.length === 0}>
                            <Typography variant='overline'>Show {searchResult?.searchResults?.length} results out of {searchResult?.totalNumOfResult} documents ({searchResult?.totalTimeUsed} seconds)</Typography>
                        </Grid>
                        {searchResultView}
                    </div>
                </Container>
            </main>

        </div>
    );
};

export default SearchPage;
