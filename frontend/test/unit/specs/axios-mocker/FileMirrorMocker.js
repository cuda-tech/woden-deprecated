export default {
    get: {
        '/file/1/mirror': {
            mirrors: [
                {
                    id: 40,
                    fileId: 1,
                    content: 'qyeosvfgnborxpqfmqyagyheklugcbiylczhxxtaedyeerkkpmajgskpgntohyyl',
                    message: 'krvpcwcg',
                    createTime: '2020-05-04 03:18:07',
                    updateTime: '2023-05-06 00:32:21'
                },
                {
                    id: 56,
                    fileId: 1,
                    content: 'wrjmwtrbeoqmkunhifbkybchxhzxrlfcoelzuoobwukvsavhymdtjqlitblzfyxb',
                    message: 'eneasves',
                    createTime: '2025-09-07 13:38:33',
                    updateTime: '2025-09-10 09:58:15'
                },
                {
                    id: 135,
                    fileId: 1,
                    content: 'iyzzxvkqvjgbhjiewbfnepenadmchhusgrejvjmbgfbbcqhsiijiumghsziaomrm',
                    message: 'lirtmdot',
                    createTime: '2042-05-04 19:25:08',
                    updateTime: '2043-10-15 14:11:59'
                }
            ],
            count: 9
        },
        '/file/1/mirror/40': {
            mirror: {
                id: 40,
                fileId: 1,
                content: 'qyeosvfgnborxpqfmqyagyheklugcbiylczhxxtaedyeerkkpmajgskpgntohyyl',
                message: 'krvpcwcg',
                createTime: '2020-05-04 03:18:07',
                updateTime: '2023-05-06 00:32:21'
            }
        }
    },
    post: {
        '/file/3/mirror': {
            mirror: {
                id: 301,
                fileId: 3,
                content: 'jfoarywksxudqwimajgenwlvebjrjdfbiumogupwebatcyvmjhryscbjwkeshont',
                message: 'testCreate',
                createTime: '2020-05-23 12:56:20',
                updateTime: '2020-05-23 12:56:20'
            }
        }
    },
    put: {},
    delete: {
        '/file/1/mirror/1': {}
    }
}
