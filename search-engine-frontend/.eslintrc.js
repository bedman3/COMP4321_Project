module.exports = {
    env: {
        browser: true,
        es6: true,
    },
    extends: [
        'airbnb',
        // 'plugin:@typescript-eslint/recommended'
    ],
    globals: {
        Atomics: 'readonly',
        SharedArrayBuffer: 'readonly',
    },
    parserOptions: {
        ecmaFeatures: {
            // jsx: true,
        },
        ecmaVersion: 2018,
        sourceType: 'module',
    },
    plugins: [
        'react',
        '@typescript-eslint',
    ],
    parser: '@typescript-eslint/parser',
    rules: {
        'camelcase': [0, 4],
        'indent': [2, 4],
        'import/extensions': [
            'warn',
            'ignorePackages',
            {
                'js': 'never',
                'jsx': 'never',
                'ts': 'never',
                'tsx': 'never'
            }
        ],
        'jsx-a11y/anchor-is-valid': 'off',
        'jsx-quotes': [2, 'prefer-single'],
        'jsx-a11y/label-has-associated-control': 'off',
        'react/jsx-indent': [2, 4],
        'react/jsx-filename-extension': [1, { 'extensions': ['.js', '.jsx', '.ts', '.tsx'] }],
        'react/jsx-one-expression-per-line': 'off',
        'react/jsx-indent-props': [2 ,4],
        'react/no-array-index-key': 'off',
        'react/prefer-stateless-function': 'off',
        'max-len': [1, {
            'code': 120,
        }],
        'no-console': 'off',
        'no-nested-ternary': 'off',
        'no-plusplus': 'off',
        'no-unused-expressions': 'off',
        'object-curly-spacing': [2, 'always', {
            'objectsInObjects': false,
        }],
    },
    overrides: [
        {
            files: ['*.ts', '*.tsx'],
            rules: {
                '@typescript-eslint/no-unused-vars': [2, { args: 'none' }]
            }
        }
    ],
    settings: {
        'import/resolver': {
            'node': {
                'extensions': ['.js', '.jsx', '.ts', '.tsx']
            }
        }
    },
};
