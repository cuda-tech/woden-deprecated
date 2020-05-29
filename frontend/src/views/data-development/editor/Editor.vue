<style scoped>
</style>

<template>
    <div id="editor" :style="{height: this.height + 'px'}"></div>
</template>

<script>
    import * as monaco from 'monaco-editor';
    import EventBus from "../../../event-bus/DataDevlopmentEventBus";

    export default {
        name: 'Editor',

        props: [
            'height'
        ],

        watch: {
            height: function (newHeight, oldHeight) {
                if (newHeight < oldHeight) { // 缩小编辑框貌似会触发 monaco 的 bug，先这样处理
                    this.$nextTick(() => {
                        setTimeout(() => {
                            this.editor.layout()
                        }, 100)
                    });
                } else {
                    this.$nextTick(() => {
                        this.editor.layout()
                    });
                }
                // todo: 但是这个存在性能问题，会导致短时间内大量的调用 layout, 需要优化成一个死区形式
            }
        },

        created() {
            EventBus.$on("switch-file", this.fetchContent);
            document.addEventListener('keydown', this.shortcutHandler);
        },

        mounted() {
            this.editor = monaco.editor.create(document.getElementById('editor'), {
                value: '',
                language: 'javascript',
                theme: 'vs-dark',
                contextmenu: true
            });
            this.$nextTick(() => {
                this.editor.layout()
            });
        },

        beforeDestroy() {
            document.removeEventListener('keydown', this.shortcutHandler);
        },

        data() {
            return {
                editor: null,
                file: null
            }
        },

        methods: {
            fetchContent(file) {
                this.axios.get(`/file/${file.id}/content`).then(data => {
                    let lang = {
                        SQL: 'sql',
                        SPARK: 'shell'
                    }[file.type];
                    this.editor.setValue(data.content);
                    monaco.editor.setModelLanguage(this.editor.getModel(), lang);
                    this.file = file;
                });
            },

            shortcutHandler(event) {
                if (event.keyCode === 83 && event.ctrlKey) {
                    this.save();
                    event.preventDefault();
                    event.returnValue = false;
                    return false;
                }
            },

            save() {
                let params = new FormData();
                params.set("id", this.file.id);
                params.set("content", this.editor.getValue());
                this.axios.put(`/file/${this.file.id}`, params).then(data => {
                    this.$Message.success('已保存');
                });
            }
        }
    }
</script>


