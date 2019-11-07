<style scoped>

</style>
<template>
    <Tooltip content="提交" placement="top" theme="light">
        <Button class="toolbar-button" icon="md-cloud-upload" @click="modalIsOpen=true"/>
        <Modal
            v-model="modalIsOpen"
            title="提交确认"
            @on-ok="submitNode"
            @on-cancel="cancel">
            <Form :label-width="70">
                <FormItem label="备注">
                    <Input v-model="submitComment" type="textarea" placeholder="补充备注信息..."
                           :autosize="{minRows: 2,maxRows: 5}"/>
                </FormItem>
                <FormItem label="语法检查">
                    <Alert type="success">通过</Alert>
                </FormItem>
            </Form>
        </Modal>
    </Tooltip>
</template>
<script>
    export default {
        data() {
            return {
                modalIsOpen: false,
                submitComment: ''
            }
        },
        methods: {
            submitNode() {
                this.modalIsOpen = true;
            },
            cancel() {
                this.modalIsOpen = false;
            }

        }
    }
</script>
