<script lang="ts" setup>
import { useVbenDrawer } from '@vben/common-ui';
import { useVbenForm } from '#/adapter/form';
import { createPermission, updatePermission, validatePermission } from '#/api/system/permission';
import { getPermissionTree } from '#/api/system/permission';
import { message } from 'ant-design-vue';
import { ref } from 'vue';

const emit = defineEmits(['success']);
const isUpdate = ref(false);
const recordId = ref<number | undefined>(undefined);
const parentOptions = ref<{ label: string; value: number }[]>([{ label: '根', value: 0 }]);

async function loadParentOptions() {
  const list = await getPermissionTree('ALL');
  const flatten = (nodes: typeof list, level = 0): { label: string; value: number }[] => {
    const opts: { label: string; value: number }[] = [];
    nodes.forEach((n) => {
      opts.push({ label: '　'.repeat(level) + `[${n.permType}] ${n.permName}`, value: n.id });
      if (n.children?.length) opts.push(...flatten(n.children, level + 1));
    });
    return opts;
  };
  parentOptions.value = [{ label: '根', value: 0 }, ...flatten(list)];
}

const [Form, formApi] = useVbenForm({
  showActionButtonGroup: false,
  schema: [
    { component: 'Input', label: '权限码', fieldName: 'permCode', rules: 'required' },
    { component: 'Input', label: '名称', fieldName: 'permName', rules: 'required' },
    {
      component: 'Select',
      label: '类型',
      fieldName: 'permType',
      rules: 'required',
      componentProps: {
        options: [
          { label: 'MENU', value: 'MENU' },
          { label: 'BUTTON', value: 'BUTTON' },
          { label: 'API', value: 'API' },
        ],
      },
    },
    { component: 'Input', label: '菜单路径', fieldName: 'menuPath' },
    { component: 'Input', label: '组件', fieldName: 'component' },
    { component: 'Input', label: 'API 方法', fieldName: 'apiMethod', componentProps: { placeholder: 'GET/POST' } },
    { component: 'Input', label: 'API 路径', fieldName: 'apiPath', componentProps: { placeholder: '/api/...' } },
    {
      component: 'Select',
      label: '父级',
      fieldName: 'parentId',
      componentProps: { options: parentOptions, fieldNames: { label: 'label', value: 'value' } },
    },
    { component: 'InputNumber', label: '排序', fieldName: 'sortNo', defaultValue: 0 },
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
  ],
});

const [Drawer, drawerApi] = useVbenDrawer({
  onConfirm: async () => {
    await formApi.validate();
    const values = await formApi.getValues();
    const permTypeVal = values.permType ?? 'BUTTON';
    try {
      const valid = await validatePermission({
        permCode: values.permCode?.trim() || undefined,
        menuPath: permTypeVal === 'MENU' ? values.menuPath?.trim() : undefined,
        apiMethod: permTypeVal === 'API' ? values.apiMethod?.trim() : undefined,
        apiPath: permTypeVal === 'API' ? values.apiPath?.trim() : undefined,
        permType: permTypeVal,
        excludeId: isUpdate.value ? recordId.value : undefined,
      });
      if (!valid.valid) {
        message.warning(valid.message ?? '校验不通过');
        return;
      }
    } catch (e) {
      return;
    }
    try {
      drawerApi.setState({ loading: true });
      const payload = {
        permCode: values.permCode?.trim(),
        permName: values.permName?.trim(),
        permType: permTypeVal,
        menuPath: values.menuPath?.trim() || undefined,
        component: values.component?.trim() || undefined,
        apiMethod: values.apiMethod?.trim() || undefined,
        apiPath: values.apiPath?.trim() || undefined,
        parentId: values.parentId === 0 ? undefined : values.parentId,
        sortNo: values.sortNo ?? 0,
        status: values.status ?? 'ENABLED',
      };
      if (isUpdate.value && recordId.value != null) {
        await updatePermission(recordId.value, payload);
        message.success('更新成功');
      } else {
        await createPermission(payload);
        message.success('创建成功');
      }
      drawerApi.close();
      emit('success');
    } finally {
      drawerApi.setState({ loading: false });
    }
  },
  onOpenChange: async (isOpen: boolean) => {
    if (isOpen) {
      await loadParentOptions();
      formApi.updateSchema({ fieldName: 'parentId', componentProps: { options: parentOptions } });
      const data = drawerApi.getData<Record<string, unknown> & { isUpdate?: boolean }>();
      isUpdate.value = !!data?.isUpdate;
      recordId.value = data?.id as number | undefined;
      if (isUpdate.value && data) {
        formApi.setValues(data);
        drawerApi.setTitle('编辑权限点');
        formApi.updateSchema({ fieldName: 'permCode', componentProps: { disabled: true } });
      } else {
        formApi.resetForm();
        formApi.setValues({ parentId: data?.parentId ?? 0, sortNo: 0, permType: 'BUTTON' });
        drawerApi.setTitle('新增权限点');
        formApi.updateSchema({ fieldName: 'permCode', componentProps: { disabled: false } });
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
