<template>
    <div>
        <Form ref="formInline" inline>
            <FormItem prop="user">
                <Input type="text" v-model="user.name" placeholder="Username">
                    <Icon type="ios-person-outline" slot="prepend"></Icon>
                </Input>
            </FormItem>
            <FormItem prop="password">
                <Input type="password" v-model="user.password" placeholder="Password">
                    <Icon type="ios-lock-outline" slot="prepend"></Icon>
                </Input>
            </FormItem>
            <FormItem>
                <Button type="primary" @click="login">登录</Button>
            </FormItem>
        </Form>
    </div>

</template>

<script>
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
                let data = new FormData();
                data.set("username", this.user.name);
                data.set("password", this.user.password);
                this.axios.post("/login", data).then(data => {
                    window.localStorage["token"] = data.token;
                    this.$router.push({name: 'Home'})
                })
            }
        }
    }
</script>

<style scoped>

</style>
