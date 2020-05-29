import Vue from 'vue'

export default new Vue();

/**
 * 所有可能触发的事件
 * select-file(file): 由文件管理器触发，当用户双击某个文件时抛出这个事件，并附带选中的文件
 * switch-file(file): 由编辑器的顶部 toolbar 触发，当用户切换到另一个文件进行开发时抛出这个事件，并附带选中文件
 */
