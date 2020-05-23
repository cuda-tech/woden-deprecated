import fileApi from '../../../../src/api/FileAPI'
import axios from 'axios';

jest.mock('axios');

describe('文件接口', () => {

    test('获取某个文件夹下的文件列表', done => {
        axios.get.mockResolvedValue({
            files: [
                {
                    id: 4,
                    groupId: 1,
                    ownerId: 26,
                    name: 'zwgjydgn',
                    type: 'DIR',
                    parentId: 1,
                    createTime: '2002-05-14 08:16:08',
                    updateTime: '2004-04-17 08:43:14'
                },
                {
                    id: 43,
                    groupId: 1,
                    ownerId: 140,
                    name: 'kniovyqn',
                    type: 'SQL',
                    parentId: 1,
                    createTime: '2009-09-17 00:47:55',
                    updateTime: '2011-11-10 01:43:32'
                },
                {
                    id: 6,
                    groupId: 1,
                    ownerId: 167,
                    name: 'ladlehnr',
                    type: 'SQL',
                    parentId: 1,
                    createTime: '2003-09-09 05:14:44',
                    updateTime: '2004-06-15 14:47:45'
                },
                {
                    id: 2,
                    groupId: 1,
                    ownerId: 10,
                    name: 'jldwzlys',
                    type: 'SPARK',
                    parentId: 1,
                    createTime: '2048-12-27 13:12:08',
                    updateTime: '2049-01-24 17:09:09'
                }
            ],
            count: 4
        });

        fileApi.listing(1, (count, files) => {
            expect(count).toBe(4);
            expect(files).toEqual([
                {
                    id: 4,
                    groupId: 1,
                    ownerId: 26,
                    name: 'zwgjydgn',
                    type: 'DIR',
                    parentId: 1,
                    createTime: '2002-05-14 08:16:08',
                    updateTime: '2004-04-17 08:43:14'
                },
                {
                    id: 43,
                    groupId: 1,
                    ownerId: 140,
                    name: 'kniovyqn',
                    type: 'SQL',
                    parentId: 1,
                    createTime: '2009-09-17 00:47:55',
                    updateTime: '2011-11-10 01:43:32'
                },
                {
                    id: 6,
                    groupId: 1,
                    ownerId: 167,
                    name: 'ladlehnr',
                    type: 'SQL',
                    parentId: 1,
                    createTime: '2003-09-09 05:14:44',
                    updateTime: '2004-06-15 14:47:45'
                },
                {
                    id: 2,
                    groupId: 1,
                    ownerId: 10,
                    name: 'jldwzlys',
                    type: 'SPARK',
                    parentId: 1,
                    createTime: '2048-12-27 13:12:08',
                    updateTime: '2049-01-24 17:09:09'
                }
            ]);
            done()
        })
    });

    test('模糊搜索文件列表', done => {
        axios.get.mockResolvedValue({
            files: [
                {
                    id: 7,
                    groupId: 7,
                    ownerId: 36,
                    name: 'bamvjrno',
                    type: 'SQL',
                    parentId: 64,
                    createTime: '2045-08-02 02:39:46',
                    updateTime: '2048-06-19 13:58:27'
                },
                {
                    id: 18,
                    groupId: 16,
                    ownerId: 48,
                    name: 'bcmawkte',
                    type: 'SQL',
                    parentId: 49,
                    createTime: '2002-05-01 00:41:43',
                    updateTime: '2003-10-20 15:00:30'
                },
                {
                    id: 60,
                    groupId: 5,
                    ownerId: 48,
                    name: 'lwbaccod',
                    type: 'SQL',
                    parentId: 7,
                    createTime: '2007-02-12 03:45:03',
                    updateTime: '2008-04-14 18:06:49'
                }
            ],
            count: 3
        });

        fileApi.search('a b', (count, files) => {
            expect(count).toBe(3);
            expect(files).toEqual([
                {
                    id: 7,
                    groupId: 7,
                    ownerId: 36,
                    name: 'bamvjrno',
                    type: 'SQL',
                    parentId: 64,
                    createTime: '2045-08-02 02:39:46',
                    updateTime: '2048-06-19 13:58:27'
                },
                {
                    id: 18,
                    groupId: 16,
                    ownerId: 48,
                    name: 'bcmawkte',
                    type: 'SQL',
                    parentId: 49,
                    createTime: '2002-05-01 00:41:43',
                    updateTime: '2003-10-20 15:00:30'
                },
                {
                    id: 60,
                    groupId: 5,
                    ownerId: 48,
                    name: 'lwbaccod',
                    type: 'SQL',
                    parentId: 7,
                    createTime: '2007-02-12 03:45:03',
                    updateTime: '2008-04-14 18:06:49'
                }
            ]);
            done()
        })
    });

    test('查找指定项目组的根目录', done => {
        axios.get.mockResolvedValue({
            file: {
                id: 1,
                groupId: 1,
                ownerId: 1,
                name: 'root_project',
                type: 'DIR',
                parentId: null,
                createTime: '2037-05-20 14:58:39',
                updateTime: '2040-02-04 21:46:36'
            }
        });

        fileApi.findRoot(1, file => {
            expect(file).toEqual({
                id: 1,
                groupId: 1,
                ownerId: 1,
                name: 'root_project',
                type: 'DIR',
                parentId: null,
                createTime: '2037-05-20 14:58:39',
                updateTime: '2040-02-04 21:46:36'
            });
            done()
        })

    });

    test('查找指定文件节点的所有父节点', done => {
        axios.get.mockResolvedValue({
            files: [
                {
                    id: 1,
                    groupId: 1,
                    ownerId: 1,
                    name: 'root_project',
                    type: 'DIR',
                    parentId: null,
                    createTime: '2037-05-20 14:58:39',
                    updateTime: '2040-02-04 21:46:36'
                }
            ],
            count: 1
        });
        fileApi.listingParent(1, (count, files) => {
            expect(count).toBe(1);
            expect(files).toEqual([
                {
                    id: 1,
                    groupId: 1,
                    ownerId: 1,
                    name: 'root_project',
                    type: 'DIR',
                    parentId: null,
                    createTime: '2037-05-20 14:58:39',
                    updateTime: '2040-02-04 21:46:36'
                }
            ]);
            done()
        })
    });

    test('查找指定文件节点当前的内容', done => {
        axios.get.mockResolvedValue({
            content: {
                content: 'jfoarywksxudqwimajgenwlvebjrjdfbiumogupwebatcyvmjhryscbjwkeshont'
            }
        });

        fileApi.getContent(1, content => {
            expect(content).toBe('jfoarywksxudqwimajgenwlvebjrjdfbiumogupwebatcyvmjhryscbjwkeshont');
            done()
        })

    });

    test('创建文件节点', done => {
        axios.post.mockResolvedValue({
            file: {
                id: 70,
                groupId: 1,
                ownerId: 1,
                name: 'testCreate',
                type: 'SQL',
                parentId: 1,
                createTime: '2020-05-23 12:49:53',
                updateTime: '2020-05-23 12:49:53'
            }
        });
        fileApi.create({groupId: 1, name: 'testCreate', parentId: 1}, file => {
            expect(file).toEqual({
                id: 70,
                groupId: 1,
                ownerId: 1,
                name: 'testCreate',
                type: 'SQL',
                parentId: 1,
                createTime: '2020-05-23 12:49:53',
                updateTime: '2020-05-23 12:49:53'
            });
            done()
        })

    });

    test('更新指定 ID 的文件节点', done => {
        axios.put.mockResolvedValue({
            file: {
                id: 4,
                groupId: 1,
                ownerId: 26,
                name: 'testUpdate',
                type: 'DIR',
                parentId: 1,
                createTime: '2002-05-14 08:16:08',
                updateTime: '2020-05-23 12:48:46'
            }
        });

        fileApi.update(4, {ownerId: 26, name: 'testUpdate'}, file => {
            expect(file).toEqual({
                id: 4,
                groupId: 1,
                ownerId: 26,
                name: 'testUpdate',
                type: 'DIR',
                parentId: 1,
                createTime: '2002-05-14 08:16:08',
                updateTime: '2020-05-23 12:48:46'
            });
            done()
        })

    });

    test('删除指定 ID 的文件节点', done => {
        axios.delete.mockResolvedValue();
        fileApi.delete(1, done)
    });

    test('分页查询指定文件的镜像列表', done => {
        axios.get.mockResolvedValue({
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
        });

        fileApi.listingMirror(1, 1, 3, null, (count, mirrors) => {
            expect(count).toBe(9);
            expect(mirrors).toEqual([
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
            ]);
            done()
        })
    });

    test('创建文件镜像', done => {
        axios.post.mockResolvedValue({
            mirror: {
                id: 301,
                fileId: 3,
                content: 'jfoarywksxudqwimajgenwlvebjrjdfbiumogupwebatcyvmjhryscbjwkeshont',
                message: 'testCreate',
                createTime: '2020-05-23 12:56:20',
                updateTime: '2020-05-23 12:56:20'
            }
        });
        fileApi.createMirror(3, mirror => {
            expect(mirror).toEqual({
                id: 301,
                fileId: 3,
                content: 'jfoarywksxudqwimajgenwlvebjrjdfbiumogupwebatcyvmjhryscbjwkeshont',
                message: 'testCreate',
                createTime: '2020-05-23 12:56:20',
                updateTime: '2020-05-23 12:56:20'
            });
            done()
        })

    });

    test('查找指定的文件镜像', done => {
        axios.get.mockResolvedValue({
            mirror: {
                id: 40,
                fileId: 1,
                content: 'qyeosvfgnborxpqfmqyagyheklugcbiylczhxxtaedyeerkkpmajgskpgntohyyl',
                message: 'krvpcwcg',
                createTime: '2020-05-04 03:18:07',
                updateTime: '2023-05-06 00:32:21'
            }
        });
        fileApi.findMirror(1, 40, mirror => {
            expect(mirror).toEqual({
                id: 40,
                fileId: 1,
                content: 'qyeosvfgnborxpqfmqyagyheklugcbiylczhxxtaedyeerkkpmajgskpgntohyyl',
                message: 'krvpcwcg',
                createTime: '2020-05-04 03:18:07',
                updateTime: '2023-05-06 00:32:21'
            });
            done()
        });
    });

    test('删除指定的文件镜像', done => {
        axios.delete.mockResolvedValue();
        fileApi.deleteMirror(1, 1, done);
    });

});