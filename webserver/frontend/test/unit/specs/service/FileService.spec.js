import '../axios-mocker';
import FileService from "@/service/FileService";

describe('文件服务', () => {

    test('获取某个文件夹下的文件列表', done => {
        FileService.listing(1, (count, files) => {
            expect(count).toBe(4);
            expect(files).toEqual([
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
            ]);
            done()
        })
    });

    test('模糊搜索文件列表', done => {
        FileService.search('a b', (count, files) => {
            expect(count).toBe(3);
            expect(files).toEqual([
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
            ]);
            done()
        })
    });

    test('查找指定项目组的根目录', done => {
        FileService.findRoot(1, file => {
            expect(file).toEqual({
                id: 1,
                teamId: 1,
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
        FileService.listingParent(1, (count, files) => {
            expect(count).toBe(1);
            expect(files).toEqual([
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
            ]);
            done()
        })
    });

    test('查找指定文件节点当前的内容', done => {
        FileService.getContent(1, content => {
            expect(content).toBe('jfoarywksxudqwimajgenwlvebjrjdfbiumogupwebatcyvmjhryscbjwkeshont');
            done()
        })
    });

    test('创建文件节点', done => {
        FileService.create({teamId: 1, name: 'testCreate', parentId: 1}, file => {
            expect(file).toEqual({
                id: 70,
                teamId: 1,
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
        FileService.update(4, {ownerId: 26, name: 'testUpdate'}, file => {
            expect(file).toEqual({
                id: 4,
                teamId: 1,
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
        FileService.delete(1, done)
    });

    test('分页查询指定文件的镜像列表', done => {
        FileService.listingMirror(1, {pageId: 1, pageSize: 3}, (count, mirrors) => {
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
        FileService.createMirror(3, mirror => {
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
        FileService.findMirror(1, 40, mirror => {
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
        FileService.deleteMirror(1, 1, done);
    });


});
