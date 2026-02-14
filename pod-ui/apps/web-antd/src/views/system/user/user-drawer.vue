<script lang="ts" setup>
import { useVbenDrawer } from '@vben/common-ui';
import { useVbenForm } from '#/adapter/form';
import { createUser, updateUser } from '#/api/system/user';
import { message } from 'ant-design-vue';
import { ref } from 'vue';

const emit = defineEmits(['success']);
const isUpdate = ref(false);
const recordId = ref<number | undefined>(undefined);

const [Form, formApi] = useVbenForm({
    showActionButtonGroup: false,
    schema: [
        {
            component: 'Input',
            label: '用户名',
            fieldName: 'username',
            rules: 'required',
        },
        {
            component: 'Input',
            label: '真实姓名',
            fieldName: 'realName',
            rules: 'required',
        },
        {
            component: 'Input',
            label: '手机号',
            fieldName: 'phone',
        },
        {
            component: 'Select',
            label: '状态',
            fieldName: 'status',
            defaultValue: 'ENABLED',
            componentProps: {
                options: [
                    { label: '启用', value: 'ENABLED' },
                    { label: '禁用', value: 'DISABLED' }
                ]
            }
        },
    ]
});

const [Drawer, drawerApi] = useVbenDrawer({
    onConfirm: async () => {
        await formApi.validate();
        const values = await formApi.getValues();
        
        try {
            drawerApi.setState({ loading: true });
            if (isUpdate.value) {
                await updateUser({ id: recordId.value, ...values });
                message.success('更新成功');
            } else {
                await createUser(values);
                message.success('创建成功');
            }
            drawerApi.close();
            emit('success');
        } finally {
            drawerApi.setState({ loading: false });
        }
    },
    onOpenChange: (isOpen) => {
        if (isOpen) {
            const data = drawerApi.getData<any>();
            isUpdate.value = !!data?.id;
            recordId.value = data?.id;
            
            if (isUpdate.value) {
                formApi.setValues(data);
                drawerApi.setTitle('编辑用户');
                formApi.updateSchema({
                    fieldName: 'username',
                    componentProps: { disabled: true }
                });
            } else {
                formApi.resetForm();
                drawerApi.setTitle('新增用户');
                formApi.updateSchema({
                    fieldName: 'username',
                    componentProps: { disabled: false }
                });
            }
        }
    }
});
</script>

<template>
    <Drawer>
        <Form />
    </Drawer>
</template>
