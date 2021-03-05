export default {
    get: {
        '/file': {
            files: [
                {
                    id: 4,
                    teamId: 1,
                    ownerId: 26,
                    name: 'zwgjydgn',
                    type: 'DIR',
                    parentId: 1,
                    createTime: '2002-05-14 08:16:08',
                    updateTime: '2004-04-17 08:43:14'
                },
                {
                    id: 43,
                    teamId: 1,
                    ownerId: 140,
                    name: 'kniovyqn',
                    type: 'SQL',
                    parentId: 1,
                    createTime: '2009-09-17 00:47:55',
                    updateTime: '2011-11-10 01:43:32'
                },
                {
                    id: 6,
                    teamId: 1,
                    ownerId: 167,
                    name: 'ladlehnr',
                    type: 'SQL',
                    parentId: 1,
                    createTime: '2003-09-09 05:14:44',
                    updateTime: '2004-06-15 14:47:45'
                },
                {
                    id: 2,
                    teamId: 1,
                    ownerId: 10,
                    name: 'jldwzlys',
                    type: 'SPARK',
                    parentId: 1,
                    createTime: '2048-12-27 13:12:08',
                    updateTime: '2049-01-24 17:09:09'
                }
            ],
            count: 4
        },
        '/file/search': {
            files: [
                {
                    id: 7,
                    teamId: 7,
                    ownerId: 36,
                    name: 'bamvjrno',
                    type: 'SQL',
                    parentId: 64,
                    createTime: '2045-08-02 02:39:46',
                    updateTime: '2048-06-19 13:58:27'
                },
                {
                    id: 18,
                    teamId: 16,
                    ownerId: 48,
                    name: 'bcmawkte',
                    type: 'SQL',
                    parentId: 49,
                    createTime: '2002-05-01 00:41:43',
                    updateTime: '2003-10-20 15:00:30'
                },
                {
                    id: 60,
                    teamId: 5,
                    ownerId: 48,
                    name: 'lwbaccod',
                    type: 'SQL',
                    parentId: 7,
                    createTime: '2007-02-12 03:45:03',
                    updateTime: '2008-04-14 18:06:49'
                }
            ],
            count: 3
        },
        '/file/root': {
            file: {
                id: 1,
                teamId: 1,
                ownerId: 1,
                name: 'root_project',
                type: 'DIR',
                parentId: null,
                createTime: '2037-05-20 14:58:39',
                updateTime: '2040-02-04 21:46:36'
            }
        },
        '/file/1/parent': {
            files: [
                {
                    id: 1,
                    teamId: 1,
                    ownerId: 1,
                    name: 'root_project',
                    type: 'DIR',
                    parentId: null,
                    createTime: '2037-05-20 14:58:39',
                    updateTime: '2040-02-04 21:46:36'
                }
            ],
            count: 1
        },
        '/file/1/content': {
            content: {
                content: 'jfoarywksxudqwimajgenwlvebjrjdfbiumogupwebatcyvmjhryscbjwkeshont'
            }
        },
    },
    post: {
        '/file': {
            file: {
                id: 70,
                teamId: 1,
                ownerId: 1,
                name: 'testCreate',
                type: 'SQL',
                parentId: 1,
                createTime: '2020-05-23 12:49:53',
                updateTime: '2020-05-23 12:49:53'
            }
        }
    },
    put: {
        '/file/4': {
            file: {
                id: 4,
                teamId: 1,
                ownerId: 26,
                name: 'testUpdate',
                type: 'DIR',
                parentId: 1,
                createTime: '2002-05-14 08:16:08',
                updateTime: '2020-05-23 12:48:46'
            }
        }
    },
    delete: {
        '/file/1': {}
    }
}
