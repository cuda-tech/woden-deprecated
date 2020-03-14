<style scoped>
    .btn {
        width: 100%;
        margin: 0 auto;
        line-height: 100%;
        padding-top: 20px;
        padding-bottom: 20px;
    }
</style>
<template>
    <div class="btn" @click="drawerIsOpen=true">
        <Icon type="md-git-branch" style="margin-bottom: 8px"/>
        <br>
        血缘依赖
        <Drawer title="血缘依赖" :closable="false" v-model="drawerIsOpen" width="50">
            这里需要用 plotly 画图, todo
        </Drawer>
    </div>
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
