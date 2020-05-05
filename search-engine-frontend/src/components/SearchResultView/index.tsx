import Grid from '@material-ui/core/Grid';
import Typography from '@material-ui/core/Typography';
import Link from '@material-ui/core/Link';
import { ExpansionPanel } from '@material-ui/core';
import ExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import ExpansionPanelDetails from '@material-ui/core/ExpansionPanelDetails';
import React, { useMemo } from 'react';
import { createStyles, makeStyles, Theme } from '@material-ui/core/styles';
import Button from '@material-ui/core/Button';
import { useDispatch } from 'react-redux';
import { ISearchResultRecord, SearchResultType } from '../../containers/SearchPage/reducers';
import theme from '../../theme';
import { setIsFetchingFlagAction, setSearchBarAction } from '../../containers/SearchPage/actions';

const useStyles = makeStyles((theme: Theme) => createStyles({
    heading: {
        fontSize: theme.typography.pxToRem(15),
        fontWeight: theme.typography.fontWeightLight,
    },
    root: {
        flexGrow: 1,
    },
}));

interface SearchResultViewProps {
    searchResult: SearchResultType
    enableGetSimilarPage?: boolean
}

const SearchResultView = (props: SearchResultViewProps) => {
    const classes = useStyles(theme);
    const dispatch = useDispatch();
    const { searchResult, enableGetSimilarPage } = props;

    const handleGetSimilarPage = (record: ISearchResultRecord) => {
        const newQuery = record?.keywordFrequencyModelList.map((pair) => pair[0]).join(' ');
        dispatch(setSearchBarAction(newQuery));
        dispatch(setIsFetchingFlagAction(true));
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
                <Grid container spacing={2}>
                    <Grid item xs={8}>
                        <Link target='_blank' href={`http://${value?.url}`}>
                            <Typography variant='h5'>
                                {value?.pageTitle}
                            </Typography>
                        </Link>
                        <Link target='_blank' href={`http://${value?.url}`}>
                            <Typography variant='caption'>{value?.url}</Typography>
                        </Link>
                    </Grid>
                    <div className={classes.root} />
                    <Grid item hidden={enableGetSimilarPage === undefined ? true : !enableGetSimilarPage}>
                        <Button variant='contained' color='primary' onClick={() => handleGetSimilarPage(value)}>
                            Get Similar Pages
                        </Button>
                    </Grid>
                </Grid>
                <Grid container spacing={2}>
                    <Grid item>
                        <Typography variant='overline'>Last modified: {value?.lastModifiedDate},
                            Size: {value?.sizeOfPage}
                        </Typography>
                    </Grid>
                    <div className={classes.root} />
                    <Grid item>
                        <Typography variant='subtitle2'>Search score: {value?.score}</Typography>
                    </Grid>
                </Grid>

                {/* </div> */}
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
        if (searchResultLocal?.searchResults?.length === 0 || !searchResultLocal?.searchResults) {
            return (
                <Typography
                    variant='h3'
                >No record match this search
                </Typography>
            );
        }
        return mapSearchResultToView(searchResultLocal);
    };

    const searchResultView = useMemo(() => createSearchResultToView(searchResult), [searchResult]);

    return (
        <div>
            {searchResultView}
        </div>
    );
};

export default SearchResultView;
