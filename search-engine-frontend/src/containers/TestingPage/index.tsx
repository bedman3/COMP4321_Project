import { TypedUseSelectorHook, useDispatch, useSelector as useReduxSelector } from 'react-redux';
import React, { useEffect, useState } from 'react';
import TextField from '@material-ui/core/TextField';
import Autocomplete, { RenderGroupParams } from '@material-ui/lab/Autocomplete';
import { CircularProgress } from '@material-ui/core';
import { ListChildComponentProps, VariableSizeList } from 'react-window';
import ListSubheader from '@material-ui/core/ListSubheader';
import Typography from '@material-ui/core/Typography';
import { useTheme } from '@material-ui/core/styles';
import useMediaQuery from '@material-ui/core/useMediaQuery';
import { commonHeader } from '../../api';
import { RootState } from '../../rootReducer';

export const useSelector: TypedUseSelectorHook<RootState> = useReduxSelector;


const TestingPage = () => {
    const dispatch = useDispatch();
    const [textField, setTextField] = useState<string>('');
    const [isLoading, setIsLoading] = useState(false);
    const [suggestionList, setSuggestionList] = useState<string[]>([]);
    const stemmedKeywordList = useSelector((state) => state.searchPageReducer.stemmedKeywordsList);


    function fetchSuggestions() {
        fetch('http://localhost:8080/suggestions', {
            ...commonHeader,
            method: 'POST',
            body: JSON.stringify({
                query: textField,
            }),
        }).then((response) => {
            if (response.status === 200) {
                response.json().then((responseJson: string[]) => {
                    setSuggestionList(responseJson);
                });
            }
        });
    }

    useEffect(() => {
        fetchSuggestions();
    }, [textField]);

    return (
        <div>
            <Autocomplete
                id='combo-box-demo'
                options={suggestionList}
                // getOptionLabel={(option) => option.year}
                style={{ width: 300 }}
                loading={isLoading}
                renderInput={
                    (params) => (
                        <TextField
                            {...params}
                            onChange={((event) => setTextField(event.target.value as string))}
                            label='Combo box'
                            variant='outlined'
                            value={textField}
                            InputProps={{
                                ...params.InputProps,
                                endAdornment: (
                                    <>
                                        {isLoading ? <CircularProgress color='inherit' size={20} /> : null}
                                        {params.InputProps.endAdornment}
                                    </>
                                ),
                            }}
                        />
                    )
                }
            />

        </div>
    );
};

export default TestingPage;
