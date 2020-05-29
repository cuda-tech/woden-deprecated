<style scoped>
    .btn:hover {
        background-color: #4C5052;
    }

    .btn {
        color: #BABABA;
    }
</style>

<template>
    <Tooltip content="搜索" placement="top" theme="dark" transfer :delay="1000">
        <Button class="btn" icon="md-search" type="text"/>
    </Tooltip>
</template>

<script>
    export default {
        name: "SearchButton"
    }
</script>


