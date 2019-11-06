<style>
    .toolbar-button {
        font-size: large;
        border-radius: 0 !important;
        margin-bottom: -1px;
        margin-right: -5px;
    }

</style>
<template>
    <div class="toolbar-buttons-group">
        <RunButton/>
        <StopButton/>
        <UnlockButton/>
        <SubmitButton/>
        <ReleaseButton/>
        <SchedulerButton/>
        <DependencyButton/>
        <VersionButton/>
    </div>
</template>
<script>
    import SchedulerButton from './SchedulerButton';
    import RunButton from './RunButton';
    import StopButton from './StopButton';
    import UnlockButton from './UnlockButton';
    import SubmitButton from './SubmitButton';
    import ReleaseButton from './ReleaseButton';
    import VersionButton from './VersionButton';
    import DependencyButton from './DependencyButton';

    export default {
        components: {
            SchedulerButton: SchedulerButton,
            RunButton: RunButton,
            StopButton: StopButton,
            UnlockButton: UnlockButton,
            SubmitButton: SubmitButton,
            ReleaseButton: ReleaseButton,
            VersionButton: VersionButton,
            DependencyButton: DependencyButton
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
