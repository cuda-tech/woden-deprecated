<style scoped>
    .btn:hover {
        background-color: #4C5052;
    }
</style>
<template>
    <Tooltip content="停止执行" placement="top" theme="dark" transfer :delay="1000">
        <Button class="btn" icon="md-remove-circle" @click="confirm" style="color:#C75450" type="text"/>
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
