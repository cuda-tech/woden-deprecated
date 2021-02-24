<template>
    <Card style="margin: 30px">
        <Row>
            <Col span="10">
            <Input v-model="search.key">
            <Select v-model="search.by" slot="prepend" style="width: 80px">
                <Option value="user_name">用户</Option>
                <Option value="project_name">项目</Option>
                <Option value="instance_id">实例ID</Option>
            </Select>
            <Button slot="append" icon="ios-search"></Button>
            </Input>
            </Col>
            <Col>
            </Col>
        </Row>

        <Table :columns="columns" :data="instances" style="margin-top: 30px"></Table>
        <Page :total="100" style="float: right"/>
    </Card>
</template>
