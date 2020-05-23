<style scoped>

</style>
<template>
    <div>
        <SearchButton/>
        <SwitchButton/>
        <FoldButton/>
    </div>
</template>

<script>
    import SearchButton from "./bottons/SearchButton";
    import SwitchButton from "./bottons/SwitchButton";
    import FoldButton from "./bottons/FoldButton";

    export default {
        components: {
            SearchButton: SearchButton,
            SwitchButton: SwitchButton,
            FoldButton: FoldButton
        },
        name: "FileManagerToolBar"
    }
</script>

