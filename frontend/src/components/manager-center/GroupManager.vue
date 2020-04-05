<style scoped>

</style>

<template>
    <Card style="margin: 30px;">
        <Row>
            <Button type="primary" @click="createGroupModal.visible=true">
                新建
                <Modal v-model="createGroupModal.visible" @on-ok="createGroup"
                       @on-cancel="createGroupModal.visible = false">
                    <div>
                        <Icon type="md-alert" style="font-size: x-large; color: #2db7f5"/>
                        <span style="font-size: large; margin-left: 5px"> 新建项目组 </span>
                    </div>
                    <Form :label-width="100" style="margin-top: 30px">
                        <FormItem label="项目组名称">
                            <Input v-model="createGroupModal.name" :maxlength="256" show-word-limit/>
                        </FormItem>
                    </Form>
                </Modal>
            </Button>


            <Modal v-model="updateGroupModal.visible" @on-ok="updateGroup"
                   @on-cancel="updateGroupModal.visible = false">
                <div>
                    <Icon type="md-alert" style="font-size: x-large; color: #2db7f5"/>
                    <span style="font-size: large; margin-left: 5px"> 更新项目组信息 </span>
                </div>
                <Form :label-width="60" style="margin-top: 30px">
                    <FormItem label="项目组名称">
                        <Input v-model="updateGroupModal.name" :maxlength="256" show-word-limit/>
                    </FormItem>
                </Form>
            </Modal>

            <Col span="6" style="float: right">
                <Input v-model="search.key" search @on-enter="() => this.changePage(1)"/>
            </Col>
        </Row>

        <Table :columns="columns" :data="groups" :loading="loading" style="margin-top: 30px;"/>

        <div style="text-align: right; margin-top: 20px">
            <Page :total="this.groupCount" :current="pageId" show-sizer show-total
                  @on-change="changePage"
                  @on-page-size-change="changePageSize"/>
        </div>
    </Card>

</template>

<script>
    /**
     * 项目组管理
     * 普通用户：查看项目组 & 申请加入项目组
     * 超级管理员：增删改查项目组
     */
    export default {
        name: "GroupManager",
        beforeMount() {
            this.changePage(1);
        },
        data() {
            return {
                loading: false,
                columns: [
                    {
                        title: '项目组 ID',
                        key: 'id',
                        width: 150,
                        align: 'center'
                    },
                    {
                        title: '项目组名称',
                        key: 'name',
                        align: 'center'
                    },
                    {
                        title: '创建时间',
                        key: 'createTime',
                        align: 'center'
                    },
                    {
                        title: '更新时间',
                        key: 'updateTime',
                        align: 'center'
                    },
                    {
                        title: '操作',
                        align: 'center',
                        width: 150,
                        render: (h, {row, col, index}) => {
                            return h('div', [
                                h('Button', {
                                    props: {
                                        type: 'info',
                                        size: 'small',
                                        shape: 'circle',
                                        icon: 'md-create'
                                    },
                                    on: {
                                        'click': () => {
                                            this.updateGroupModal.id = row.id;
                                            this.updateGroupModal.name = row.name;
                                            this.updateGroupModal.rowIndex = index;
                                            this.updateGroupModal.visible = true;
                                        }
                                    }
                                }),
                                h('Poptip', {
                                        props: {
                                            title: `确认删除项目组 ${row.name} ?`,
                                            confirm: true,
                                            transfer: true
                                        },
                                        on: {
                                            'on-ok': () => {
                                                this.axios.delete(`/group/${row.id}`).then(data => {
                                                    this.groups.splice(index, 1);
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
                search: {
                    key: null
                },
                groups: [],
                groupCount: 0,
                pageSize: 10,
                pageId: 1,
                createGroupModal: {
                    name: null,
                    visible: false
                },
                updateGroupModal: {
                    id: null,
                    name: null,
                    rowIndex: null,
                    visible: false
                }
            }
        },
        methods: {
            createGroup() {
                let params = new FormData();
                params.set("name", this.createGroupModal.name);
                this.axios.post("/group", params).then(data => {
                    this.groupCount += 1;
                    if (this.groups.length < this.pageSize) {
                        this.groups.push(data.group)
                    }
                    this.createGroupModal = {
                        name: null,
                        visible: false
                    }
                });
            },

            updateGroup() {
                let params = new FormData();
                params.set("id", this.updateGroupModal.id);
                params.set("name", this.updateGroupModal.name);
                this.axios.put(`/group/${this.updateGroupModal.id}`, params).then(data => {
                    this.$set(this.groups, this.updateGroupModal.rowIndex, data.group);
                    this.updateGroupModal = {
                        id: null,
                        name: null,
                        rowIndex: null,
                        visible: false
                    }
                });
            },

            changePage(pageId) {
                this.loading = true;
                let params = {
                    page: pageId,
                    pageSize: this.pageSize
                };
                if (this.search.key !== null && this.search.key.trim() !== '') {
                    params.like = this.search.key;
                }
                this.axios.get("/group", {params: params}).then(data => {
                    this.groupCount = data.count;
                    this.groups = data.groups;
                    this.pageId = pageId;
                    this.loading = false;
                }).catch(err => {
                    this.loading = false;
                })
            },

            changePageSize(pageSize) {
                this.pageSize = pageSize;
                this.changePage(1);
            }
        }
    }
</script>
