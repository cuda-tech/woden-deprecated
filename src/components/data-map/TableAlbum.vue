<style scoped>

</style>

<template>
    <Card style="margin: 30px">
        <Row>
            <Col span="10">
                <Input v-model="search.key">
                <Select v-model="search.by" slot="prepend" style="width: 80px">
                    <Option value="user_name">用户名</Option>
                    <Option value="album_name">专辑名</Option>
                </Select>
                <Button slot="append" icon="ios-search"></Button>
                </Input>
            </Col>
            <Button type="primary" style="float: right">
                新建
            </Button>
        </Row>

        <Table :columns="columns" :data="albums" style="margin-top: 30px"></Table>
        <Page :total="100" style="float: right"/>

    </Card>
</template>

<script>
    export default {

        name: 'TableAlbum',

        data() {
            return {
                columns: [
                    {
                        title: '专辑名',
                        key: 'name'
                    },
                    {
                        title: '创建者',
                        key: 'owner'
                    },
                    {
                        title: '创建时间',
                        key: 'create_time'
                    },
                    {
                        title: '更新时间',
                        key: 'update_time'
                    },
                    {
                        title: '操作'
                    },
                ],
                albums: [],
                search: {
                    key: 'current_username',
                    by: 'user_name'
                }
            }
        },

        methods: {}
    }
</script>
