<style scoped>

</style>

<template>
    <Card style="margin: 30px;">

        <Row>
            <Button type="primary" @click="createUserModal.visible=true">
                新建
                <Modal v-model="createUserModal.visible" @on-ok="createUser"
                       @on-cancel="createUserModal.visible = false">
                    <div>
                        <Icon type="md-alert" style="font-size: x-large; color: #2db7f5"/>
                        <span style="font-size: large; margin-left: 5px"> 新建用户 </span>
                    </div>
                    <Form :label-width="60" style="margin-top: 30px">
                        <FormItem label="登录名">
                            <Input v-model="createUserModal.name" :maxlength="256" show-word-limit/>
                        </FormItem>
                        <FormItem label="密码">
                            <Input v-model="createUserModal.password" type="password">
                                <Icon custom="iconfont icon-dice" slot="suffix" @click="setRandomPassword"/>
                            </Input>
                        </FormItem>
                        <FormItem label="邮箱">
                            <Input v-model="createUserModal.email" :maxlength="256" show-word-limit/>
                        </FormItem>
                        <FormItem label="项目组">
                            <GroupSelection v-model="createUserModal.groupIds" :multiple="true"/>
                        </FormItem>
                    </Form>
                </Modal>
            </Button>

            <Modal v-model="updateUserModal.visible" @on-ok="updateUser" @on-cancel="updateUserModal.visible = false">
                <div>
                    <Icon type="md-alert" style="font-size: x-large; color: #2db7f5"/>
                    <span style="font-size: large; margin-left: 5px"> 更新用户信息 </span>
                </div>
                <Form :label-width="60" style="margin-top: 30px">
                    <FormItem label="登录名">
                        <Input v-model="updateUserModal.name" :maxlength="256" show-word-limit/>
                    </FormItem>
                    <FormItem label="密码">
                        <Input v-model="updateUserModal.password" type="password" placeholder="如不需更新可不填">
                            <Icon custom="iconfont icon-dice" slot="suffix" @click="setRandomPassword"/>
                        </Input>
                    </FormItem>
                    <FormItem label="邮箱">
                        <Input v-model="updateUserModal.email" :maxlength="256" show-word-limit/>
                    </FormItem>
                    <FormItem label="项目组">
                        <GroupSelection v-model="updateUserModal.groupIds" :multiple="true"/>
                    </FormItem>
                </Form>
            </Modal>

            <Col span="6" style="float: right">
                <Input v-model="search.key" search @on-enter="() => this.changePage(1)"/>
            </Col>
        </Row>

        <Table :columns="columns" :data="users" :loading="loading" style="margin-top: 30px;"/>

        <div style="text-align: right; margin-top: 20px">
            <Page :current="pageId" :total="this.userCount" show-sizer show-total
                  @on-change="changePage"
                  @on-page-size-change="changePageSize"/>
        </div>
    </Card>

</template>

<script>
    /**
     * 用户组管理
     * 普通用户：查看当前组下的所有用户
     * 超级管理员：增删改查所有组的用户
     */
    import '../../assets/icons/iconfont.css'
    import GroupSelection from "../../components/common/selections/GroupSelection";

    export default {
        name: "UserManager",
        components: {
            GroupSelection: GroupSelection
        },
        beforeMount() {
            this.changePage(1);
        },
        data() {
            return {
                columns: [
                    {
                        title: '用户 ID',
                        key: 'id',
                        align: 'center',
                        width: 150
                    },
                    {
                        title: '用户组',
                        key: 'groupIds',
                        align: 'center',
                    },
                    {
                        title: '登录名',
                        key: 'name',
                        align: 'center',
                    },
                    {
                        title: '邮箱',
                        key: 'email',
                        align: 'center',
                    },
                    {
                        title: '创建时间',
                        key: 'createTime',
                        align: 'center',
                    },
                    {
                        title: '更新时间',
                        key: 'updateTime',
                        align: 'center',
                    },
                    {
                        title: '操作',
                        align: 'center',
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
                                            this.updateUserModal.id = row.id;
                                            this.updateUserModal.name = row.name;
                                            this.updateUserModal.email = row.email;
                                            this.updateUserModal.groupIds = row.groupIds;
                                            this.updateUserModal.password = null;
                                            this.updateUserModal.rowIndex = index;
                                            this.updateUserModal.visible = true;
                                        }
                                    }
                                }),
                                h('Poptip', {
                                        props: {
                                            title: `确认删除用户 ${row.name} ?`,
                                            confirm: true,
                                            transfer: true
                                        },
                                        on: {
                                            'on-ok': () => {
                                                this.axios.delete(`/user/${row.id}`).then(data => {
                                                    this.users.splice(index, 1);
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
                    },
                ],
                search: {
                    key: null
                },
                users: [],
                userCount: 0,
                loading: false,
                pageSize: 10,
                pageId: 1,
                createUserModal: {
                    visible: false,
                    name: null,
                    password: null,
                    email: null,
                    groupIds: [],
                },
                updateUserModal: {
                    visible: false,
                    rowIndex: null,
                    id: null,
                    name: null,
                    password: null,
                    email: null,
                    groupIds: null,
                }
            }
        },
        methods: {

            setRandomPassword() {
                let chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz123456789~!@#$%^&*-=+_';
                let pwd = '';
                for (let i = 0; i < 16; i++) {
                    pwd += chars.charAt(Math.floor(Math.random() * chars.length));
                }
                this.createUserModal.password = pwd
            },

            createUser() {
                let params = new FormData();
                params.set("name", this.createUserModal.name);
                params.set("password", this.createUserModal.password);
                params.set("groupIds", this.createUserModal.groupIds);
                params.set("email", this.createUserModal.email);

                this.axios.post("/user", params).then(data => {
                    this.userCount += 1;
                    if (this.users.length < this.pageSize) {
                        this.users.push(data.user)
                    }
                    this.createUserModal = {
                        visible: false,
                        name: null,
                        password: null,
                        email: null,
                        groupId: null,
                    }
                });
            },

            updateUser() {
                let params = new FormData();
                params.set("id", this.updateUserModal.id);
                params.set("name", this.updateUserModal.name);
                params.set("password", this.updateUserModal.password);
                params.set("groupIds", this.updateUserModal.groupIds);
                params.set("email", this.updateUserModal.email);

                this.axios.put(`/user/${this.updateUserModal.id}`, params).then(data => {
                    this.$set(this.users, this.updateUserModal.rowIndex, data.user);
                    this.updateUserModal = {
                        visible: false,
                        rowIndex: null,
                        id: null,
                        name: null,
                        password: null,
                        email: null,
                        groupIds: null,
                    }
                });

            },

            changePage(pageId) {
                this.loading = true;
                let params = {
                    page: pageId,
                    pageSize: this.pageSize
                };
                if (this.search.key !== null && this.search.key.trim() !== "") {
                    params['like'] = this.search.key;
                }
                this.axios.get("/user", {
                    params: params
                }).then(data => {
                    this.userCount = data.count;
                    this.users = data.users;
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
