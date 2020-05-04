import React, { useEffect, useMemo, useState } from 'react';
import {
    Link, Redirect, Route, Switch, useLocation, withRouter,
} from 'react-router-dom';
import AppBar from '@material-ui/core/AppBar';
import clsx from 'clsx';
import Toolbar from '@material-ui/core/Toolbar';
import IconButton from '@material-ui/core/IconButton';
import MenuIcon from '@material-ui/icons/Menu';
import Typography from '@material-ui/core/Typography';
import SearchIcon from '@material-ui/icons/Search';
import InputBase from '@material-ui/core/InputBase';
import LinearProgress from '@material-ui/core/LinearProgress';
import Drawer from '@material-ui/core/Drawer';
import ChevronLeftIcon from '@material-ui/icons/ChevronLeft';
import ChevronRightIcon from '@material-ui/icons/ChevronRight';
import Divider from '@material-ui/core/Divider';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import ListItemText from '@material-ui/core/ListItemText';
import {
    createStyles, fade, makeStyles, Theme,
} from '@material-ui/core/styles';
import { useDispatch } from 'react-redux';
import MenuBookIcon from '@material-ui/icons/MenuBook';
import Container from '@material-ui/core/Container';
import HistoryIcon from '@material-ui/icons/History';
import { setIsFetchingFlagAction, setSearchBarAction } from './containers/SearchPage/actions';
import StemmedKeywordPage from './containers/StemmedKeywordPage';
import theme from './theme';
import SearchPage, { useSelector } from './containers/SearchPage';
import { fetchStemmedKeyword, SearchBarContentType } from './containers/SearchPage/reducers';
import TestingPage from './containers/TestingPage';
import SearchBarForLongOptionList from './components/SearchBarForLongOptionList';
import QueryHistoryPage from './containers/QueryHistoryPage';

const drawerWidth = 240;

const useStyles = makeStyles((theme: Theme) => createStyles({
    grow: {
        flexGrow: 1,
    },
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
    chips: {
        display: 'flex',
        flexWrap: 'wrap',
    },
    chip: {
        margin: 2,
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
    hidden: {
        hidden: true,
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
            width: '70ch',
        },
    },
    heading: {
        fontSize: theme.typography.pxToRem(15),
        fontWeight: theme.typography.fontWeightLight,
    },
    toolBarRightElement: {
        marginRight: theme.spacing(2),
    },
}));


const App = () => {
    const location = useLocation();
    const classes = useStyles(theme);
    const dispatch = useDispatch();
    const [drawerOpen, setDrawerOpen] = useState(false);
    const searchBarContent = useSelector<SearchBarContentType>((state) => state.searchPageReducer.searchBarContent);
    const isFetching = useSelector((state) => state.searchPageReducer.isFetching);
    const stemmedKeywordList = useSelector((state) => state.searchPageReducer.stemmedKeywordsList);
    const searchBarVisibilityStyle = useMemo(() => (location.pathname !== '/' ? { display: 'none' } : {}), [location.pathname]);

    useEffect(() => {
        fetchStemmedKeyword(dispatch);
    }, []);

    const handleDrawerOpen = () => {
        setDrawerOpen(true);
    };

    const handleDrawerClose = () => {
        setDrawerOpen(false);
    };

    const checkKeyPress = (event: React.KeyboardEvent<HTMLTextAreaElement | HTMLInputElement>) => {
        if (event.keyCode === 13) {
            // press enter
            dispatch(setIsFetchingFlagAction(true));
        }
    };

    const handleStemmedKeywordSelection = (value: string) => {
        if (value !== undefined && value !== null) dispatch(setSearchBarAction(`${searchBarContent ?? ''} ${value}`));
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
                    <Typography variant='h6'>
                        COMP4321 Project
                    </Typography>
                    <div className={classes.search} style={searchBarVisibilityStyle}>
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
                    <div className={classes.grow} />
                    <div className={classes.search} style={searchBarVisibilityStyle}>
                        <SearchBarForLongOptionList
                            optionList={stemmedKeywordList}
                            labelText='Search stemmed keywords to add to search bar'
                            appBar
                            getValueCallback={handleStemmedKeywordSelection}
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
                    <ListItem button component={Link} to='/'>
                        <ListItemIcon><SearchIcon /></ListItemIcon>
                        <ListItemText primary='Search' />
                    </ListItem>
                    <ListItem button component={Link} to='/StemmedKeywords'>
                        <ListItemIcon><MenuBookIcon /></ListItemIcon>
                        <ListItemText primary='Stemmed Keywords List' />
                    </ListItem>
                    <ListItem button component={Link} to='/QueryHistory'>
                        <ListItemIcon><HistoryIcon /></ListItemIcon>
                        <ListItemText primary='Query History' />
                    </ListItem>
                </List>
            </Drawer>
            <main
                className={clsx(classes.content, {
                    [classes.contentShift]: drawerOpen,
                })}
            >
                <div className={classes.drawerHeader} />
                <Container maxWidth='lg'>
                    <Switch>
                        <Route path='/' exact component={SearchPage} />
                        <Route path='/StemmedKeywords' exact component={StemmedKeywordPage} />
                        <Route path='/QueryHistory' exact component={QueryHistoryPage} />
                        <Route path='/Testing' exact component={TestingPage} />
                        <Redirect to='/' />
                    </Switch>
                </Container>

            </main>
        </div>
    );
};

export default withRouter(App);
