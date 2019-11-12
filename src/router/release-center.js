export default [
    {
        path: '/release_center/create_package',
        name: 'ReleaseCenterCreatePackage',
        component: resolve => require(['../components/release-center/CreatePackage.vue'], resolve)
    },
    {
        path: '/release_center/list_package',
        name: 'ReleaseCenterListPackage',
        component: resolve => require(['../components/release-center/ListPackage.vue'], resolve)
    }
]
