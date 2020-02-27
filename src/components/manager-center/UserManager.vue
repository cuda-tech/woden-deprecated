<style scoped>

</style>

<template>
    <Card style="margin: 30px;">

        <Table :columns="columns" :data="users" style="margin-top: 30px;"/>

        <div style="text-align: right; margin-top: 20px">
            <Page :total="this.userCount" show-sizer show-total
                  @on-change="changePage"
                  @on-page-size-change="changePageSize"/>
        </div>
    </Card>

</template>

<script>
    export default {
        name: "UserManager",
        beforeMount() {
            this.changePage(1);
        },
        data() {
            return {
                columns: [
                    {
                        title: '用户 ID',
                        key: 'id',
                        width: 150
                    },
                    {
                        title: '用户组',
                        key: 'groupIds',
                    },
                    {
                        title: '登录名',
                        key: 'name',
                    },
                    {
                        title: '邮箱',
                        key: 'email',
                    },
                    {
                        title: '创建时间',
                        key: 'createTime'
                    },
                    {
                        title: '更新时间',
                        key: 'updateTime'
                    },
                    {
                        title: '操作'
                    }
                ],
                users: [],
                userCount: 0,
                pageSize: 10,
                pageId: 1
            }
        },
        methods: {
            changePage(pageId) {
                this.axios.get("/user", {
                    params: {
                        page: pageId,
                        pageSize: this.pageSize
                    }
                }).then(data => {
                    this.userCount = data.count;
                    this.users = data.users;
                })
            },
            changePageSize(pageSize) {
                this.pageSize = pageSize;
                this.changePage(1);
            }
        }
    }
</script>
