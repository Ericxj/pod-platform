<script lang="ts" setup>
import { useVbenDrawer } from '@vben/common-ui';
import { useVbenForm } from '#/adapter/form';
import { createRole, updateRole } from '#/api/system/role';
import { message } from 'ant-design-vue';
import { ref } from 'vue';

const emit = defineEmits(['success']);
const isUpdate = ref(false);
const recordId = ref<number | undefined>(undefined);

const [Form, formApi] = useVbenForm({
  showActionButtonGroup: false,
  schema: [
    { component: 'Input', label: '角色编码', fieldName: 'roleCode', rules: 'required' },
    { component: 'Input', label: '角色名称', fieldName: 'roleName', rules: 'required' },
    {
      component: 'Select',
      label: '类型',
      fieldName: 'roleType',
      defaultValue: 'BUSINESS',
      componentProps: {
        options: [
          { label: '系统', value: 'SYSTEM' },
          { label: '业务', value: 'BUSINESS' },
        ],
      },
    },
    {
      component: 'Select',
      label: '状态',
      fieldName: 'status',
      defaultValue: 'ENABLED',
      componentProps: {
        options: [
          { label: '启用', value: 'ENABLED' },
          { label: '禁用', value: 'DISABLED' },
        ],
      },
    },
    { component: 'Input', label: '备注', fieldName: 'remark', componentProps: { placeholder: '选填' } },
  ],
});

const [Drawer, drawerApi] = useVbenDrawer({
  onConfirm: async () => {
    await formApi.validate();
    const values = await formApi.getValues();
    try {
      drawerApi.setState({ loading: true });
      if (isUpdate.value && recordId.value != null) {
        await updateRole(recordId.value, { roleName: values.roleName, status: values.status, remark: values.remark });
        message.success('更新成功');
      } else {
        await createRole({
          roleCode: values.roleCode,
          roleName: values.roleName,
          roleType: values.roleType,
          status: values.status,
          remark: values.remark,
        });
        message.success('创建成功');
      }
      drawerApi.close();
      emit('success');
    } finally {
      drawerApi.setState({ loading: false });
    }
  },
  onOpenChange: (isOpen: boolean) => {
    if (isOpen) {
      const data = drawerApi.getData<Record<string, unknown>>();
      isUpdate.value = !!(data?.id != null);
      recordId.value = data?.id as number | undefined;
      if (isUpdate.value) {
        formApi.setValues(data);
        drawerApi.setTitle('编辑角色');
        formApi.updateSchema({ fieldName: 'roleCode', componentProps: { disabled: true } });
      } else {
        formApi.resetForm();
        drawerApi.setTitle('新增角色');
        formApi.updateSchema({ fieldName: 'roleCode', componentProps: { disabled: false } });
      }
    }
  },
});
</script>

<template>
  <Drawer>
    <Form />
  </Drawer>
</template>
