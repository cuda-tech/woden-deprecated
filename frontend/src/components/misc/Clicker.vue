<template>
    <span @click="handleClick">
        <!-- @slot 包装鼠标单击&双击事件监控 -->
        <slot/>
    </span>
</template>
<script>
    /**
     * 鼠标单击 & 双击 wrapper
     * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
     * @since 1.0.0
     */
    export default {
        name: "Clicker",
        props: {
            /**
             * 双击间隔, 如果点击一次后在 delay 毫秒内再点击一次则判定为双击事件
             */
            delay: {
                type: Number,
                default: 250
            }
        },
        data() {
            return {
                clickCount: 0,
                clickTimer: null
            }
        },

        methods: {
            handleClick(e) {
                e.preventDefault();
                this.clickCount++;
                if (this.clickCount === 1) {
                    this.clickTimer = setTimeout(() => {
                        this.clickCount = 0;
                        /**
                         * 单击事件
                         */
                        this.$emit('single-click');
                    }, this.delay)
                } else if (this.clickCount === 2) {
                    clearTimeout(this.clickTimer);
                    this.clickCount = 0;
                    /**
                     * 双击事件
                     */
                    this.$emit('double-click');
                }
            }
        }
    }
</script>
