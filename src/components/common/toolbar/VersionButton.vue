<style scoped>
    .time {
        font-size: 14px;
        font-weight: bold;
    }



</style>
<template>
    <Tooltip content="历史版本" placement="top" theme="light">
        <Button class="toolbar-button" icon="ios-albums" @click="drawerIsOpen=true"/>
        <Drawer title="历史版本" :closable="false" v-model="drawerIsOpen" width="20">
            <Timeline>
                <TimelineItem>
                    <Icon type="ios-redo" slot="dot"/>
                    <p class="time">
                        <Button type="primary" size="small" @click="rollback">节点ID</Button>
                        2019-11-07 23:42:01
                    </p>
                    <p class="content">提交备注</p>
                </TimelineItem>
                <TimelineItem>
                    <Icon type="ios-redo" slot="dot"/>
                    <p class="time">
                        <Button type="primary" size="small" @click="rollback">节点ID</Button>
                        2019-11-07 23:42:01
                    </p>
                    <p class="content">提交备注</p>
                </TimelineItem>
                <TimelineItem>
                    <Icon type="ios-redo" slot="dot"/>
                    <p class="time">
                        <Button type="primary" size="small" @click="rollback">节点ID</Button>
                        2019-11-07 23:42:01
                    </p>
                    <p class="content">提交备注</p>
                </TimelineItem>
            </Timeline>
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
        methods: {
            rollback() {
                alert(1)
            }
        }
    }
</script>
