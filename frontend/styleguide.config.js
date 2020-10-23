const path = require('path');
const webpackConfig = require('./build_script/webpack.base.conf');
webpackConfig.module.rules.push({
    test: /\.css$/,
    use: ['style-loader', 'css-loader']
});

module.exports = {
    title: 'woden 前端文档',
    version: '0.1.0',
    webpackConfig: require('./build_script/webpack.base.conf'),
    sections: [
        {
            name: '通用组件',
            sections: [
                {
                    name: 'Selections',
                    components: 'src/components/selections/**/[A-Z]*.vue',
                },
                {
                    name: 'Searcher',
                    components: 'src/components/searcher/**/[A-Z]*.vue',
                },
                {
                    name: '杂项',
                    components: 'src/components/misc/**/[A-Z]*.vue',
                }
            ]
        },
        {
            name: '业务组件'
        }
    ],
    usageMode: 'expand',
    exampleMode: 'hide',
    defaultExample: true,
    compilerConfig: {
        target: {ie: 11}
    },
    styleguideDir: 'dist',
    displayOrigins: true
};
