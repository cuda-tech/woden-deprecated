<style scoped>
    .btn:hover {
        background-color: #4C5052;
    }
</style>
<template>
    <Tooltip content="发布" placement="top" theme="dark" transfer :delay="1000">
        <Button class="btn" icon="md-paper-plane" @click="modalIsOpen=true" type="text" style="color: #3592C0"/>
        <Modal
            v-model="modalIsOpen"
            title="节点参数确认"
            @on-ok="releaseNode"
            @on-cancel="cancel">
            <Form :label-width="70">
                <FormItem label="节点信息">
                    <Tag color="primary">1234</Tag>
                    <Tag color="primary">版本号</Tag>
                </FormItem>
                <FormItem label="输入表">
                    <List border size="small">
                        <ListItem>database_1.table_1</ListItem>
                        <ListItem>database_2.table_2</ListItem>
                        <ListItem>database_3.table_3</ListItem>
                    </List>
                </FormItem>
                <FormItem label="输出表">
                    <List border size="small">
                        <ListItem>database_4.table_4</ListItem>
                    </List>
                </FormItem>
                <FormItem label="调度信息">
                    <Tag color="primary">每周</Tag>
                </FormItem>
            </Form>
        </Modal>
    </Tooltip>
</template>
<script>
    export default {
        data() {
            return {
                modalIsOpen: false
            }
        },
        methods: {
            releaseNode() {
                this.modalIsOpen = false;
            },
            cancel() {
                this.modalIsOpen = false;
            }

        }
    }
</script>
