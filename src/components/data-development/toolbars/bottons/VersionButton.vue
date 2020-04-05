<style scoped>
    .time {
        font-size: 14px;
        font-weight: bold;
    }

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
        <Icon type="ios-albums" style="margin-bottom: 8px"/>
        <br>
        版本控制
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
    </div>
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
