/**
 * mock 后端请求返回, 一定要在测试文件的最开始 import 这个文件，否则无法 mock
 */
import axios from 'axios';
import FileMocker from "./FileMocker";
import FileMirrorMocker from "./FileMirrorMocker";
import GroupMocker from "./GroupMocker";
import ContainerMocker from "./ContainerMocker";
import UserMocker from "./UserMocker";

jest.mock('axios');

// 这四个变量这么写太丑陋了，后面有时间改一下
let getMapping = {
    ...FileMocker.get,
    ...FileMirrorMocker.get,
    ...GroupMocker.get,
    ...ContainerMocker.get,
    ...UserMocker.get
};
let postMapping = {
    ...FileMocker.post,
    ...FileMirrorMocker.post,
    ...GroupMocker.post,
    ...ContainerMocker.post,
    ...UserMocker.post
};
let putMapping = {
    ...FileMocker.put,
    ...FileMirrorMocker.put,
    ...GroupMocker.put,
    ...ContainerMocker.put,
    ...UserMocker.put
};
let deleteMapping = {
    ...FileMocker.delete,
    ...FileMirrorMocker.delete,
    ...GroupMocker.delete,
    ...ContainerMocker.delete,
    ...UserMocker.delete
};

axios.get.mockImplementation(url => Promise.resolve(getMapping[url]));
axios.post.mockImplementation(url => Promise.resolve(postMapping[url]));
axios.put.mockImplementation(url => Promise.resolve(putMapping[url]));
axios.delete.mockImplementation(url => Promise.resolve(deleteMapping[url]));
