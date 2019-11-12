import Vue from 'vue';
import Router from 'vue-router';
import DataDevelopment from './data-development';
import TempQuery from './temp-query';
import ReleaseCenter from './release-center';

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
                ...ReleaseCenter
            ]
        },

    ]
});
