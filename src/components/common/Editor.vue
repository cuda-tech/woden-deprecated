<style scoped>

</style>

<template>
    <div class="hello" style="margin-right: 30px">
        <div id="editor" style="height: 1000px;"></div>
    </div>
</template>

<script>
    import * as monaco from 'monaco-editor'

    export default {
        name: 'HelloWorld',
        mounted() {
            monaco.editor.create(document.getElementById('editor'), {
                value: 'console.log("Hello, world")',
                language: 'javascript',
                theme: 'vs-dark'
            });
        },
        data() {
            return {
                msg: 'Welcome to Your Vue.js App'
            }
        }
    }
</script>


