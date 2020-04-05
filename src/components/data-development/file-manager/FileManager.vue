<style>

    /*滚动条样式*/
    .file-manager .ivu-scroll-container {
        overflow-x: hidden;
    }

    .file-manager ::-webkit-scrollbar {
        width: 6px;
    }

    /*滚动槽*/
    .file-manager ::-webkit-scrollbar-track {
        border-radius: 10px;
        background-color: #3C3F41;
    }

    /*滚动块*/
    .file-manager ::-webkit-scrollbar-thumb {
        background-color: #595B5D;
        border-radius: 6px;
    }

    /*文件项*/
    .file-manager .ivu-tree-title {
        width: 100%;
    }

    .file-manager .ivu-tree-title:hover {
        background-color: #0D293E;
    }

    .ivu-tree-title-selected, .ivu-tree-title-selected:hover {
        background-color: #0D293E;
    }

    /*右键菜单*/
    .file-manager .ivu-select-dropdown {
        width: 100px;
        background-color: #3C3F41;
        border: 1px solid #515151;
        border-radius: 2px;
    }

    .file-manager .ivu-dropdown-item {
        color: #BBBBBB;
    }

    .file-manager .ivu-dropdown-item:hover {
        background-color: #4B6EAF;
    }

    /*项目组选择下拉框*/
    .file-manager .ivu-select-selection {
        background-color: #3C3F41;
        border: none;
        color: #BABABA;
    }

    .file-manager .ivu-select-item {
        color: #BABABA;
    }

    .file-manager .ivu-select-item-selected {
        background-color: #4B6EAF;
    }

    .file-manager .ivu-select-item:hover {
        background-color: #4B6EAF;
    }


</style>

<template>
    <div class="file-manager" style="background-color: #3C3F41; color: #BABABA; border-bottom: 1px solid #323232">
        <div style="border-bottom: solid 1px #323232; border-right: solid 1px #323232;">
            <GroupSelection style="width: 120px;"/>
            <FileManagerToolBar style="float: right"/>
        </div>
        <Modal v-model="createFileModal.visible" @on-ok="createFile" @on-cancel="createFileModal.visible = false">
            <div>
                <Icon type="md-alert" style="font-size: x-large; color: #2db7f5"/>
                <span style="font-size: large; margin-left: 5px">
                    新建{{ createFileModal.type === 'Dir'? '文件夹' : '工作流节点' }}
                </span>
            </div>
            <span style="margin-top: 15px">
                {{ createFileModal.type === 'Dir'? '文件夹名称': createFileModal.type + ' 节点名称' }}
            </span>
            <Input v-model="createFileModal.name" :maxlength="128" show-word-limit/>
        </Modal>

        <Modal v-model="updateFileModal.visible" @on-ok="updateFile" @on-cancel="updateFileModal.visible = false">
            <div>
                <Icon type="md-alert" style="font-size: x-large; color: #2db7f5"/>
                <span style="font-size: large; margin-left: 5px">
                    重命名{{ updateFileModal.treeNode.node.type === 'Dir'? '文件夹' : '工作流节点' }}
                </span>
            </div>
            <span style="margin-top: 15px">
                {{ updateFileModal.treeNode.node.type === 'Dir'? '文件夹新名称': createFileModal.type + ' 节点新名称' }}
            </span>
            <Input v-model="updateFileModal.name" :maxlength="128" show-word-limit/>
        </Modal>

        <Modal v-model="removeFileModal.visible" @on-ok="removeFile" @on-cancel="removeFileModal.visible = false">
            <div>
                <Icon type="md-alert" style="font-size: x-large; color: #ff9900"/>
                <span style="font-size: large; margin-left: 5px">删除确认</span>
            </div>
            <div style="margin-top: 10px; margin-left: 30px">
                确认删除<span v-if="removeFileModal.treeNode.node.type === 'DIR'">文件夹</span>
                <span v-else> {{ removeFileModal.treeNode.node.type }} 节点</span>
                <strong>{{ removeFileModal.treeNode.node.name }}</strong>
                <span v-if="removeFileModal.treeNode.node.type === 'DIR'">及其内容</span>
                ？
            </div>
        </Modal>

        <Scroll :height="height">
            <Tree :data="files" :load-data="getChild" :render="renderContent" @on-toggle-expand="cleanChildWhenFold"/>
        </Scroll>
    </div>
</template>

