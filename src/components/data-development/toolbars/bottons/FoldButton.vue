<style scoped>
    .btn:hover {
        background-color: #4C5052;
    }

    .btn {
        color: #BABABA;
    }
</style>
<template>
    <Tooltip content="收起" placement="top" theme="dark" transfer :delay="1000">
        <Button icon="md-remove" type="text" class="btn"/>
    </Tooltip>
</template>

<script>
    export default {
        name: "FoldButton"
    }
</script>

