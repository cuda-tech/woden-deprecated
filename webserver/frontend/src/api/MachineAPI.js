import axios from 'axios';

export default {
    /**
     * 删除服务器
     * @param id: 服务器 ID
     * @param callback(): 回调函数
     */
    delete(id, callback) {
        axios.delete(`/machine/${id}`).then(callback());
    },

    /**
     * 更新服务器
     * @param id: 服务器 ID
     * @param hostname: 服务器 hostname
     * @param ip: 服务器 IP
     * @param callback(machine): 回调函数，请求成功后返回更新后的服务器
     */
    update(id, {hostname = null, ip = null}, callback) {
        let params = new FormData();
        params.set('hostname', hostname);
        params.set('ip', ip);
        axios.put(`/machine/${id}`, params).then(data => callback(data.machine));
    },

    /**
     * 获取服务器列表
     * @param pageId: 分页 ID，从 1 开始计数
     * @param pageSize: 分页大小
     * @param like: 服务器 hostname 模糊匹配，多个词用空格间隔，null 或空字符串会忽略
     * @param callback(count, machines): 回调函数，请求成功后会返回服务器列表以及服务器总数
     */
    listing({pageId, pageSize, like = null}, callback) {
        let params = {
            page: pageId,
            pageSize: pageSize
        };
        if (like !== null && like.trim() !== '') {
            params['like'] = like;
        }
        axios.get('/machine', {params: params}).then(data => callback(data.count, data.machines))
    },

    /**
     * 创建服务器
     * @param ip: 服务器 IP
     * @param callback(machine): 回调函数，请求成功后返回创建的服务器
     */
    create({ip}, callback) {
        let params = new FormData();
        params.set('ip', ip);
        axios.post('/machine', params).then(data => callback(data.machine));
    },

    /**
     * 通过 ID 查找服务器
     * @param id: 服务器 ID
     * @param callback(machine): 回调函数，请求成功后返回指定的服务器
     */
    find(id, callback) {
        axios.get(`/machine/${id}`).then(data => callback(data.machine))
    }
}
