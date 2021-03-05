<style scoped>

</style>

<template>
    <Card style="margin: 30px;">
        <Row>
            <Button type="primary" @click="createTeamModal.visible=true">
                新建
                <Modal v-model="createTeamModal.visible" @on-ok="createTeam"
                       @on-cancel="createTeamModal.visible = false">
                    <div>
                        <Icon type="md-alert" style="font-size: x-large; color: #2db7f5"/>
                        <span style="font-size: large; margin-left: 5px"> 新建项目组 </span>
                    </div>
                    <Form :label-width="100" style="margin-top: 30px">
                        <FormItem label="项目组名称">
                            <Input v-model="createTeamModal.name" :maxlength="256" show-word-limit/>
                        </FormItem>
                    </Form>
                </Modal>
            </Button>


            <Modal v-model="updateTeamModal.visible" @on-ok="updateTeam"
                   @on-cancel="updateTeamModal.visible = false">
                <div>
                    <Icon type="md-alert" style="font-size: x-large; color: #2db7f5"/>
                    <span style="font-size: large; margin-left: 5px"> 更新项目组信息 </span>
                </div>
                <Form :label-width="60" style="margin-top: 30px">
                    <FormItem label="项目组名称">
                        <Input v-model="updateTeamModal.name" :maxlength="256" show-word-limit/>
                    </FormItem>
                </Form>
            </Modal>

            <Col span="6" style="float: right">
                <Input v-model="search.key" search @on-enter="() => this.changePage(1)"/>
            </Col>
        </Row>

        <Table :columns="columns" :data="teams" :loading="loading" style="margin-top: 30px;"/>

        <div style="text-align: right; margin-top: 20px">
            <Page :total="this.teamCount" :current="pageId" show-sizer show-total
                  @on-change="changePage"
                  @on-page-size-change="changePageSize"/>
        </div>
    </Card>

</template>

<script>
    import TeamAPI from "../../api/TeamAPI";

    /**
     * 项目组管理
     * 普通用户：查看项目组 & 申请加入项目组
     * 超级管理员：增删改查项目组
     */
    export default {
        name: "TeamManager",
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
                                            this.updateTeamModal.id = row.id;
                                            this.updateTeamModal.name = row.name;
                                            this.updateTeamModal.rowIndex = index;
                                            this.updateTeamModal.visible = true;
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
                                                this.axios.delete(`/team/${row.id}`).then(data => {
                                                    this.teams.splice(index, 1);
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
                teams: [],
                teamCount: 0,
                pageSize: 10,
                pageId: 1,
                createTeamModal: {
                    name: null,
                    visible: false
                },
                updateTeamModal: {
                    id: null,
                    name: null,
                    rowIndex: null,
                    visible: false
                }
            }
        },
        methods: {
            createTeam() {
                let params = new FormData();
                params.set("name", this.createTeamModal.name);
                this.axios.post("/team", params).then(data => {
                    this.teamCount += 1;
                    if (this.teams.length < this.pageSize) {
                        this.teams.push(data.team)
                    }
                    this.createTeamModal = {
                        name: null,
                        visible: false
                    }
                });
            },

            updateTeam() {
                let params = new FormData();
                params.set("id", this.updateTeamModal.id);
                params.set("name", this.updateTeamModal.name);
                this.axios.put(`/team/${this.updateTeamModal.id}`, params).then(data => {
                    this.$set(this.teams, this.updateTeamModal.rowIndex, data.team);
                    this.updateTeamModal = {
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
                this.axios.get("/team", {params: params}).then(data => {
                    this.teamCount = data.count;
                    this.teams = data.teams;
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
