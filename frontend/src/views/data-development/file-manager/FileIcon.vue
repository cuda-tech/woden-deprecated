<style scoped>

</style>

<template>
    <Icon :custom="icons[type].logo" :style="{color: icons[type].color}"/>
</template>

<script>
    export default {
        name: "FileIcon",
        props: [
            'type'
        ],
        data() {
            return {
                icons: {
                    DIR: {
                        logo: 'iconfont icon-folder',
                        color: '#87939A'
                    },
                    SPARK: {
                        logo: 'iconfont icon-spark',
                        color: '#BC5A2B'
                    },
                    MR: {
                        logo: 'iconfont icon-hadoop',
                        color: '#BD8E3E'
                    },
                    SQL: {
                        logo: 'iconfont icon-sql-file',
                        color: '#579242'
                    }
                }
            }

        }
    }
</script>
