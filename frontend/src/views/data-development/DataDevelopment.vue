<style>
    .data-development {
        overflow-y: hidden;
    }
</style>
<template>
    <Row class="data-development">
        <Col span="4">
            <FileManager/>
        </Col>

        <Col span="20">
            <div style="position: absolute;left:0;top:0;width:98%;">
                <!--                <div style=" background-color: #3C3F41; border-bottom: solid 1px #323232; ">-->
                <!--                    <Row>-->
                <!--                        <Col span="20">-->
                <!--                            <FilePathToolBar/>-->
                <!--                        </Col>-->
                <!--                        <Col span="4">-->
                <!--                            <EditorToolBar style="float: right;margin-right: 5px"/>-->
                <!--                        </Col>-->
                <!--                    </Row>-->
                <!--                </div>-->
                <FileTabToolBar/>
                <!--                <div>-->
                <Split v-model="editorRatio" mode="vertical" :style="{height: this.clientHeight + 'px'}" max="26px">
                    <div slot="top">
                        <Editor :node="1" :height="editorHeight"/>
                    </div>

                    <Row slot="trigger" style="background-color: #3C3F41; border: solid 1px #323232; margin-right: -1px">
                        <Col span="20">
                            tab
                        </Col>
                        <Col span="4">
                            <Button size="small" :icon="logIcon" type="text" style="float: right;color: #BABABA;"
                                    @click="switchLogWindow"/>
                        </Col>
                    </Row>

                    <div slot="bottom"
                         :style="{backgroundColor: '#2B2B2B', marginTop: '24px', height: logHeight + 'px', overflow: 'hidden'}">
                        logview
                    </div>
                </Split>
            </div>

            <!--右侧工具栏-->
            <div style="position: absolute;width:2%;transform: translate(4901%,0);">
                <SideToolBar/>
            </div>
        </Col>

        <!--        <FilePathToolBar style="position: fixed; bottom: 0; width: 100%; z-index: 10; border-top: 1px solid #323232; background-color: #3C3F41" />-->

        <div style="position: fixed; bottom: 0; width: 100%; z-index: 10; border-top: 1px solid #323232; background-color: #3C3F41">
            <StatusToolBar style="height: 30px"/>
            <!--            <Button type="text" size="small">??</Button>-->
            <!--            状态栏-->
        </div>


    </Row>

</template>

<script>
    import Editor from './Editor';
    import FileManager from './file-manager/FileManager';
    import EditorToolBar from './toolbars/EditorToolBar';
    import StatusToolBar from "./toolbars/StatusToolBar";
    import FileTabToolBar from "./toolbars/FileTabToolBar";
    import SideToolBar from './toolbars/SideToolBar';
    import LogWindow from './logview/LogWindow';
    import EventBus from "../../event-bus/DataDevlopmentEventBus";

    export default {
        components: {
            Editor: Editor,
            EditorToolBar: EditorToolBar,
            FileManager: FileManager,
            LogWindow: LogWindow,
            SideToolBar: SideToolBar,
            StatusToolBar: StatusToolBar,
            FileTabToolBar: FileTabToolBar
        },
        computed: {
            editorHeight: function () {
                return this.clientHeight * this.editorRatio
            },
            logHeight: function () {
                return this.clientHeight * (1 - this.editorRatio) - 24
            },
            logIcon: function () {
                return this.editorRatio < 0.95 ? 'ios-arrow-down' : 'ios-arrow-up'
            }
        },

        beforeMount() {
            this.clientHeight = document.documentElement.clientHeight;
            this.clientHeight -= 65; // 头部高度
            this.clientHeight -= 59; // 头部 & 工具栏高度
        },

        data() {
            return {
                editorRatio: 1.0,
                clientHeight: 0,
            };
        },

        methods: {
            switchLogWindow() {
                this.editorRatio = this.editorRatio < 0.95 ? 1.0 : 0.66
            }
        }
    }
</script>
