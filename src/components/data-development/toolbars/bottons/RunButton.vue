<style scoped>
    .btn:hover{
        background-color: #4C5052;
    }
</style>
<template>
    <Tooltip content="执行" placement="top" theme="dark" transfer :delay="1000">
        <Button class="btn" icon="md-play" @click="modalIsOpen=true" type="text" style="color: #499C54;" />
        <Modal
            v-model="modalIsOpen"
            title="执行参数"
            @on-ok="startJob"
            @on-cancel="cancel">
            <Form :label-width="80">
                <FormItem label="$bizdate">
                    <DatePicker v-model="bizdate" type="date"
                                format="yyyyMMdd"
                                placeholder="业务日期" style="width: 200px"/>
                </FormItem>
            </Form>
        </Modal>
    </Tooltip>
</template>
<script>
    export default {
        created() {
            this.bizdate.setTime(this.bizdate.getTime() - 24 * 60 * 60 * 1000);
        },
        data() {
            return {
                modalIsOpen: false,
                bizdate: new Date()
            }
        },
        methods: {
            startJob() {

            },
            cancel() {
                this.modalIsOpen = false
            }
        }
    }
</script>
