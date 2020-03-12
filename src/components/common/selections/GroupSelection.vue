<style scoped>

</style>

<template>
    <Select :value="value" :multiple="multiple" @on-change="val => this.$emit('input', val)">
        <Option v-for="group in groups" :value="group.id">
            {{ group.name }}
        </Option>
    </Select>
</template>

<script>
    /**
     * 项目组选择菜单
     */
    export default {
        name: "GroupSelection",
        props: {
            value: {
                default: null,
            },
            multiple: {
                type: Boolean,
                default: false
            }
        },
        beforeMount() {
            this.axios.get("/group").then(data => {
                this.groupsCount = data.count;
                this.groups = data.groups;
            })
        },
        data() {
            return {
                groupsCount: 0,
                groups: []
            }
        }
    }
</script>


