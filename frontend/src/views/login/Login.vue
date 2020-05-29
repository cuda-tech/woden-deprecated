<template>
    <div align="center">
        <Form ref="formInline" inline style="margin-top: 25%">
            <FormItem prop="user">
                <Input type="text" v-model="user.name" placeholder="Username">
                    <Icon type="ios-person-outline" slot="prepend"></Icon>
                </Input>
            </FormItem>
            <br>
            <FormItem prop="password">
                <Input type="password" v-model="user.password" placeholder="Password">
                    <Icon type="ios-lock-outline" slot="prepend"></Icon>
                </Input>
            </FormItem>
            <br>
            <FormItem>
                <Button type="primary" @click="login">登录</Button>
            </FormItem>
        </Form>
    </div>
</template>

<script>
    import UserAPI from "../../api/UserAPI";

    export default {
        name: "Login",
        data() {
            return {
                user: {
                    name: null,
                    password: null
                }
            }
        },
        methods: {
            login() {
                UserAPI.login(this.user.name, this.user.password, token => {
                    window.localStorage["token"] = token;
                    this.$router.push({name: 'Home'})
                });
            }
        }
    }
</script>

<style scoped>

</style>
