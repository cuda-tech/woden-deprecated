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
    import '../../../assets/icons/iconfont.css'

    export default {

        components: {
            GroupSelection: GroupSelection,
            FileManagerToolBar: FileManagerToolBar
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
                let file = data;
                let icon = {
                    'DIR': h('Icon', {props: {type: 'md-folder'}, style: {marginRight: '8px', color: '#87939A'}}),
                    'SQL': h('Icon', {
                        props: {custom: 'iconfont icon-sql-file'},
                        style: {marginRight: '8px', color: '#579242'}
                    }),
                    'SPARK': h('Icon', {
                        props: {custom: 'iconfont icon-spark'},
                        style: {marginRight: '8px', color: '#BC5A2B'}
                    }),
                    'MR': h('Icon', {
                        props: {custom: 'iconfont icon-hadoop'},
                        style: {marginRight: '8px', color: '#BD8E3E'}
                    }),
                }[file.type];
                let contextMenuItems = node === root[0] ? [] : [
                    h('DropdownItem', {props: {name: 'rename'}}, '重命名'),
                    h('DropdownItem', {props: {name: 'remove'}}, '删除'),
                ]; // 根节点不允许重命名 & 删除
                if (file.type === 'DIR') {
                    contextMenuItems.splice(0, 0, h('Dropdown', {props: {placement: 'right-start'}}, [
                        h('DropdownItem', ['新建节点', h('Icon', {props: {type: 'ios-arrow-forward'}})]),
                        h('DropdownMenu', {slot: 'list'}, [
                            h('DropdownItem', {props: {name: 'createSQL'}}, 'SQL'),
                            h('DropdownItem', {props: {name: 'createSpark'}}, 'Spark'),
                            h('DropdownItem', {props: {name: 'createMR'}}, 'MR'),

                        ])

                    ]));
                    contextMenuItems.splice(0, 0, h('DropdownItem', {props: {name: 'createDir'}}, '新建文件夹'))
                }

                let elementID = `file_${file.id}`;

                return h('span', {
                    style: {
                        display: 'inline-block',
                        width: '100%'
                    }
                }, [
                    h('Dropdown', {
                        props: {
                            trigger: 'contextMenu',
                            placement: 'bottom-start',
                            refs: 'dropdown',
                        },
                        style: {
                            width: '100%',
                            padding: '0'
                        },
                        on: {
                            'on-visible-change': (visible) => {
                                if (visible) { // 这里是为了修复 iView 存在多个 context menu 时不自动关闭其他的 menu 问题
                                    document.getElementById(elementID).dispatchEvent(new MouseEvent('click', {
                                        view: window,
                                        bubbles: true,
                                        cancelable: true
                                    }));
                                }
                            },
                            'on-click': (name) => {
                                if (name === 'rename') {
                                    this.updateFileModal.id = file.id;
                                    this.updateFileModal.name = file.name;
                                    this.updateFileModal.treeNode = node;
                                    this.updateFileModal.treeRoot = root;
                                    this.updateFileModal.visible = true;
                                } else if (name === 'remove') {
                                    this.removeFileModal.id = file.id;
                                    this.removeFileModal.treeRoot = root;
                                    this.removeFileModal.treeNode = node;
                                    this.removeFileModal.visible = true;
                                } else if (['createSQL', 'createSpark', 'createMR', 'createDir'].includes(name)) {
                                    this.createFileModal.type = name.substring(6);
                                    this.createFileModal.parentId = file.id;
                                    this.createFileModal.treeNode = node;
                                    this.createFileModal.visible = true;
                                } else {
                                    // nothing to do
                                }
                            }
                        }

                    }, [
                        icon,
                        h('span', {domProps: {id: elementID}, style: {color: '#BABABA'}}, file.name),
                        h('DropdownMenu', {slot: "list"}, contextMenuItems)
                    ])
                ]);
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
