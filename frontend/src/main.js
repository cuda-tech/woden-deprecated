import Vue from 'vue'
import axios from 'axios'
import VueAxios from 'vue-axios'
import App from './App'
import router from './router'
import ViewUI from 'view-design';
import 'view-design/dist/styles/iview.css';
import Plotly from 'plotly.js';

axios.defaults.baseURL = '/api/';

axios.interceptors.request.use(config => {
    config.headers.token = localStorage.token;
    return config

});
axios.interceptors.response.use(response => {
    if (response.data.status !== 'success') {
        ViewUI.Notice.error({
            title: '请求错误',
            desc: response.data.error
        });
        throw response.data.error
    } else {
        return response.data.data;
    }
});

Vue.use(VueAxios, axios);
Vue.use(ViewUI);
Vue.prototype.$Plotly = Plotly;

Vue.config.productionTip = false;

new Vue({
    el: '#app',
    router,
    components: {App},
    template: '<App/>'
});
