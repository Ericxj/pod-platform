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
const drawerType = ref<'MENU' | 'BUTTON' | 'API'>('MENU');
const parentOptions = ref<{ label: string; value: number }[]>([]);

async function loadParentOptions() {
  const list = await getPermissionTree('MENU', true);
  const flatten = (nodes: any[], level = 0): { label: string; value: number }[] => {
    const opts: { label: string; value: number }[] = [];
    nodes?.forEach((n) => {
      opts.push({ label: '　'.repeat(level) + (n.permName || n.permCode || String(n.id)), value: n.id });
      if (n.children?.length) opts.push(...flatten(n.children, level + 1));
    });
    return opts;
  };
  parentOptions.value = [{ label: '根', value: 0 }, ...flatten(list)];
}

const schema = computed(() => {
  const base = [
    { component: 'Input', label: '权限码', fieldName: 'permCode', rules: 'required' },
    { component: 'Input', label: '名称', fieldName: 'permName', rules: 'required' },
    {
      component: 'Select',
      label: drawerType.value === 'MENU' ? '父级菜单' : '所属菜单',
      fieldName: 'parentId',
      rules: drawerType.value === 'BUTTON' ? 'required' : undefined,
      componentProps: { options: parentOptions.value, fieldNames: { label: 'label', value: 'value' } },
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
  ];
  if (drawerType.value === 'MENU') {
    return [
      ...base,
      { component: 'Input', label: '菜单路径', fieldName: 'menuPath', componentProps: { placeholder: '/system/xxx' } },
      { component: 'Input', label: '组件', fieldName: 'component', componentProps: { placeholder: 'system/xxx/index' } },
      { component: 'Input', label: '图标', fieldName: 'icon' },
      { component: 'Input', label: '重定向', fieldName: 'redirect' },
      { component: 'Switch', label: '隐藏', fieldName: 'hidden', defaultValue: false },
      { component: 'Switch', label: '缓存', fieldName: 'keepAlive', defaultValue: true },
      { component: 'Switch', label: '总是显示', fieldName: 'alwaysShow', defaultValue: false },
    ];
  }
  if (drawerType.value === 'API') {
    return [
      ...base,
      { component: 'Input', label: 'API 方法', fieldName: 'apiMethod', rules: 'required', componentProps: { placeholder: 'GET/POST' } },
      { component: 'Input', label: 'API 路径', fieldName: 'apiPath', rules: 'required', componentProps: { placeholder: '/api/...' } },
    ];
  }
  return base;
});

const [Form, formApi] = useVbenForm({
  showActionButtonGroup: false,
  schema: schema as any,
});

const [Drawer, drawerApi] = useVbenDrawer({
  onConfirm: async () => {
    await formApi.validate();
    const values = await formApi.getValues();
    const type = drawerType.value;
    try {
      const valid = await validatePermission({
        permCode: values.permCode?.trim() || undefined,
        menuPath: type === 'MENU' ? values.menuPath?.trim() : undefined,
        apiMethod: type === 'API' ? values.apiMethod?.trim() : undefined,
        apiPath: type === 'API' ? values.apiPath?.trim() : undefined,
        permType: type,
        excludeId: isUpdate.value ? recordId.value : undefined,
      });
      if (!valid.valid) {
        message.warning(valid.message ?? '校验不通过');
        return;
      }
    } catch (e: any) {
      message.error(e?.message || e?.msg || '校验失败');
      return;
    }
    try {
      drawerApi.setState({ loading: true });
      const payload: any = {
        permCode: values.permCode?.trim(),
        permName: values.permName?.trim(),
        permType: type,
        parentId: values.parentId === 0 ? undefined : values.parentId,
        sortNo: values.sortNo ?? 0,
        status: values.status ?? 'ENABLED',
      };
      if (type === 'MENU') {
        payload.menuPath = values.menuPath?.trim() || undefined;
        payload.component = values.component?.trim() || undefined;
        payload.icon = values.icon?.trim() || undefined;
        payload.redirect = values.redirect?.trim() || undefined;
        payload.hidden = !!values.hidden;
        payload.keepAlive = values.keepAlive !== false;
        payload.alwaysShow = !!values.alwaysShow;
      } else if (type === 'API') {
        payload.apiMethod = values.apiMethod?.trim() || undefined;
        payload.apiPath = values.apiPath?.trim() || undefined;
      }
      if (isUpdate.value && recordId.value != null) {
        await updatePermission(recordId.value, payload);
        message.success('更新成功');
      } else {
        await createPermission(payload);
        message.success('创建成功');
      }
      drawerApi.close();
      emit('success');
    } catch (e: any) {
      message.error(e?.message || e?.msg || '操作失败');
    } finally {
      drawerApi.setState({ loading: false });
    }
  },
  onOpenChange: async (isOpen: boolean) => {
    if (isOpen) {
      const data = drawerApi.getData<{ drawerType?: 'MENU' | 'BUTTON' | 'API'; parentId?: number; record?: any; isUpdate?: boolean }>();
      drawerType.value = data?.drawerType ?? 'MENU';
      isUpdate.value = !!data?.isUpdate;
      recordId.value = data?.record?.id;
      await loadParentOptions();
      if (isUpdate.value && data?.record) {
        formApi.setValues(data.record);
        drawerApi.setTitle(drawerType.value === 'MENU' ? '编辑菜单' : drawerType.value === 'BUTTON' ? '编辑按钮权限' : '编辑接口权限');
      } else {
        formApi.resetForm();
        formApi.setValues({
          parentId: data?.parentId ?? 0,
          sortNo: 0,
          status: 'ENABLED',
        });
        drawerApi.setTitle(drawerType.value === 'MENU' ? '新增菜单' : drawerType.value === 'BUTTON' ? '新增按钮权限' : '新增接口权限');
      }
    }
  },
});
</script>

<template>
  <Drawer>
    <Form :key="drawerType" />
  </Drawer>
</template>
