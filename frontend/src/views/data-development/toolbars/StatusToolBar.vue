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
                <StopButton/>
                <UnlockButton/>
                <CreateMirrorButton/>
                <ReleaseButton/>
            </div>
        </Col>
    </Row>
</template>

<script>
    /**
     * 底部状态栏
     */
    import RunButton from './bottons/RunButton';
    import StopButton from './bottons/StopButton';
    import UnlockButton from './bottons/UnlockButton';
    import ReleaseButton from './bottons/ReleaseButton';
    import CreateMirrorButton from "./bottons/CreateMirrorButton";
    import EventBus from "../../../event-bus/DataDevlopmentEventBus";

    export default {
        name: "StatusToolBar",
        components: {
            RunButton: RunButton,
            StopButton: StopButton,
            UnlockButton: UnlockButton,
            ReleaseButton: ReleaseButton,
            CreateMirrorButton: CreateMirrorButton
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
