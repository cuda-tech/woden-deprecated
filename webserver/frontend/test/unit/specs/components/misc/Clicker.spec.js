import {mount} from '@vue/test-utils';
import Clicker from "@/components/misc/Clicker";

describe('鼠标点击事件处理器', async () => {

    test('单击事件', done => {
        let wrapper = mount(Clicker);
        wrapper.find('span').trigger('click');
        expect(wrapper.emitted()).toEqual({});
        setTimeout(() => {
            expect(wrapper.emitted()).toHaveProperty('single-click');
            done()
        }, 300)
    });

    test('双击', done => {
        let wrapper = mount(Clicker);

        // 点击一次
        wrapper.find('span').trigger('click');

        // 100ms 后再点击一次
        setTimeout(() => {
            wrapper.find('span').trigger('click');
        }, 100);

        // 100ms 后再等待 251ms 检查双击事件抛出
        setTimeout(() => {
            expect(wrapper.emitted()).toHaveProperty('double-click');
            done()
        }, 100 + 250 + 1)
    })


});
