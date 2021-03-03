export default [
    {
        path: '/manager_center/user',
        name: 'UserManager',
        component: resolve => require(['../views/manager-center/UserManager.vue'], resolve)
    },
    {
        path: '/manager_center/group',
        name: 'GroupManager',
        component: resolve => require(['../views/manager-center/GroupManager.vue'], resolve)
    },
    {
        path: '/manager_center/container',
        name: 'ContainerManager',
        component: resolve => require(['../views/manager-center/ContainerManager.vue'], resolve)
    },
]
