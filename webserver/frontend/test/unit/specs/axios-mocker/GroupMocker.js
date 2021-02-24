export default {
    get: {
        '/group': {
            groups: [
                {
                    id: 2,
                    name: 'testUpdate',
                    createTime: '2029-05-26 23:17:01',
                    updateTime: '2020-05-23 12:36:21'
                },
                {
                    id: 3,
                    name: 'cdqmxplc',
                    createTime: '2045-06-15 10:48:04',
                    updateTime: '2046-03-20 16:54:28'
                },
                {
                    id: 4,
                    name: 'rdiwafif',
                    createTime: '2025-06-12 09:41:41',
                    updateTime: '2027-01-04 14:36:46'
                }
            ],
            count: 32
        },
        '/group/2': {
            group: {
                id: 2,
                name: 'testUpdate',
                createTime: '2029-05-26 23:17:01',
                updateTime: '2020-05-23 12:36:21'
            }
        }
    },
    post: {
        '/group': {
            group: {
                id: 40,
                name: 'testCreate',
                createTime: '2020-05-23 12:36:52',
                updateTime: '2020-05-23 12:36:52'
            }
        }
    },
    put: {
        '/group/2':{
            group: {
                id: 2,
                name: 'testUpdate',
                createTime: '2029-05-26 23:17:01',
                updateTime: '2020-05-23 12:36:20'
            }
        }
    },
    delete: {
        '/group/1': {}
    }
}
