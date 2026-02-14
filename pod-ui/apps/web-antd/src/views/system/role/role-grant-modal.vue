<script lang="ts" setup>
import { useVbenDrawer } from '@vben/common-ui';
import { getPermissionTree } from '#/api/system/permission';
import { getRolePermissions, grantRolePermissions } from '#/api/system/role';
import type { PermissionTreeDto } from '#/api/system/permission';
import { message } from 'ant-design-vue';
import { ref } from 'vue';
import type { TreeProps } from 'ant-design-vue';

const emit = defineEmits(['success']);
const [Drawer, drawerApi] = useVbenDrawer({ showConfirmButton: true });

const roleId = ref<number | null>(null);
const treeData = ref<TreeProps['treeData']>([]);
const checkedKeys = ref<number[]>([]);
const loading = ref(false);

function toAntTree(nodes: PermissionTreeDto[] | undefined): TreeProps['treeData'] {
  if (!nodes?.length) return [];
  return nodes.map((n) => ({
    key: n.id,
    title: `${n.permName} (${n.permCode})`,
    children: toAntTree(n.children),
  }));
}

async function loadData() {
  const data = drawerApi.getData<{ roleId?: number; roleName?: string }>();
  if (data?.roleId == null) return;
  roleId.value = data.roleId;
  loading.value = true;
  try {
    const [list, perms] = await Promise.all([getPermissionTree('ALL'), getRolePermissions(data.roleId)]);
    treeData.value = toAntTree(list);
    checkedKeys.value = perms.permIds ?? [];
  } finally {
    loading.value = false;
  }
}

drawerApi.setOnOpenChange((open: boolean) => {
  if (open) {
    const data = drawerApi.getData<{ roleId?: number; roleName?: string }>();
    drawerApi.setTitle(`分配权限：${data?.roleName ?? ''}`);
    loadData();
  }
});

drawerApi.setOnConfirm(async () => {
  if (roleId.value == null) return;
  try {
    drawerApi.setState({ loading: true });
    await grantRolePermissions(roleId.value, { permIds: checkedKeys.value });
    message.success('保存成功');
    emit('success');
    drawerApi.close();
  } finally {
    drawerApi.setState({ loading: false });
  }
});
</script>

<template>
  <Drawer>
    <a-spin :spinning="loading">
      <a-tree
        v-model:checkedKeys="checkedKeys"
        checkable
        :tree-data="treeData"
        :field-names="{ key: 'key', title: 'title', children: 'children' }"
      />
    </a-spin>
  </Drawer>
</template>
