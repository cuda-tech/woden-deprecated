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
    import SearchButton from "./buttons/SearchButton";
    import SwitchButton from "./buttons/SwitchButton";
    import FoldButton from "./buttons/FoldButton";

    export default {
        components: {
            SearchButton: SearchButton,
            SwitchButton: SwitchButton,
            FoldButton: FoldButton
        },
        name: "FileManagerToolBar"
    }
</script>

