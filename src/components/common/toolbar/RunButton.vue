<style scoped>

</style>
<template>
    <Tooltip content="执行">
        <Button class="toolbar-button" icon="md-play" @click="modalIsOpen=true"/>
        <Modal
            v-model="modalIsOpen"
            title="执行参数"
            @on-ok="startJob"
            @on-cancel="cancel">
            <p>Content of dialog</p>
            <p>Content of dialog</p>
            <p>Content of dialog</p>
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
            startJob() {

            },
            cancel() {
                this.modalIsOpen = false
            }
        }
    }
</script>
