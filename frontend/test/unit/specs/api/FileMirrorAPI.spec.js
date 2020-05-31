import '../axios-mocker';
import FileMirrorAPI from "@/api/FileMirrorAPI";

describe('文件镜像接口', () => {
    test('分页查询指定文件的镜像列表', done => {
        FileMirrorAPI.listingMirror(1, {pageId: 1, pageSize: 3}, (count, mirrors) => {
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
        FileMirrorAPI.createMirror(3, mirror => {
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
        FileMirrorAPI.findMirror(1, 40, mirror => {
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
        FileMirrorAPI.deleteMirror(1, 1, done);
    });


});
