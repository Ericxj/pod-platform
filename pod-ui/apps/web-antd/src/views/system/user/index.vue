<script lang="ts" setup>
import type { VxeGridProps } from '#/adapter/vxe-table';
import { Page, useVbenVxeGrid } from '#/adapter/vxe-table';
import { getUserPage, deleteUser, resetPassword } from '#/api/system/user';
import { message, Modal } from 'ant-design-vue';
import { useVbenDrawer } from '@vben/common-ui';
import UserDrawer from './user-drawer.vue';

const gridOptions: VxeGridProps = {
  columns: [
    { field: 'username', title: '用户名' },
    { field: 'realName', title: '真实姓名' },
    { field: 'phone', title: '手机号' },
    { field: 'status', title: '状态', slots: { default: 'status' } },
    { field: 'createdAt', title: '创建时间' },
    { title: '操作', slots: { default: 'action' }, width: 250, fixed: 'right' },
  ],
  proxyConfig: {
    ajax: {
      query: async ({ page, form }) => {
        const res = await getUserPage({
          pageNum: page.currentPage,
          pageSize: page.pageSize,
          ...form,
        });
        return {
            result: res.records,
            total: res.total
        };
      },
    },
  },
  toolbarConfig: {
      refresh: true,
      custom: true,
      slots: { buttons: 'toolbar-buttons' }
  },
  formConfig: {
      items: [
          { field: 'username', title: '用户名', itemRender: { name: 'AInput' } },
          { field: 'realName', title: '真实姓名', itemRender: { name: 'AInput' } },
      ]
  }
};

const [Grid, gridApi] = useVbenVxeGrid({ gridOptions });
const [Drawer, drawerApi] = useVbenDrawer({
    connectedComponent: UserDrawer,
});

function handleCreate() {
    drawerApi.setData({});
    drawerApi.open();
}

function handleEdit(row: any) {
    drawerApi.setData({ ...row });
    drawerApi.open();
}

function handleDelete(row: any) {
    Modal.confirm({
        title: '确认删除',
        content: `确认删除用户 ${row.username} 吗？`,
        onOk: async () => {
            await deleteUser(row.id);
            message.success('删除成功');
            gridApi.reload();
        }
    });
}

function handleResetPassword(row: any) {
    Modal.confirm({
        title: '重置密码',
        content: `确认重置用户 ${row.username} 的密码为 123456 吗？`,
        onOk: async () => {
            await resetPassword(row.id, '123456');
            message.success('重置成功');
        }
    });
}

function handleDrawerSuccess() {
    gridApi.reload();
}
</script>

<template>
  <Page title="用户管理">
    <Grid>
      <template #toolbar-buttons>
          <a-button type="primary" @click="handleCreate">新增用户</a-button>
      </template>
      
      <template #status="{ row }">
          <a-tag :color="row.status === 'ENABLED' ? 'green' : 'red'">
              {{ row.status === 'ENABLED' ? '启用' : '禁用' }}
          </a-tag>
      </template>

      <template #action="{ row }">
        <a-button type="link" @click="handleEdit(row)">编辑</a-button>
        <a-button type="link" @click="handleResetPassword(row)">重置密码</a-button>
        <a-button type="link" danger @click="handleDelete(row)">删除</a-button>
      </template>
    </Grid>
    <Drawer @success="handleDrawerSuccess" />
  </Page>
</template>
