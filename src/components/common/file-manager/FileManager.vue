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

<!--        <ContextMenu-->
<!--            :show="contextMenuVisible"-->
<!--            class="right-menu"-->
<!--            :target="contextMenuTarget"-->
<!--            @update:show="(show) => contextMenuVisible = show"-->
<!--        >-->
<!--            <a href="javascript:;">创建子目录</a>-->
<!--            <a href="javascript:;">创建文件</a>-->
<!--            <a href="javascript:;">重命名</a>-->
<!--            <a href="javascript:;">移到回收站</a>-->
<!--        </ContextMenu>-->

        <Scroll :height="height">
            <Tree :data="data5" :render="renderContent"></Tree>
        </Scroll>
    </div>
</template>

<script>
    import {component as VueContextMenu} from '@xunlei/vue-context-menu'

    export default {
        components: {
            ContextMenu: VueContextMenu
        },
        mounted() {
            this.height = document.documentElement.clientHeight - 95;
        },
        data() {
            return {
                height: 600,
                // contextMenuVisible: false,
                // contextMenuTarget: document.body,

                data5: [
                    {
                        title: 'parent 1',
                        expand: true,
                        render: (h, {root, node, data}) => {
                            return h('span', {
                                style: {
                                    display: 'inline-block',
                                    width: '100%'
                                }
                            }, [
                                h('span', [
                                    h('Icon', {
                                        props: {
                                            type: 'ios-folder-outline'
                                        },
                                        style: {
                                            marginRight: '8px'
                                        }
                                    }),
                                    h('span', data.title)
                                ]),
                                h('span', {
                                    style: {
                                        display: 'inline-block',
                                        float: 'right',
                                        marginRight: '32px'
                                    }
                                }, [
                                    h('Button', {
                                        props: Object.assign({}, this.buttonProps, {
                                            icon: 'ios-add',
                                            type: 'primary'
                                        }),
                                        style: {
                                            width: '64px'
                                        },
                                        on: {
                                            click: () => {
                                                this.append(data)
                                            }
                                        }
                                    })
                                ])
                            ]);
                        },
                        children: [
                            {
                                title: 'child 1-1',
                                expand: true,
                                children: [
                                    {
                                        title: 'leaf 1-1-1',
                                        expand: true,
                                        children: [
                                            {
                                                title: 'leaf 1-1-1',
                                                expand: true,
                                                children: [
                                                    {
                                                        title: 'leaf 1-1-1',
                                                        expand: true,
                                                        children: [
                                                            {
                                                                title: 'leaf 1-1-1',
                                                                expand: true,
                                                                children: [
                                                                    {
                                                                        title: 'leaf 1-1-1',
                                                                        expand: true,
                                                                        children: [
                                                                            {
                                                                                title: 'leaf 1-1-1',
                                                                                expand: true,
                                                                                children: [
                                                                                    {
                                                                                        title: 'leaf 1-1-1',
                                                                                        expand: true
                                                                                    },
                                                                                    {
                                                                                        title: 'leaf 1-1-2',
                                                                                        expand: true
                                                                                    }
                                                                                ]
                                                                            },
                                                                            {
                                                                                title: 'leaf 1-1-2',
                                                                                expand: true
                                                                            }
                                                                        ]
                                                                    },
                                                                    {
                                                                        title: 'leaf 1-1-2',
                                                                        expand: true
                                                                    }
                                                                ]
                                                            },
                                                            {
                                                                title: 'leaf 1-1-2',
                                                                expand: true
                                                            }
                                                        ]
                                                    },
                                                    {
                                                        title: 'leaf 1-1-2',
                                                        expand: true
                                                    }
                                                ]
                                            },
                                            {
                                                title: 'leaf 1-1-2',
                                                expand: true
                                            }
                                        ]
                                    },
                                    {
                                        title: 'leaf 1-1-2',
                                        expand: true
                                    }
                                ]
                            },
                            {
                                title: 'child 1-2',
                                expand: true,
                                children: [
                                    {
                                        title: 'leaf 1-2-1',
                                        expand: true
                                    },
                                    {
                                        title: 'leaf 1-2-1',
                                        expand: true
                                    }
                                ]
                            }
                        ]
                    }
                ],
                buttonProps: {
                    type: 'default',
                    size: 'small',
                }
            }
        },
        methods: {
            renderContent(h, {root, node, data}) {
                return h('span', {
                    style: {
                        display: 'inline-block',
                        width: '100%'
                    }
                }, [
                    h('span', [
                        h('Icon', {
                            props: {
                                type: 'ios-paper-outline'
                            },
                            style: {
                                marginRight: '8px'
                            }
                        }),
                        h('span', data.title)
                    ]),
                ]);
            },
            append(data) {
                const children = data.children || [];
                children.push({
                    title: 'appended node',
                    expand: true
                });
                this.$set(data, 'children', children);
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
