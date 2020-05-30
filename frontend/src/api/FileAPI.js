import axios from 'axios';

export default {

    /**
     * 获取某个文件夹下的文件列表
     * @param parentId: 文件夹 ID
     * @param callback(count, files): 回调函数，返回节点总数和文件列表
     */
    listing(parentId, callback) {
        let params = {parentId: parentId};
        axios.get('/file', {params: params}).then(data => callback(data.count, data.files))
    },

    /**
     * 模糊搜索文件列表
     * @param like: 搜索关键词
     * @param callback(count, files): 回调函数，返回节点总数和文件列表
     */
    search(like, callback) {
        let params = {like: like};
        axios.get('/file/search', {params: params}).then(data => callback(data.count, data.files))
    },

    /**
     * 查找指定项目组的根目录
     * @param groupId: 项目组 ID
     * @param callback(file): 回调函数，返回项目组根目录
     */
    findRoot(groupId, callback) {
        let params = {groupId: groupId};
        axios.get('/file/root', {params: params}).then(data => callback(data.file))
    },

    /**
     * 查找指定文件节点的所有父节点
     * @param id: 文件 ID
     * @param callback(count, files): 回调函数，返回所有父节点数和父节点信息
     */
    listingParent(id, callback) {
        axios.get(`/file/${id}/parent`).then(data => callback(data.count, data.files))
    },

    /**
     * 查找指定文件节点当前的内容
     * @param id: 文件节点
     * @param callback(content): 回调函数, 返回文件节点内容
     */
    getContent(id, callback) {
        axios.get(`/file/${id}/content`).then(data => callback(data.content.content))
    },

    /**
     * 创建文件节点
     * @param groupId: 项目组 ID
     * @param name: 节点名称
     * @param type: 节点类型
     * @param parentId: 父节点 ID
     * @param callback(file): 回调函数, 返回创建后的文件节点
     */
    create({groupId, name, type, parentId}, callback) {
        let params = new FormData();
        params.set('groupId', groupId);
        params.set('name', name);
        params.set('type', type);
        params.set('parentId', parentId);
        axios.post('/file', params).then(data => callback(data.file));
    },

    /**
     * 更新指定 ID 的文件节点
     * @param id: 文件 ID
     * @param ownerId: 归属者 ID
     * @param name: 文件名称
     * @param content: 文件内容
     * @param parentId: 父节点ID
     * @param callback(file): 回调函数, 返回更新后的文件信息
     */
    update(id, {ownerId = null, name = null, content = null, parentId = null}, callback) {
        let params = new FormData();
        params.set('ownerId', ownerId);
        params.set('name', name);
        params.set('content', content);
        params.set('parentId', parentId);
        axios.put(`/file/${id}`, params).then(data => callback(data.file));
    },

    /**
     * 删除指定 ID 的文件节点
     * @param id: 文件 ID
     * @param callback(): 回调函数
     */
    delete(id, callback) {
        axios.delete(`/file/${id}`).then(callback());
    },

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
