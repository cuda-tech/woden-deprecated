import ContainerAPI from "@/api/ContainerAPI";

export default {
    /**
     * 删除容器
     * @param id: 容器 ID
     * @param callback(): 回调函数
     */
    delete(id, callback) {
        ContainerAPI.delete(id, callback);
    },

    /**
     * 获取容器列表
     * @param pageId: 分页 ID，从 1 开始计数
     * @param pageSize: 分页大小
     * @param like: 容器 hostname 模糊匹配，多个词用空格间隔，null 或空字符串会忽略
     * @param callback(count, containers): 回调函数，请求成功后会返回容器列表以及容器总数
     */
    listing({pageId, pageSize, like = null}, callback) {
        ContainerAPI.listing({pageId, pageSize, like}, callback);
    },

    /**
     * 通过 ID 查找容器
     * @param id: 容器 ID
     * @param callback(container): 回调函数，请求成功后返回指定的容器
     */
    find(id, callback) {
        ContainerAPI.find(id, callback);
    }
}
