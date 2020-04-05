<style scoped>
    .btn:hover {
        background-color: #4C5052;
    }
</style>
<template>
    <Tooltip content="解锁编辑" placement="top" theme="dark" transfer :delay="1000">
        <Button class="btn" icon="md-unlock" @click="unlock" type="text" style="color: #BD8E3E"/>
    </Tooltip>
</template>
<script>
    export default {
        data() {
            return {
            }
        },
        methods: {
            unlock(){

            }
        }
    }
</script>
