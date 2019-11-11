<style scoped>

</style>
<template>
    <Tooltip content="调度配置" placement="top" theme="light">
        <Button class="toolbar-button" icon="md-alarm" @click="openDrawer"/>
        <Drawer title="调度配置" :closable="false" v-model="drawerIsOpen" width="40">
            <p>Some contents...</p>
            <p>Some contents...</p>
            <p>Some contents...</p>
        </Drawer>
    </Tooltip>
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
