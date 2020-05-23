<style>

</style>
<template>
    <div class="toolbar-buttons-group">
        <RunButton/>
        <StopButton/>
        <UnlockButton/>
        <ReleaseButton/>
    </div>
</template>
<script>
    import RunButton from './bottons/RunButton';
    import StopButton from './bottons/StopButton';
    import UnlockButton from './bottons/UnlockButton';
    import ReleaseButton from './bottons/ReleaseButton';

    export default {
        components: {
            RunButton: RunButton,
            StopButton: StopButton,
            UnlockButton: UnlockButton,
            ReleaseButton: ReleaseButton,
        },
        data() {
            return {}
        },
        methods: {
            openSchedulerDrawer() {

            }
        }

    }
</script>
