<style scoped>

</style>

<template>
    <Card style="margin: 30px;">
        <Row>
            <Col span="6" style="float: right">
                <Input v-model="search.key" search @on-enter="() => this.changePage(1)"/>
            </Col>
        </Row>

        <Table :columns="columns" :data="containers" :loading="loading" style="margin-top: 30px;"/>

        <div style="text-align: right; margin-top: 20px">
            <Page :current="pageId" :total="this.containerCount" show-sizer show-total
                  @on-change="changePage"
                  @on-page-size-change="changePageSize"/>
        </div>
    </Card>

</template>

<script>
    import ContainerAPI from "../../api/ContainerAPI";

    /**
     * 容器管理
     * 普通用户：无权查看
     * 超级管理员：删除容器
     */
    export default {
        name: "ContainerManager",
        beforeMount() {
            this.changePage(1);
        },
        data() {
            return {
                columns: [
                    {
                        title: '容器 ID',
                        key: 'id',
                        align: 'center',
                        width: 150
                    },
                    {
                        title: 'hostname',
                        align: 'center',
                        key: 'hostname',
                    },
                    {
                        title: 'CPU 负载',
                        align: 'center',
                        key: 'cpuLoad',
                    },
                    {
                        title: '内存负载',
                        align: 'center',
                        key: 'memLoad'
                    },
                    {
                        title: '磁盘使用率',
                        align: 'center',
                        key: 'diskUsage'
                    },
                    {
                        title: '创建时间',
                        align: 'center',
                        key: 'createTime'
                    },
                    {
                        title: '更新时间',
                        align: 'center',
                        key: 'updateTime'
                    },
                    {
                        title: '操作',
                        align: 'center',
                        render: (h, {row, col, index}) => {
                            return h('div', [
                                h('Poptip', {
                                        props: {
                                            title: `确认删除容器 ${row.hostname} ?`,
                                            confirm: true,
                                            transfer: true
                                        },
                                        on: {
                                            'on-ok': () => {
                                                ContainerAPI.delete(row.id, () => {
                                                    this.containers.splice(index, 1);
                                                    //todo: 向下补全
                                                });
                                            }
                                        }
                                    }, [h('Button', {
                                        props: {
                                            type: 'error',
                                            size: 'small',
                                            shape: 'circle',
                                            icon: 'md-trash'
                                        },
                                        style: {
                                            marginLeft: '5px'
                                        }
                                    })]
                                ),
                            ])

                        }
                    }
                ],
                containers: [],
                containerCount: 0,
                pageSize: 10,
                pageId: 1,
                search: {
                    key: null
                },
                loading: false

            }
        },
        methods: {
            changePage(pageId) {
                this.loading = true;
                ContainerAPI.listing(pageId, this.pageSize, this.search.key, (count, containers) => {
                    this.containerCount = count;
                    this.containers = containers;
                    this.pageId = pageId;
                    this.loading = false;
                });
            },

            changePageSize(pageSize) {
                this.pageSize = pageSize;
                this.changePage(1);
            },
        }
    }
</script>
