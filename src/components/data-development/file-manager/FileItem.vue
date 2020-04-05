<style scoped>
    .filename {
        color: #BABABA;
        display: inline-block;
        width: 100%
    }
</style>

<template>
    <Dropdown v-if="this.with === 'context'" trigger="contextMenu"
              placement="bottom-start" style="width: 100%; padding: 0"
              @on-visible-change="closeOtherContextMenu"
              @on-click="clickContextMenuItem">
        <DropdownMenu slot="list">
            <DropdownItem v-if="file.type === 'DIR'" name="createDir">新建文件夹</DropdownItem>
            <Dropdown v-if="file.type === 'DIR'" placement="right-start">
                <DropdownItem>
                    新建节点
                    <Icon type="ios-arrow-forward"/>
                </DropdownItem>
                <DropdownMenu slot="list">
                    <DropdownItem name="createSQL">SQL</DropdownItem>
                    <DropdownItem name="createSpark">Spark</DropdownItem>
                    <DropdownItem name="createMR">MR</DropdownItem>
                </DropdownMenu>
            </Dropdown>
            <DropdownItem v-if="file.parentId !== undefined" name="rename">重命名</DropdownItem>
            <DropdownItem v-if="file.parentId !== undefined" name="remove">删除</DropdownItem>
        </DropdownMenu>
        <Clicker :id="'file_' + this.file.id" class="filename" @double-click="handleDoubleClick">
            <FileIcon :type="file.type" style="margin-right: 5px"/>
            {{ file.name}}
        </Clicker>

    </Dropdown>
    <span v-else class="filename" @click="handleSingleClick">
        <FileIcon :type="file.type" style="margin-right: 5px"/>
        {{ file.name}}
    </span>
</template>

<script>
    import FileIcon from "./FileIcon";
    import Clicker from "../../common/Clicker";

    export default {
        name: "FileItem",
        props: {
            /**
             * 后端返回的文件数据
             */
            file: {
                type: Object,
                required: true
            },
            /**
             * 附加功能，有效值：'context', 'dropdown', null
             */
            with: {
                type: String,
                required: false,
                default: null,
                validator(value) {
                    return ['context', 'dropdown', null].includes(value)
                }
            }
        },
        components: {
            FileIcon: FileIcon,
            Clicker: Clicker
        },
        data() {
            return {}
        },
        methods: {
            closeOtherContextMenu(visible) {
                if (visible) { // 这里是为了修复 iView 存在多个 context menu 时不自动关闭其他的 menu 问题
                    document.getElementById(`file_${this.file.id}`).dispatchEvent(new MouseEvent('click', {
                        view: window,
                        bubbles: true,
                        cancelable: true
                    }));
                }
            },

            clickContextMenuItem(name) {
                if (name === 'rename') {
                    /**
                     * 重命名文件节点
                     * @type {Event}
                     */
                    this.$emit("rename");
                } else if (name === 'remove') {
                    /**
                     * 删除文件节点
                     * @type {Event}
                     */
                    this.$emit("remove")
                } else if (['createSQL', 'createSpark', 'createMR', 'createDir'].includes(name)) {
                    /**
                     * 新建文件节点
                     * @type {Event}
                     */
                    this.$emit("create", name.substring(6));
                } else {
                    // nothing to do
                }

            },

            handleDoubleClick() {
                this.$emit("db-click");
            },

            handleSingleClick() {
                this.$emit("click");
            }

        }
    }
</script>
