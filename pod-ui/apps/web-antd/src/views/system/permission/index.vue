<script lang="ts" setup>
import { Page } from '#/components/Page';
import {
  getPermissionTree,
  deletePermission,
} from '#/api/system/permission';
import type { PermissionTreeDto } from '#/api/system/permission';
import { message, Modal } from 'ant-design-vue';
import { ref, onMounted, watch } from 'vue';
import type { TreeProps } from 'ant-design-vue';
import PermissionDrawer from './permission-drawer.vue';
import { useVbenDrawer } from '@vben/common-ui';
import { usePermission } from '#/composables/usePermission';

const { hasPermission: hasPerm } = usePermission();
const permType = ref<'ALL' | 'MENU' | 'BUTTON' | 'API'>('ALL');
const treeData = ref<PermissionTreeDto[]>([]);
const loading = ref(false);
const [Drawer, drawerApi] = useVbenDrawer({ connectedComponent: PermissionDrawer });

async function loadTree() {
  loading.value = true;
  try {
    treeData.value = await getPermissionTree(permType.value);
  } finally {
    loading.value = false;
  }
}

function toAntTree(nodes: PermissionTreeDto[] | undefined): TreeProps['treeData'] {
  if (!nodes?.length) return [];
  return nodes.map((n) => ({
    key: n.id,
    title: `[${n.permType}] ${n.permName} (${n.permCode})`,
    children: toAntTree(n.children),
  }));
}

function findRecord(nodes: PermissionTreeDto[] | undefined, id: number): PermissionTreeDto | null {
  if (!nodes?.length) return null;
  for (const n of nodes) {
    if (n.id === id) return n;
    const found = findRecord(n.children, id);
    if (found) return found;
  }
  return null;
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
    content: `确认删除权限「${record.permName}」吗？`,
    onOk: async () => {
      await deletePermission(record.id);
      message.success('删除成功');
      loadTree();
    },
  });
}

function handleEditByKey(key: number) {
  const r = findRecord(treeData.value, key);
  if (r) handleEdit(r);
}

function handleDeleteByKey(key: number) {
  const r = findRecord(treeData.value, key);
  if (r) handleDelete(r);
}

function handleDrawerSuccess() {
  loadTree();
}

watch(permType, () => loadTree());
onMounted(() => loadTree());
</script>

<template>
  <Page title="权限点管理">
    <div class="mb-4 flex gap-2">
      <a-radio-group v-model:value="permType" @change="loadTree">
        <a-radio-button value="ALL">全部</a-radio-button>
        <a-radio-button value="MENU">菜单</a-radio-button>
        <a-radio-button value="BUTTON">按钮</a-radio-button>
        <a-radio-button value="API">API</a-radio-button>
      </a-radio-group>
      <a-button v-if="hasPerm('iam:perm:create')" type="primary" @click="handleAdd()">新增</a-button>
    </div>
    <a-spin :spinning="loading">
      <a-tree
        :tree-data="toAntTree(treeData)"
        :field-names="{ key: 'key', title: 'title', children: 'children' }"
        block-node
        default-expand-all
      >
        <template #title="{ title, key, node }">
          <span class="flex items-center justify-between gap-4">
            <span>{{ title }}</span>
            <span>
              <a-button v-if="hasPerm('iam:perm:create')" type="link" size="small" @click.stop="handleAdd(Number(key))">添加子节点</a-button>
              <a-button v-if="hasPerm('iam:perm:update')" type="link" size="small" @click.stop="handleEditByKey(Number(key))">编辑</a-button>
              <a-button v-if="hasPerm('iam:perm:delete')" type="link" size="small" danger @click.stop="handleDeleteByKey(Number(key))">删除</a-button>
            </span>
          </span>
        </template>
      </a-tree>
    </a-spin>
    <Drawer @success="handleDrawerSuccess" />
  </Page>
</template>
