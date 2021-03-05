export default [
    {
        path: '/manager_center/user',
        name: 'UserManager',
        component: resolve => require(['../views/manager-center/UserManager.vue'], resolve)
    },
    {
        path: '/manager_center/team',
        name: 'TeamManager',
        component: resolve => require(['../views/manager-center/TeamManager.vue'], resolve)
    },
    {
        path: '/manager_center/container',
        name: 'ContainerManager',
        component: resolve => require(['../views/manager-center/ContainerManager.vue'], resolve)
    },
]
