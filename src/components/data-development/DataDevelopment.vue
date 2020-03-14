<style>

</style>
<template>
    <Row class="data-development">
        <Col span="4">
            <FileManager/>
        </Col>

        <Col span="20">
            <div style="position: absolute;left:0;top:0;width:98%;">
                <div style=" background-color: #3C3F41; border-bottom: solid 1px #323232; ">
                    <Row>
                        <Col span="20">
                            <FilePathToolBar/>
                        </Col>
                        <Col span="4">
                            <EditorToolBar style="float: right;margin-right: 5px"/>
                        </Col>
                    </Row>
                </div>
                <FileTabToolBar/>
                <!--                <div>-->
                <Split v-model="logWindowRatio" mode="vertical" :style="{height: this.clientHeight + 'px'}" max="26px">
                    <div slot="top">
                        <Editor :node="1" :height="editorHeight"/>
                    </div>

                    <Row slot="trigger"
                         style="background-color: #3C3F41; border: solid 1px #323232; ">
                        <Col span="20">
                            tab
                        </Col>
                        <Col span="4">
                            <Button size="small" :icon="logIcon" type="text" style="float: right;color: #BABABA;"
                                    @click="switchLogWindow"
                            />
                        </Col>
                    </Row>

                    <div slot="bottom"
                         :style="{backgroundColor: '#2B2B2B', marginTop: '24px', height: logHeight + 'px', overflow: 'hidden'}">
                        logview
                    </div>
                </Split>
            </div>

            <!--右侧工具栏-->
            <div style="position: absolute;width:2%;transform: translate(4900%,0);">
                <SideToolBar/>
            </div>
        </Col>


    </Row>

</template>

<script>
    import Editor from './Editor';
    import FileManager from './file-manager/FileManager';
    import EditorToolBar from './toolbars/EditorToolBar';
    import FilePathToolBar from "./toolbars/FilePathToolBar";
    import FileTabToolBar from "./toolbars/FileTabToolBar";
    import SideToolBar from './toolbars/SideToolBar';
    import LogWindow from './logview/LogWindow';

    export default {
        components: {
            Editor: Editor,
            EditorToolBar: EditorToolBar,
            FileManager: FileManager,
            LogWindow: LogWindow,
            SideToolBar: SideToolBar,
            FilePathToolBar: FilePathToolBar,
            FileTabToolBar: FileTabToolBar
        },
        computed: {
            editorHeight: function () {
                return this.clientHeight * this.logWindowRatio
            },
            logHeight: function () {
                return this.clientHeight * (1 - this.logWindowRatio) - 24
            },
            logIcon: function () {
                return this.logWindowRatio < 0.95 ? 'ios-arrow-down': 'ios-arrow-up'

            }
        },
        beforeMount() {
            this.clientHeight = document.documentElement.clientHeight;
            this.clientHeight -= 65; // 头部高度
            this.clientHeight -= 58; // 头部 & 工具栏高度
        },
        data() {
            return {
                logWindowRatio: 1.0,
                clientHeight: 0,
            };
        },
        methods: {
            switchLogWindow() {
                console.log(this.logWindowRatio)
                this.logWindowRatio = this.logWindowRatio < 0.95 ? 1.0 : 0.66
            }
        }
    }
</script>
