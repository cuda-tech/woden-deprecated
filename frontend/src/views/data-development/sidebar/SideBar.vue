<style scoped>
    .btn:hover {
        background-color: #353739;
    }
</style>
<template>
    <div align="center" :style="{backgroundColor: '#3C3F41', height: height, color: '#BABABA', fontSize: 'medium'}">
        <DependencyButton class="btn"/>
        <SchedulerButton class="btn"/>
        <MirrorButton class="btn"/>
    </div>
</template>

<script>
    /**
     * 右边导航栏
     */
    import DependencyButton from './buttons/DependencyButton';
    import SchedulerButton from './buttons/SchedulerButton';
    import MirrorButton from "./buttons/MirrorButton";

    export default {
        name: "SideToolBar",
        components: {
            DependencyButton: DependencyButton,
            SchedulerButton: SchedulerButton,
            MirrorButton: MirrorButton
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
