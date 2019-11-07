<style scoped>

</style>
<template>
    <Tooltip content="血缘追踪" placement="top" theme="light">
        <Button class="toolbar-button" icon="md-git-branch" @click="drawerIsOpen=true"/>
        <Drawer title="血缘关系" :closable="false" v-model="drawerIsOpen" width="50">
            这里需要用 plotly 画图, todo

        </Drawer>
    </Tooltip>
</template>
<script>
    export default {
        data() {
            return {
                drawerIsOpen: false
            }
        },
        methods: {}
    }
</script>
