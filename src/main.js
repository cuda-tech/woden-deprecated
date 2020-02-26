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
        console.log("error")
    } else {
        return response.data.data;
    }
}, error => {
    console.log(error)
    return Promise.reject(error);
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
