import { combineReducers, Reducer } from 'redux';
import { connectRouter } from 'connected-react-router';
import { History } from 'history';
import { searchPageReducer, SearchPageStoreType } from './containers/SearchPage/reducers';

export interface rootReducerState {
    router: Reducer,
    searchPageReducer: SearchPageStoreType,
}

const rootReducer = (history: History) => combineReducers<rootReducerState>({
    router: connectRouter(history) as Reducer,
    searchPageReducer,
});


export default rootReducer;
export type RootState = rootReducerState
