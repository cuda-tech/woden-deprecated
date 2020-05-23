<style>

    .file-tab-tool-bar {
        background-color: #3C3F41;
        height: 32px;
    }

    .file-tab-tool-bar .ivu-tag {
        background-color: #3C3F41;
        margin: 0 -5px 0 0;
        border: none;
        border-radius: 0;
        color: #BABABA;
        height: 31px;
    }

    .file-tab-tool-bar .ivu-tag:hover {
        background-color: #27292A !important;
    }

    .file-tab-tool-bar .selected {
        background-color: #4E5254 !important;
        border-bottom: 5px solid #4A88C7 !important;
    }

    .file-tab-tool-bar .ivu-tag-text {
        color: #BABABA;
    }

    /*iView 在 check 状态会改变关闭按钮的颜色, 这里强制一下*/
    .ivu-tag:not(.ivu-tag-border):not(.ivu-tag-dot):not(.ivu-tag-checked) .ivu-icon-ios-close {
        color: #BABABA !important;
    }

    .file-tab-tool-bar .ivu-icon-ios-close {
        color: #BABABA !important;

    }
</style>

<template>
    <div class="file-tab-tool-bar">
        <Tag closable size="medium" v-for="(file, idx) in files" :class="idx===selectedIndex?'selected':'unselected'"
             @on-change="selectedIndex=idx" checkable> <!--iView 拦截了 click 事件，所以这里用 check 事件伪装一下-->
            <FileIcon :type="file.type" style="margin-right: 5px"/>
            {{ file.name}}
        </Tag>
    </div>
</template>

<script>
    import FileIcon from "../file-manager/FileIcon";
    import EventBus from "../../../event-bus/DataDevlopmentEventBus";

    export default {
        name: "FileTabToolBar",
        props: {
            files: {
                type: Array,
                required: false,
                default: function () {
                    return []
                }
            }
        },
        watch: {
            selectedIndex: function (idx) {
                EventBus.$emit("switch-file", this.files[idx]);
            }
        },

        components: {
            FileIcon: FileIcon,
        },

        created() {
            EventBus.$on("select-file", this.switchOrOpenTab);
        },



        data() {
            return {
                selectedIndex: null
            }
        },

        methods: {
            switchOrOpenTab(file) {
                for (let i = 0; i < this.files.length; i++) {
                    if (this.files[i].id === file.id) {
                        this.selectedIndex = i;
                        return
                    }
                }
                this.files.push(JSON.parse(JSON.stringify(file))); // deep copy
                this.selectedIndex = this.files.length - 1;
            }
        }
    }
</script>
