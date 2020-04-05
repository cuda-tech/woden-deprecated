<style scoped>

</style>
<template>
    <div>

        <div :style="style">
            <div style="background: grey">
                <Button size="small">任务1</Button>
                <Button size="small">任务2</Button>
                <Button size="small" @click="isCollapsed=!isCollapsed" style="float: right" type="text" ghost>
                    <Icon v-if="isCollapsed" type="ios-arrow-up"/>
                    <Icon v-else="isCollapsed" type="ios-arrow-down"/>
                </Button>
            </div>
            <div v-if="!isCollapsed">日志内容</div>
        </div>
    </div>
</template>
<script>
    export default {
        computed: {
            style: function () {
                return {
                    width: '100%',
                    zIndex: 1000,
                    position: 'absolute',
                    background: 'white',
                    height: this.isCollapsed ? '0px' : '300px',
                    marginTop: this.isCollapsed ? '0px' : '-260px'
                }
            }
        },
        data() {
            return {
                isCollapsed: true,

            }
        },
        methods: {}
    }
</script>
