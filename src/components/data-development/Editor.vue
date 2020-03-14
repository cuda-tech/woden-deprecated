<style scoped>
</style>

<template>
    <div id="editor" :style="{height: this.height + 'px'}"></div>
</template>

<script>
    import * as monaco from 'monaco-editor'

    export default {
        props: [
            'height'
        ],

        name: 'HelloWorld',
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

        mounted() {
            this.editor = monaco.editor.create(document.getElementById('editor'), {
                value: 'console.log("Hello, world")',
                language: 'javascript',
                theme: 'vs-dark',
                contextmenu: true
            });
            this.$nextTick(() => {
                this.editor.layout()
            });
        },
        data() {
            return {
                editor: null
            }
        },
        methods: {}
    }
</script>


