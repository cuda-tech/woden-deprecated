<style>
    .ivu-tabs-tab {
        margin-right: -1px !important;
        border-radius: 0 !important;
    }

    .ivu-select-selection {
        border-radius: 0 !important;
        margin-right: -1px;
    }


</style>
<template>
    <Row>
        <Col span="4">
            <FileManager/>
        </Col>

        <Col span="20">
            <ToolBar/>
            <hr style="margin-bottom: -1px;background: #d7dde4;border:none;height: 1px">
            <Tabs type="card" closable :animated="false">
                <TabPane label="文件一" v-if="tab0">
                    <Editor style="margin-top: -16px" :node="1"/>
                </TabPane>
                <TabPane label="文件二" v-if="tab1">
                    <Editor style="margin-top: -16px" :node="2"/>
                </TabPane>
                <TabPane label="文件三" v-if="tab2">
                    <Editor style="margin-top: -16px" :node="3"/>
                </TabPane>
            </Tabs>
            <LogWindow/>

        </Col>

    </Row>

</template>

<script>
    import Editor from '../common/Editor';
    import FileManager from '../common/file-manager/FileManager';
    import ToolBar from '../common/toolbar/ToolBar';
    import LogWindow from '../common/log-window/LogWindow';

    export default {
        components: {
            Editor: Editor,
            ToolBar: ToolBar,
            FileManager: FileManager,
            LogWindow: LogWindow
        },
        data() {
            return {
                tab0: true,
                tab1: true,
                tab2: true
            };
        },
    }
</script>
