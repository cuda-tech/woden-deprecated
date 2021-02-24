<style scoped>
    .btn:hover {
        background-color: #4C5052;
    }

    .btn {
        color: #BABABA;
    }
</style>
<template>
    <Tooltip content="切换" placement="top" theme="dark" transfer :delay="1000">
        <Button custom-icon="iconfont icon-switch" type="text" class="btn"/>
    </Tooltip>

</template>

<script>
    export default {
        name: "SwitchButton"
    }
</script>


