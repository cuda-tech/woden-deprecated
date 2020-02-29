<style scoped>
    .right-menu {
        border: 1px solid #eee;
        box-shadow: 0 0.5em 1em 0 rgba(0, 0, 0, .1);
        border-radius: 1px;
        position: fixed;
        background: #fff;
        z-index: 999;
        display: none;
    }

    .right-menu a {
        padding: 2px;
        width: 75px;
        height: 28px;
        line-height: 28px;
        text-align: center;
        display: block;
        color: #1a1a1a;
    }

    .right-menu a:hover {
        background: #515a6e;
        color: #fff;
    }
</style>

<template>
    <div>
        <Select placeholder="搜索文件" filterable remote> </Select>

        <Scroll :height="height">
            <Tree :data="files"
                  :load-data="getChild"
                  :render="renderContent"/>
        </Scroll>
    </div>
</template>

<script>
    import '../../../assets/icons/iconfont.css'

    export default {


        created() {
            this.getRoot();
        },

        mounted() {
            this.height = document.documentElement.clientHeight - 95;
        },

        data() {
            return {
                height: 600,
                // contextMenuVisible: false,
                // contextMenuTarget: document.body,

                files: [],

            }
        },
        methods: {

            getRoot() {
                this.axios.get('/file/root', {
                    params: {
                        groupId: 1
                    }
                }).then(data => {
                    let rootDir = data.file;
                    rootDir.children = [];
                    rootDir.loading = false;
                    this.files = [rootDir];
                })

            },

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

            renderContent(h, {root, node, data}) {
                let file = data;
                let icon = {
                    'DIR': h('Icon', {props: {type: 'md-folder'}, style: {marginRight: '8px', color: '#0099FF'}}),
                    'SQL': h('Icon', {
                        props: {custom: 'iconfont icon-sql-file'},
                        style: {marginRight: '8px', color: 'green'}
                    }),
                    'DDL': h('Icon', {
                        props: {custom: 'iconfont icon-ddl-file'},
                        style: {marginRight: '8px', color: '#FF9933'}
                    }),
                }[file.type];
                let contextMenuItems = [
                    h('DropdownItem', '重命名'),
                    h('DropdownItem', '删除'),
                ];
                if (file.type === 'DIR') {
                    contextMenuItems.splice(0, 0, h('DropdownItem', '新建文件'));
                    contextMenuItems.splice(0, 0, h('DropdownItem', '新建文件夹'))
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
                            placement: 'right-start',
                            refs: 'dropdown',
                        },
                        on: {

                            'on-visible-change': (visible) => {
                                if (visible) { // 这里是为了修复 iView 多个 context menu 存在时不会自动关闭其他的 menu 问题
                                    document.getElementById(elementID).dispatchEvent(new MouseEvent('click', {
                                        view: window,
                                        bubbles: true,
                                        cancelable: true
                                    }));
                                }
                            }

                        }

                    }, [
                        icon,
                        h('span', {
                            props: {id: 'yyyy'}, domProps: {
                                id: elementID
                            }
                        }, file.name),
                        h('DropdownMenu', {slot: "list"}, contextMenuItems)
                    ])
                ]);
            },

            remove(root, node, data) {
                const parentKey = root.find(el => el === node).parent;
                const parent = root.find(el => el.nodeKey === parentKey).node;
                const index = parent.children.indexOf(data);
                parent.children.splice(index, 1);
            }
        }
    }
</script>
