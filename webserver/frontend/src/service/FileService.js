import FileAPI from "@/api/FileAPI";
import FileMirrorAPI from "@/api/FileMirrorAPI";

export default {
    /**
     * 获取某个文件夹下的文件列表
     * @param parentId: 文件夹 ID
     * @param callback(count, files): 回调函数，返回节点总数和文件列表
     */
    listing(parentId, callback) {
        FileAPI.listing(parentId, callback);
    },

    /**
     * 模糊搜索文件列表
     * @param like: 搜索关键词
     * @param callback(count, files): 回调函数，返回节点总数和文件列表
     */
    search(like, callback) {
        FileAPI.search(like, callback);
    },

    /**
     * 查找指定项目组的根目录
     * @param teamId: 项目组 ID
     * @param callback(file): 回调函数，返回项目组根目录
     */
    findRoot(teamId, callback) {
        FileAPI.findRoot(teamId, callback);
    },

    /**
     * 查找指定文件节点的所有父节点
     * @param id: 文件 ID
     * @param callback(count, files): 回调函数，返回所有父节点数和父节点信息
     */
    listingParent(id, callback) {
        FileAPI.listingParent(id, callback);
    },

    /**
     * 查找指定文件节点当前的内容
     * @param id: 文件节点
     * @param callback(content): 回调函数, 返回文件节点内容
     */
    getContent(id, callback) {
        FileAPI.getContent(id, callback);
    },

    /**
     * 创建文件节点
     * @param teamId: 项目组 ID
     * @param name: 节点名称
     * @param type: 节点类型
     * @param parentId: 父节点 ID
     * @param callback(file): 回调函数, 返回创建后的文件节点
     */
    create({teamId, name, type, parentId}, callback) {
        FileAPI.create({teamId, name, type, parentId}, callback);
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
        FileAPI.update(id, {ownerId, name, content, parentId}, callback);
    },

    /**
     * 删除指定 ID 的文件节点
     * @param id: 文件 ID
     * @param callback(): 回调函数
     */
    delete(id, callback) {
        FileAPI.delete(id, callback);
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
        FileMirrorAPI.listingMirror(id, {pageId, pageSize, like}, callback);
    },

    /**
     * 创建文件镜像
     * @param id: 文件 ID
     * @param callback(mirror): 回调函数，返回创建后的镜像信息
     */
    createMirror(id, callback) {
        FileMirrorAPI.createMirror(id, callback);
    },

    /**
     * 查找指定的文件镜像
     * @param id: 文件ID
     * @param mirrorId: 镜像 ID
     * @param callback(mirror): 回调函数，返回镜像信息
     */
    findMirror(id, mirrorId, callback) {
        FileMirrorAPI.findMirror(id, mirrorId, callback);
    },

    /**
     * 删除指定的文件镜像
     * @param id: 文件 ID
     * @param mirrorId: 镜像 ID
     * @param callback(): 回调函数
     */
    deleteMirror(id, mirrorId, callback) {
        FileMirrorAPI.deleteMirror(id, mirrorId, callback);
    }

}
