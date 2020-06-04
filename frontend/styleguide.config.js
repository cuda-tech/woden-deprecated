const BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin;
const vueLoader = require('vue-loader');
const path = require('path');

const transpileDependencies = [
    'regexpu-core',
    'strip-ansi',
    'ansi-regex',
    'ansi-styles',
    'react-dev-utils',
    'chalk',
    'unicode-match-property-ecmascript',
    'unicode-match-property-value-ecmascript',
    'acorn-jsx',
    '@znck[\\\\/]prop-types'
];

module.exports = {
    title: 'datahub 前端文档',
    version: '1.0.0',
    components: 'src/**/[A-Z]*.vue',
    ignore: [
        '**/src/assets/**',
    ],
    getComponentPathLine(componentPath) {
        const componentFileName = path.basename(componentPath, '.vue');
        const name =
            componentFileName.toLowerCase() === 'index'
                ? path.basename(path.dirname(componentPath))
                : componentFileName;
        return `import ${name} from '${componentPath.replace(/^src\//, 'my-library/dist/')}'`
    },
    webpackConfig: {
        resolve: {
            extensions: ['.js', '.vue', '.json'],
            alias: {
                '~': path.join(__dirname, './test'),
                'vue$': 'vue/dist/vue.esm.js',
                '@': path.join(__dirname, './src')
            }
        },
        module: {
            rules: [
                {
                    test: /\.vue$/,
                    loader: 'vue-loader'
                },
                {
                    test: /\.js$/,
                    exclude: modulePath =>
                        (/node_modules/.test(modulePath) ||
                            /packages[\\/]vue-styleguidist[\\/]lib/.test(modulePath)) &&
                        !transpileDependencies.some(mod =>
                            new RegExp(`node_modules[\\\\/]${mod}[\\\\/]`).test(modulePath)
                        ),
                    use: {
                        loader: 'babel-loader',
                        options: {
                            sourceType: 'unambiguous',
                            presets: [
                                [
                                    '@babel/preset-env',
                                    {
                                        useBuiltIns: 'usage',
                                        corejs: 3,
                                        targets: {
                                            ie: '11'
                                        }
                                    }
                                ]
                            ],
                            comments: false
                        }
                    }
                },
                {
                    test: /\.css$/,
                    use: ['style-loader', 'css-loader']
                },
                {
                    test: /\.(woff2?|eot|ttf|otf)(\?.*)?$/,
                    loader: 'url-loader',
                    options: {
                        limit: 10000,
                        name: path.posix.join('static', 'fonts/[name].[hash:7].[ext]')
                    }
                },
                {
                    test: /\.(png|jpe?g|gif|svg)(\?.*)?$/,
                    loader: 'url-loader',
                    options: {
                        limit: 10000,
                        name: path.posix.join('static', 'img/[name].[hash:7].[ext]')
                    }
                },
            ]
        },

        plugins: [new vueLoader.VueLoaderPlugin()].concat(
            process.argv.includes('--analyze') ? [new BundleAnalyzerPlugin()] : []
        )
    },
    usageMode: 'expand',
    exampleMode: 'hide',
    defaultExample: true,
    compilerConfig: {
        target: {ie: 11}
    },
    styleguideDir: 'dist',
    displayOrigins: true
};
