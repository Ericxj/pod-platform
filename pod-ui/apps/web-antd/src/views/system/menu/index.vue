<script lang="ts" setup>
import { Page } from '@vben/common-ui';
import { getPermissionTree, createPermission, updatePermission, deletePermission } from '#/api/system/permission';
import type { PermissionTreeDto } from '#/api/system/permission';
import { message, Modal } from 'ant-design-vue';
import { ref, onMounted } from 'vue';
import type { TableColumnType } from 'ant-design-vue';
import MenuDrawer from './menu-drawer.vue';
import { useVbenDrawer } from '@vben/common-ui';
import { usePermission } from '#/composables/usePermission';

const { hasPermission: hasPerm } = usePermission();

const columns: TableColumnType<PermissionTreeDto>[] = [
  { title: '权限码', dataIndex: 'permCode', key: 'permCode', width: 160 },
  { title: '名称', dataIndex: 'permName', key: 'permName', width: 140 },
  { title: '菜单路径', dataIndex: 'menuPath', key: 'menuPath', width: 180 },
  { title: '组件', dataIndex: 'component', key: 'component', width: 180 },
  { title: '排序', dataIndex: 'sortNo', key: 'sortNo', width: 70 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 80 },
  { title: '隐藏', dataIndex: 'hidden', key: 'hidden', width: 70 },
  { title: '缓存', dataIndex: 'keepAlive', key: 'keepAlive', width: 70 },
  { title: '操作', key: 'action', width: 180, fixed: 'right' },
];

const treeData = ref<PermissionTreeDto[]>([]);
const loading = ref(false);
const [Drawer, drawerApi] = useVbenDrawer({ connectedComponent: MenuDrawer });

async function loadMenus() {
  loading.value = true;
  try {
    treeData.value = await getPermissionTree('MENU');
  } finally {
    loading.value = false;
  }
}

function handleAdd(parentId?: number) {
  drawerApi.setData({ parentId: parentId ?? 0 });
  drawerApi.open();
}

function handleEdit(record: PermissionTreeDto) {
  drawerApi.setData({ ...record, isUpdate: true });
  drawerApi.open();
}

function handleDelete(record: PermissionTreeDto) {
  Modal.confirm({
    title: '确认删除',
    content: `确认删除菜单「${record.permName}」吗？`,
    onOk: async () => {
      await deletePermission(record.id);
      message.success('删除成功');
      loadMenus();
    },
  });
}

function handleDrawerSuccess() {
  loadMenus();
}

onMounted(() => loadMenus());
</script>

<template>
  <Page title="菜单管理">
    <div class="mb-4 flex gap-2">
      <a-button v-if="hasPerm('iam:perm:create')" type="primary" @click="handleAdd()">新增菜单</a-button>
    </div>
    <a-table
      :columns="columns"
      :data-source="treeData"
      :loading="loading"
      row-key="id"
      :pagination="false"
      :expandable="{ defaultExpandAllRows: true }"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 'ENABLED' ? 'green' : 'red'">
            {{ record.status === 'ENABLED' ? '启用' : '禁用' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'hidden'">
          {{ record.hidden ? '是' : '否' }}
        </template>
        <template v-else-if="column.key === 'keepAlive'">
          {{ record.keepAlive !== false ? '是' : '否' }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-button v-if="hasPerm('iam:perm:create')" type="link" size="small" @click="handleAdd(record.id)">新增子菜单</a-button>
          <a-button v-if="hasPerm('iam:perm:update')" type="link" size="small" @click="handleEdit(record)">编辑</a-button>
          <a-button v-if="hasPerm('iam:perm:delete')" type="link" size="small" danger @click="handleDelete(record)">删除</a-button>
        </template>
      </template>
    </a-table>
    <Drawer @success="handleDrawerSuccess" />
  </Page>
</template>
