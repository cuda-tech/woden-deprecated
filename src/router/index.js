import Vue from 'vue';
import Router from 'vue-router';
import DataDevelopment from './data-development';
import DataQuality from './data-quality';
import OpsCenter from './ops-center';
import DataMap from './data-map';
import ManagerCenter from './manager-center';
import Trash from './trash'

Vue.use(Router);

export default new Router({
    mode: 'history',
    routes: [
        {
            path: '/',
            name: 'Home',
            component: resolve => require(['../Layout.vue'], resolve),
            children: [
                ...DataDevelopment,
                ...DataQuality,
                ...OpsCenter,
                ...DataMap,
                ...ManagerCenter,
                ...Trash
            ]
        },
        {
            path: '/login',
            name: 'Login',
            component: resolve => require(['../components/login/Login.vue'], resolve)
        },

    ]
});
