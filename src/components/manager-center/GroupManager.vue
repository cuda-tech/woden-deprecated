<style scoped>

</style>

<template>
    <Card style="margin: 30px;">

        <Table :columns="columns" :data="groups" style="margin-top: 30px;"/>

        <div style="text-align: right; margin-top: 20px">
            <Page :total="this.groupCount" show-sizer show-total
                  @on-change="changePage"
                  @on-page-size-change="changePageSize"/>
        </div>
    </Card>

</template>

<script>
    export default {
        name: "GroupManager",
        beforeMount() {
            this.changePage(1);
        },
        data() {
            return {
                columns: [
                    {
                        title: '项目组 ID',
                        key: 'id',
                        width: 150
                    },
                    {
                        title: '项目组名称',
                        key: 'name',
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
                groups: [],
                groupCount: 0,
                pageSize: 10,
                pageId: 1
            }
        },
        methods: {
            changePage(pageId) {
                this.axios.get("/group", {
                    params: {
                        page: pageId,
                        pageSize: this.pageSize
                    }
                }).then(data => {
                    this.groupCount = data.count;
                    this.groups = data.groups;
                })
            },
            changePageSize(pageSize) {
                this.pageSize = pageSize;
                this.changePage(1);
            }
        }
    }
</script>
