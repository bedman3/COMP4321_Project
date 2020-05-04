import React, { useState } from 'react';
import { TextField } from '@material-ui/core';
import Typography from '@material-ui/core/Typography';
import Autocomplete, { RenderInputParams } from '@material-ui/lab/Autocomplete';
import { useTheme } from '@material-ui/core/styles';
import useMediaQuery from '@material-ui/core/useMediaQuery';
import ListSubheader from '@material-ui/core/ListSubheader';
import { ListChildComponentProps, VariableSizeList } from 'react-window';


const LISTBOX_PADDING = 8; // px

function renderRow(props: ListChildComponentProps) {
    const { data, index, style } = props;
    return React.cloneElement(data[index], {
        style: {
            ...style,
            top: (style.top as number) + LISTBOX_PADDING,
        },
    });
}

const OuterElementContext = React.createContext({});

const OuterElementType = React.forwardRef<HTMLDivElement>((props, ref) => {
    const outerProps = React.useContext(OuterElementContext);
    return <div ref={ref} {...props} {...outerProps} />;
});

function useResetCache(data: any) {
    const ref = React.useRef<VariableSizeList>(null);
    React.useEffect(() => {
        if (ref.current != null) {
            ref.current.resetAfterIndex(0, true);
        }
    }, [data]);
    return ref;
}

const ListboxComponent = React.forwardRef<HTMLDivElement>((props, ref) => {
    const { children, ...other } = props;
    const itemData = React.Children.toArray(children);
    const theme = useTheme();
    const smUp = useMediaQuery(theme.breakpoints.up('sm'), { noSsr: true });
    const itemCount = itemData.length;
    const itemSize = smUp ? 36 : 48;

    const getChildSize = (child: React.ReactNode) => {
        if (React.isValidElement(child) && child.type === ListSubheader) {
            return 48;
        }

        return itemSize;
    };

    const getHeight = () => {
        if (itemCount > 8) {
            return 8 * itemSize;
        }
        return itemData.map(getChildSize).reduce((a, b) => a + b, 0);
    };

    const gridRef = useResetCache(itemCount);

    return (
        <div ref={ref}>
            <OuterElementContext.Provider value={other}>
                <VariableSizeList
                    itemData={itemData}
                    height={getHeight() + 2 * LISTBOX_PADDING}
                    width='100%'
                    ref={gridRef}
                    outerElementType={OuterElementType}
                    innerElementType='ul'
                    itemSize={(index) => getChildSize(itemData[index])}
                    overscanCount={5}
                    itemCount={itemCount}
                >
                    {renderRow}
                </VariableSizeList>
            </OuterElementContext.Provider>
        </div>
    );
});

interface SearchBarForLongOptionListProps {
    optionList: string[] | undefined,
    labelText: string,
    classes?: Partial<any>,
    appBar?: boolean,
    getValueCallback?: (str: string) => void;
}

const SearchBarForLongOptionList = (props: SearchBarForLongOptionListProps) => {
    const {
        optionList, labelText, classes, appBar, getValueCallback,
    } = props;

    const [text, setText] = useState<string | null>(null);

    const Input = (params: RenderInputParams) => {
        if (!appBar) return <TextField {...params} variant='outlined' label={labelText} />;
        return <TextField {...params} placeholder={labelText} style={{ width: '50em' }} value={text} />;
    };

    return (
        <div>
            <Autocomplete
                disabled={optionList === undefined}
                id='virtualize-demo'
                style={{ width: '12xs' }}
                disableListWrap
                ListboxComponent={ListboxComponent as React.ComponentType<React.HTMLAttributes<HTMLElement>>}
                options={optionList ?? []}
                // renderInput={(params) => <TextField {...params} variant='outlined' label={labelText} />}
                filterSelectedOptions
                renderInput={Input}
                renderOption={(option) => <Typography noWrap>{option}</Typography>}
                classes={classes}
                value={text}
                // @ts-ignore
                onChange={((event, value) => {
                    if (getValueCallback !== undefined) {
                        getValueCallback?.(value);
                        setText('');
                        if (text === '') setText(null);
                    }
                })}
            />
        </div>
    );
};


export default SearchBarForLongOptionList;
