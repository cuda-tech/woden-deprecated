<style scoped>

</style>
<template>
    <Row>
        <Col span="20">
            <Breadcrumb style="margin: 5px;" separator=">">
                <BreadcrumbItem style="color: #BABABA" v-for="dir in this.parent">
                    <span style="color: #BABABA">{{ dir.name }}</span>
                </BreadcrumbItem>
                <BreadcrumbItem>
                    <span style="color: #BABABA">{{ file.name }}</span>
                </BreadcrumbItem>
            </Breadcrumb>
        </Col>
        <Col span="3">
            <div style="float: right">
                <RunButton/>
                <UnlockButton/>
                <ReleaseButton/>
            </div>
        </Col>
    </Row>
</template>

<script>
    /**
     * 底部状态栏
     */
    import RunButton from './buttons/RunButton';
    import UnlockButton from './buttons/UnlockButton';
    import ReleaseButton from './buttons/ReleaseButton';
    import EventBus from "../../../event-bus/DataDevlopmentEventBus";

    export default {
        name: "StatusToolBar",
        components: {
            RunButton: RunButton,
            UnlockButton: UnlockButton,
            ReleaseButton: ReleaseButton,
        },
        created() {
            EventBus.$on("select-file", this.fetchParent);
        },

        data() {
            return {
                file: {},
                parent: []
            }
        },

        methods: {
            fetchParent(file) {
                this.axios.get(`/file/${file.id}/parent`).then(data => {
                    this.file = file;
                    this.parent = data.parent;
                });
            }
        }

    }
</script>
