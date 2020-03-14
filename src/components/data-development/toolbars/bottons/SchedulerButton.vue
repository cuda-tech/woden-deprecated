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
        <Icon type="md-alarm" style="margin-bottom: 8px"/>
        <br>
        调度配置
        <Drawer title="调度配置" :closable="false" v-model="drawerIsOpen" width="40">
            <p>Some contents...</p>
            <p>Some contents...</p>
            <p>Some contents...</p>
        </Drawer>
    </div>
</template>
<script>
    export default {
        data() {
            return {
                drawerIsOpen: false,
            }
        },
        methods: {
            openDrawer() {
                this.drawerIsOpen = true;
            }
        }
    }
</script>
