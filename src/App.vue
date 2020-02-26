<style scoped>
</style>
<template>
    <div>
        <router-view></router-view>
    </div>
</template>
<script>

    export default {
        data() {
            return {}
        },
        beforeCreate() {
            let token = localStorage.token;
            if (token === null || token === undefined && token === "" || token === 'null') {
                this.$router.push({name: 'Login'})
            }
        },
        mounted() {
        },
        beforeDestroy() {
        },
        methods: {}
    }
</script>
