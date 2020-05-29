<template>
    <Tooltip :placement="placement">
        <Icon type="md-help-circle"/>
        <div slot="content">
            <slot/>
        </div>
    </Tooltip>
</template>
<script>
    /**
     * 帮助提示
     */
    export default {
        name: 'Tips',
        props: {
            /**
             * 提示信息
             */
            message: {
                type: String,
                default: ''
            },

            /**
             *
             */
            placement: {
                type: String,
                default: 'top'
            }
        },
    }
</script>
