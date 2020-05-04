import { TypedUseSelectorHook, useSelector as useReduxSelector } from 'react-redux';
import React from 'react';
import Typography from '@material-ui/core/Typography';
import { RootState } from '../../rootReducer';
import SearchBarForLongOptionList from '../../components/SearchBarForLongOptionList';


export const useSelector: TypedUseSelectorHook<RootState> = useReduxSelector;


const StemmedKeywordPage = () => {
    const stemmedKeywordList = useSelector((state) => state.searchPageReducer.stemmedKeywordsList);
    return (
        <div>
            <Typography>Search stemmed keywords here</Typography>
            <SearchBarForLongOptionList
                optionList={stemmedKeywordList as string[]}
                labelText='Stemmed Keywords'
                appBar={false}
            />
            <Typography>Here is a list of stemmed keywords available</Typography>
            <ul>
                {stemmedKeywordList?.map((keyword) => <li>{keyword}</li>)}
            </ul>
        </div>
    );
};

export default StemmedKeywordPage;
