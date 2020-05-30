export default {
    get: {
        '/user': {
            count: 144,
            users: [
                {
                    id: 1,
                    groups: [1],
                    name: 'root',
                    email: 'root@datahub.com',
                    createTime: '2048-08-14 06:10:35',
                    updateTime: '2051-03-13 21:06:23'
                },
                {
                    id: 2,
                    groups: [1, 2, 5, 6, 7, 8, 9],
                    name: 'guest',
                    email: 'guest@datahub.com',
                    createTime: '2041-02-10 19:37:55',
                    updateTime: '2042-03-23 08:54:17'
                },
                {
                    id: 3,
                    groups: [2, 3, 4, 6, 8],
                    name: 'OHzXwnDAAd',
                    email: 'OHzXwnDAAd@189.com',
                    createTime: '2041-11-20 12:44:46',
                    updateTime: '2044-05-12 14:09:07'
                }
            ]
        },
        '/user/current': {
            user: {
                id: 1,
                groups: [1],
                name: 'root',
                email: 'root@datahub.com',
                createTime: '2048-08-14 06:10:35',
                updateTime: '2051-03-13 21:06:23'
            }
        },
        '/user/10': {
            user: {
                id: 10,
                groups: [1, 3, 4, 5, 6, 8, 9],
                name: 'IinOzxLtGL',
                email: 'IinOzxLtGL@139.com',
                createTime: '2018-03-21 03:59:24',
                updateTime: '2019-02-28 12:26:06'
            }
        }
    },
    post: {
        '/login': {token: 'token Value'},
        '/user': {
            user: {
                id: 180,
                groups: [1, 2, 3],
                name: 'testName',
                email: 'testEmail',
                createTime: '2020-05-22 01:43:10',
                updateTime: '2020-05-22 01:43:10'
            }
        }
    },
    put: {
        '/user/180': {
            user: {
                id: 180,
                groups: [1, 2, 3],
                name: 'testName',
                email: 'testEmail',
                createTime: '2020-05-22 01:43:11',
                updateTime: '2020-05-22 01:49:55'
            }
        }
    },
    delete: {
        '/user/1': {}
    }
}
