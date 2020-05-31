import axios from 'axios';

export default {

    /**
     * 分页查询指定文件的镜像列表
     * @param id: 文件 ID
     * @param pageId: 页面 ID, 从 1 开始计数
     * @param pageSize: 页面大小
     * @param like: 镜像名模糊匹配
     * @param callback(count, mirrors): 回调函数，返回镜像数量和镜像列表
     */
    listingMirror(id, {pageId, pageSize, like = null}, callback) {
        let params = {
            page: pageId,
            pageSize: pageSize
        };
        if (like !== null && like.trim() !== '') {
            params['like'] = like;
        }
        axios.get(`/file/${id}/mirror`, {params: params}).then(data => callback(data.count, data.mirrors));
    },

    /**
     * 创建文件镜像
     * @param id: 文件 ID
     * @param callback(mirror): 回调函数，返回创建后的镜像信息
     */
    createMirror(id, callback) {
        axios.post(`/file/${id}/mirror`).then(data => callback(data.mirror));
    },

    /**
     * 查找指定的文件镜像
     * @param id: 文件ID
     * @param mirrorId: 镜像 ID
     * @param callback(mirror): 回调函数，返回镜像信息
     */
    findMirror(id, mirrorId, callback) {
        axios.get(`/file/${id}/mirror/${mirrorId}`).then(data => callback(data.mirror));
    },

    /**
     * 删除指定的文件镜像
     * @param id: 文件 ID
     * @param mirrorId: 镜像 ID
     * @param callback(): 回调函数
     */
    deleteMirror(id, mirrorId, callback) {
        axios.delete(`/file/${id}/mirror/${mirrorId}`).then(callback());
    }

}
