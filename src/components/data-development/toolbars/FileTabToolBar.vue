<style >

    .file-tab-tool-bar{
        background-color: #3C3F41;
    }

    .file-tab-tool-bar .ivu-tag{
        background-color: #3C3F41;
        margin: 0 -5px 0 0;
        border: none;
        border-radius: 0;
    }

    .file-tab-tool-bar .ivu-tag:hover{
        background-color: #27292A !important;
    }

    .file-tab-tool-bar .selected-tab {
        background-color: #4E5254 !important;
        border-bottom: 5px solid #4A88C7 !important;
    }
</style>

<template>
    <div class="file-tab-tool-bar">
        <FileTabButton class="selected-tab">
            <icon custom="iconfont icon-spark" style="color: #BC5A2B"/>
            <span style="color: #BBBBBB">文件1</span>
        </FileTabButton>
        <FileTabButton>
            <icon custom="iconfont icon-hadoop" style="color: #BD8E3E"/>
            <span style="color: #BBBBBB">文件2</span>
        </FileTabButton>
        <FileTabButton>
            <icon custom="iconfont icon-sql-file" style="color: #579242"/>
            <span style="color: #BBBBBB">文件3</span>
        </FileTabButton>
    </div>

</template>

<script>
    import FileTabButton from "./bottons/FileTabButton";

    export default {
        name: "FileTabToolBar",
        components: {
            FileTabButton: FileTabButton
        }
    }
</script>
