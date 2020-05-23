<template>
    <Card style="margin: 30px">
        <Button>注册</Button>
        <Table :columns="columns" :data="functions"></Table>
        <Page :total="100" style="float: right" />
    </Card>
</template>
<script>
    export default {
        data() {
            return {
                columns: [
                    {
                        title: '函数类型',
                        key: 'type'
                    },
                    {
                        title: '函数名',
                        key: 'name'
                    },
                    {
                        title: 'jar',
                        key: 'jar'
                    },
                    {
                        title: 'class',
                        key: 'className'
                    }
                ],
                functions: [
                    {
                        type: 'UDAF',
                        name: 'sum',
                        jar: 'intern',
                        className: 'intern'
                    },
                    {
                        type: 'UDAF',
                        name: 'count',
                        jar: 'intern',
                        className: 'intern'
                    },
                    {
                        type: 'UDAF',
                        name: 'count',
                        jar: 'intern',
                        className: 'intern'
                    },
                    {
                        type: 'UDAF',
                        name: 'count',
                        jar: 'intern',
                        className: 'intern'
                    },                    {
                        type: 'UDAF',
                        name: 'count',
                        jar: 'intern',
                        className: 'intern'
                    },                    {
                        type: 'UDAF',
                        name: 'count',
                        jar: 'intern',
                        className: 'intern'
                    },                    {
                        type: 'UDAF',
                        name: 'count',
                        jar: 'intern',
                        className: 'intern'
                    },                    {
                        type: 'UDAF',
                        name: 'count',
                        jar: 'intern',
                        className: 'intern'
                    },                    {
                        type: 'UDAF',
                        name: 'count',
                        jar: 'intern',
                        className: 'intern'
                    },                    {
                        type: 'UDAF',
                        name: 'count',
                        jar: 'intern',
                        className: 'intern'
                    },

                ]

            }
        }
    }

</script>
