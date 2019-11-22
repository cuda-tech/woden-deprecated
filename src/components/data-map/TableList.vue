<style scoped>

</style>

<template>
    <Card style="margin: 30px">

        <Row>
            <Col span="10">
                <Input v-model="search.key">
                    <Select v-model="search.by" slot="prepend" style="width: 80px">
                        <Option value="user">用户</Option>
                        <Option value="project">项目</Option>
                        <Option value="table">表名</Option>
                        <Option value="column">字段</Option>
                    </Select>
                    <Button slot="append" icon="ios-search"></Button>
                </Input>
            </Col>
            <Col span="12" style="margin-left: 20px; margin-top: 5px">
                <Checkbox v-model="search.includeTemp">包含临时表</Checkbox>
                <Checkbox v-model="search.updateLastMonth">最近一月有更新</Checkbox>
            </Col>
        </Row>


        <Table :columns="columns" :data="tables" style="margin-top: 30px"></Table>
        <Page :total="100" style="float: right"/>
    </Card>
</template>

<script>
    export default {
        name: 'TableList',
        data() {
            return {
                columns: [
                    {
                        title: '项目空间',
                        key: 'project_name'
                    },
                    {
                        title: '表名',
                        key: 'table_name'
                    },
                    {
                        title: 'owner',
                        key: 'owner',
                    },
                    {
                        title: '最近一次更新时间',
                        key: 'last_update_time'
                    },
                    {
                        title: '操作'
                    }
                ],
                tables: [
                    {
                        project_name: 'project1',
                        table_name: 'table1',
                        owner: 'root',
                        last_update_time: '2019-11-11 00:00:00'
                    },
                    {
                        project_name: 'project1',
                        table_name: 'table1',
                        owner: 'root',
                        last_update_time: '2019-11-11 00:00:00'
                    },
                    {
                        project_name: 'project1',
                        table_name: 'table1',
                        owner: 'root',
                        last_update_time: '2019-11-11 00:00:00'
                    },
                    {
                        project_name: 'project1',
                        table_name: 'table1',
                        owner: 'root',
                        last_update_time: '2019-11-11 00:00:00'
                    },
                    {
                        project_name: 'project1',
                        table_name: 'table1',
                        owner: 'root',
                        last_update_time: '2019-11-11 00:00:00'
                    },
                    {
                        project_name: 'project1',
                        table_name: 'table1',
                        owner: 'root',
                        last_update_time: '2019-11-11 00:00:00'
                    },
                ],
                search:{
                    by: 'user',
                    key: 'current_user',
                    includeTemp: false,
                    updateLastMonth: false
                },
            }
        }
    }

</script>
