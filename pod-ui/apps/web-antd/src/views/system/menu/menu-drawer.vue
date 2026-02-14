<script lang="ts" setup>
import { useVbenDrawer } from '@vben/common-ui';
import { useVbenForm } from '#/adapter/form';
import { createPermission, updatePermission, validatePermission } from '#/api/system/permission';
import { getPermissionTree } from '#/api/system/permission';
import { message } from 'ant-design-vue';
import { ref, computed } from 'vue';

const emit = defineEmits(['success']);
const isUpdate = ref(false);
const recordId = ref<number | undefined>(undefined);
const parentOptions = ref<{ label: string; value: number }[]>([{ label: '根菜单', value: 0 }]);

async function loadParentOptions() {
  const list = await getPermissionTree('MENU');
  const flatten = (nodes: typeof list, level = 0): { label: string; value: number }[] => {
    const opts: { label: string; value: number }[] = [];
    nodes.forEach((n) => {
      opts.push({ label: '　'.repeat(level) + (n.permName || n.permCode), value: n.id });
      if (n.children?.length) opts.push(...flatten(n.children, level + 1));
    });
    return opts;
  };
  parentOptions.value = [{ label: '根菜单', value: 0 }, ...flatten(list)];
}

const [Form, formApi] = useVbenForm({
  showActionButtonGroup: false,
  schema: [
    { component: 'Input', label: '权限码', fieldName: 'permCode', rules: 'required' },
    { component: 'Input', label: '名称', fieldName: 'permName', rules: 'required' },
    { component: 'Input', label: '菜单路径', fieldName: 'menuPath', componentProps: { placeholder: '/system/xxx' } },
    { component: 'Input', label: '组件', fieldName: 'component', componentProps: { placeholder: '/system/xxx/index' } },
    { component: 'Input', label: '图标', fieldName: 'icon' },
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
    { component: 'Switch', label: '隐藏', fieldName: 'hidden', defaultValue: false },
    { component: 'Switch', label: '缓存', fieldName: 'keepAlive', defaultValue: true },
    { component: 'Switch', label: '总是显示', fieldName: 'alwaysShow', defaultValue: false },
  ],
});

const [Drawer, drawerApi] = useVbenDrawer({
  onConfirm: async () => {
    await formApi.validate();
    const values = await formApi.getValues();
    const permCode = values.permCode?.trim();
    const menuPath = values.menuPath?.trim();
    try {
      const valid = await validatePermission({
        permCode: permCode || undefined,
        menuPath: menuPath || undefined,
        permType: 'MENU',
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
        permCode,
        permName: values.permName?.trim(),
        permType: 'MENU',
        menuPath: menuPath || undefined,
        component: values.component?.trim() || undefined,
        icon: values.icon?.trim() || undefined,
        parentId: values.parentId === 0 ? undefined : values.parentId,
        sortNo: values.sortNo ?? 0,
        status: values.status ?? 'ENABLED',
        hidden: !!values.hidden,
        keepAlive: values.keepAlive !== false,
        alwaysShow: !!values.alwaysShow,
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
        drawerApi.setTitle('编辑菜单');
        formApi.updateSchema({ fieldName: 'permCode', componentProps: { disabled: true } });
      } else {
        formApi.resetForm();
        formApi.setValues({ parentId: data?.parentId ?? 0, sortNo: 0 });
        drawerApi.setTitle('新增菜单');
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
