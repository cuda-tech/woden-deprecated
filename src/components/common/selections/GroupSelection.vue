<style scoped>

</style>

<template>
    <Select style="width:120px;margin-top: -10px;margin-left: 20px" v-model="selectedGroup">
        <Option v-for="group in groups" :value="group.id">
            {{ group.name }}
        </Option>
    </Select>
</template>

<script>
    export default {
        name: "GroupSelection",
        beforeMount() {
            this.axios.get("/group").then(data => {
                this.groupsCount = data.count;
                this.groups = data.groups;
                if (this.groups.length > 0) {
                    this.selectedGroup = this.groups[0].id
                }
            })
        },
        data() {
            return {
                groupsCount: 0,
                groups: [],
                selectedGroup: null
            }
        }
    }
</script>


