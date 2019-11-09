<style scoped>

</style>
<template>
    <Tooltip content="停止执行" placement="top" theme="light">
        <Button class="toolbar-button" icon="md-remove-circle" @click="confirm"/>
    </Tooltip>
</template>
<script>
    export default {
        data() {
            return {}
        },
        methods: {
            confirm() {
                this.$Modal.confirm({
                    title: '确认停止执行？',
                    onOk: () => {
                        this.$Message.info('Clicked ok');
                    },
                    onCancel: () => {
                        this.$Message.info('Clicked cancel');
                    }
                });
            },
        }
    }
</script>
