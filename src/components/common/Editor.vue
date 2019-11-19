<style scoped>


</style>

<template>
    <div class="hello" style="margin-right: 30px">
        <div :id="'editor'+node" :style="{height: height}"></div>
    </div>
</template>

<script>
    import * as monaco from 'monaco-editor'

    export default {
        props: ['node'],
        name: 'HelloWorld',
        beforeMount(){
            this.height = document.documentElement.clientHeight;
            this.height -= 65; // 头部高度
            this.height -= 65; // 头部 & 工具栏高度
            this.height -= 20; // 日志栏高度
            this.height += 'px';
        },

        mounted() {
            monaco.editor.create(document.getElementById('editor' + this.node), {
                value: 'console.log("Hello, world")' + this.node,
                language: 'javascript',
                theme: 'vs-dark',
                contextmenu: false
            });
        },
        data() {
            return {
                height: '10px'
            }
        },
        methods:{

        }
    }
</script>


