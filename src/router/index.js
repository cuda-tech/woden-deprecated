import Vue from 'vue';
import Router from 'vue-router';
import DataDevelopment from './data-development';
import TempQuery from './temp-query';
import ReleaseCenter from './release-center';
import DataQuality from './data-quality';
import OpsCenter from './ops-center';
import DataMap from './data-map';
import FunctionCenter from './function-center';
import Trash from './trash'

Vue.use(Router);

export default new Router({
    mode: 'history',
    routes: [
        {
            path: '/',
            name: 'Layout',
            component: resolve => require(['../Layout.vue'], resolve),
            children: [
                ...DataDevelopment,
                ...TempQuery,
                ...ReleaseCenter,
                ...DataQuality,
                ...OpsCenter,
                ...DataMap,
                ...FunctionCenter,
                ...Trash
            ]
        },

    ]
});
