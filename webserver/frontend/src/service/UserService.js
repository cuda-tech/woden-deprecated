import UserAPI from "@/api/UserAPI";

export default {
    /**
     * 登录
     * @param username: 登录名
     * @param password: 登录密码
     * @param callback(token): 回调函数, 请求成功后返回 token
     */
    login({username, password}, callback) {
        UserAPI.login({username, password}, callback);
    },

    /**
     * 删除用户
     * @param id: 要删除的用户 ID
     * @param callback: 删除成功后的回调函数
     */
    delete(id, callback) {
        UserAPI.delete(id, callback);
    },

    /**
     * 创建用户
     * @param name: 用户名
     * @param password: 登录密码
     * @param teamIds: 归属项目组
     * @param email: 邮箱
     * @param callback(user): 回调函数, 请求成功后返回创建的 user
     */
    create({name, password, teamIds, email}, callback) {
        UserAPI.create({name, password, teamIds, email}, callback);
    },

    /**
     * 更新 ID 为 [id] 的用户信息
     * @param id: 用户 ID
     * @param name: 用户名
     * @param password: 登录密码
     * @param teamIds: 归属项目组
     * @param email: 登录邮箱
     * @param callback(user): 回调函数，请求成功后返回更新后的用户
     */
    update(id, {name = null, password = null, teamIds = null, email = null}, callback) {
        UserAPI.update(id, {name, password, teamIds, email}, callback);
    },

    /**
     * 获取用户列表
     * @param pageId: 分页 ID，从 1 开始计数
     * @param pageSize: 分页大小
     * @param like: 用户名模糊匹配，多个词用空格间隔，null 或空字符串会忽略
     * @param callback(count, users): 回调函数，请求成功后会返回用户列表以及用户总数
     */
    listing({pageId, pageSize, like = null}, callback) {
        UserAPI.listing({pageId, pageSize, like}, callback);
    },

    /**
     * 获取当前用户信息
     * @param callback(user): 回调函数，请求成功后会返回当前用户信息
     */
    currentUser(callback) {
        UserAPI.currentUser(callback);
    },

    /**
     * 通过 id 查找用户信息
     * @param id: 需要查找的用户 ID
     * @param callback(user): 回调函数，请求成功后会返回对应 id 的用户信息
     */
    find(id, callback) {
        UserAPI.find(id, callback);
    }

}