<script>
    import GroupSelection from "../../common/selections/GroupSelection";
    import FileManagerToolBar from "../toolbars/FileManagerToolBar";
    import FileItem from "./FileItem";
    import '../../../assets/icons/iconfont.css'
    import EventBus from "../EventBus";

    export default {

        components: {
            GroupSelection: GroupSelection,
            FileManagerToolBar: FileManagerToolBar,
            FileItem: FileItem
        },

        created() {
            this.getRoot();
        },

        mounted() {
            this.height = document.documentElement.clientHeight - 96;
        },

        data() {
            return {
                height: 600,
                files: [],
                createFileModal: {
                    visible: false,
                    parentId: null,
                    name: null,
                    type: null,
                    treeNode: {node: {}}
                },
                updateFileModal: {
                    visible: false,
                    id: null,
                    name: null,
                    treeRoot: null,
                    treeNode: {node: {}}
                },
                removeFileModal: {
                    visible: false,
                    id: null,
                    treeRoot: null,
                    treeNode: {node: {}}
                }

            }
        },
        methods: {

            /**
             * 获取当前项目组的根目录
             */
            getRoot() {
                this.axios.get('/file/root', {
                    params: {
                        groupId: 1 //todo
                    }
                }).then(data => {
                    let rootDir = data.file;
                    rootDir.children = [];
                    rootDir.loading = false;
                    this.files = [rootDir];
                })

            },

            /**
             * 获取当前文件夹节点的子节点
             * @param node: 文件夹节点
             * @param callback: 回调函数
             */
            getChild(node, callback) {
                this.axios.get('/file', {
                    params: {
                        parentId: node.id
                    }
                }).then(data => {
                    data.files.forEach(file => {
                        if (file.type === 'DIR') {
                            file.children = [];
                            file.loading = false;
                        }
                    });
                    callback(data.files);
                })
            },

            /**
             * 当折叠文件夹时，清除文件夹的子节点（为了渲染性能）
             * @param node: 文件夹节点
             */
            cleanChildWhenFold(node) {
                if (node.expand === false) {
                    node.children = []
                }
            },

            /**
             * 文件树渲染
             * @param h: 渲染函数
             * @param root: 文件树根节点
             * @param node: 当前要渲染的节点
             * @param data: 当前节点的数据
             * @returns 渲染后的节点
             */
            renderContent(h, {root, node, data}) {
                return h(FileItem, {
                    props: {
                        file: data,
                        with: 'context'
                    },
                    on: {
                        remove: () => {
                            this.removeFileModal.id = data.id;
                            this.removeFileModal.treeRoot = root;
                            this.removeFileModal.treeNode = node;
                            this.removeFileModal.visible = true;
                        },
                        rename: () => {
                            this.updateFileModal.id = data.id;
                            this.updateFileModal.name = data.name;
                            this.updateFileModal.treeNode = node;
                            this.updateFileModal.treeRoot = root;
                            this.updateFileModal.visible = true;
                        },
                        create: (type) => {
                            this.createFileModal.type = type;
                            this.createFileModal.parentId = data.id;
                            this.createFileModal.treeNode = node;
                            this.createFileModal.visible = true;
                        },
                        'db-click': () => {
                            // 因为无法拦截 iView 对两次点击事件的处理，导致文件节点的 selected 变化轨迹为
                            // false(初始) -> true(我们设置) -> true(iView 处理第一次点击) -> false(iView 处理第二次点击)
                            // 所以这里弄了个 1 毫秒的延迟，使得变化轨迹为
                            // false(初始) -> true(iView 处理第一次点击) -> false(iView 处理第二次点击) -> true(我们的延迟设置)
                            setTimeout(() => node.node.selected = true, 100);

                            if (data.type === 'DIR') { // 对于文件夹节点，执行展开 or 折叠操作
                                const dir = node.node;
                                if (dir.expand) {
                                    dir.expand = false;
                                    dir.children = []
                                } else {
                                    dir.loading = true;
                                    this.getChild(dir, files => {
                                        dir.loading = false;
                                        if (files.length) {
                                            dir.children = files;
                                            this.$nextTick(() => {
                                                if (dir.children.length) {
                                                    // 因为 expand 不是默认存在的，所以注意这里需要强制刷新一下双向绑定
                                                    this.$set(dir, 'expand', true);
                                                }
                                            });
                                        }
                                    });
                                }
                            } else {
                                EventBus.$emit("select-file", data);
                            }
                        }
                    }
                });
            },

            createFile() {
                let params = new FormData();
                params.set("groupId", '1');
                params.set("parentId", this.createFileModal.parentId);
                params.set("name", this.createFileModal.name);
                params.set("type", this.createFileModal.type);
                this.axios.post('file', params).then(data => {
                    // 如果在一个打开的文件夹中新建文件，则需要插入到文件树中
                    // 这里我们用了 quick & dirty 的方法，直接重新获取文件夹的内容
                    // 但这样会导致其它打开的文件夹关闭，todo
                    let node = this.createFileModal.treeNode.node;
                    if (node.expand) {
                        this.getChild(node, files => node.children = files);
                    }
                    this.createFileModal = {
                        visible: false,
                        parentId: null,
                        name: null,
                        type: null,
                        treeNode: {node: {}}
                    }
                })
            },

            updateFile() {
                let params = new FormData();
                params.set("name", this.updateFileModal.name);
                this.axios.put(`file/${this.updateFileModal.id}`, params).then(data => {
                    // todo: 更改名字后需要重新排序
                    this.updateFileModal.treeNode.node.name = this.updateFileModal.name;
                    this.updateFileModal = {
                        visible: false,
                        id: null,
                        name: null,
                        treeNode: {node: {}}
                    };
                });
            },

            removeFile() {
                this.axios.delete(`/file/${this.removeFileModal.id}`).then(data => {
                    let parentKey = this.removeFileModal.treeRoot.find(node => node === this.removeFileModal.treeNode).parent;
                    let parent = this.removeFileModal.treeRoot.find(node => node.nodeKey === parentKey).node;
                    let nodeIndex = parent.children.indexOf(this.removeFileModal.treeNode.node);
                    parent.children.splice(nodeIndex, 1);
                    this.removeFileModal = {
                        visible: false,
                        id: null,
                        treeRoot: null,
                        treeNode: {node: {}}
                    };
                });
            },
        }
    }
</script>
