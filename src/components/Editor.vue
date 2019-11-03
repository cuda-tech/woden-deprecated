<template>
    <div class="hello">
        <div id="editor" style="height: 600px"></div>
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

<style scoped>

</style>
