<style scoped>
    .btn:hover{
        background-color: #353739;
    }
</style>
<template>
    <div align="center" :style="{backgroundColor: '#3C3F41', height: height, color: '#BABABA', fontSize: 'medium'}">
        <DependencyButton class="btn"/>
        <SchedulerButton class="btn"/>
        <VersionButton class="btn"/>
    </div>
</template>

<script>
    import DependencyButton from './bottons/DependencyButton';
    import SchedulerButton from './bottons/SchedulerButton';
    import VersionButton from "./bottons/VersionButton";

    export default {
        name: "SideToolBar",
        components: {
            DependencyButton: DependencyButton,
            SchedulerButton: SchedulerButton,
            VersionButton:VersionButton
        },
        beforeMount() {
            this.height = document.documentElement.clientHeight;
            this.height -= 62; // 头部高度
            this.height += 'px';
        },
        data() {
            return {
                height: 0
            }
        }
    }
</script>

<style scoped>

</style>
