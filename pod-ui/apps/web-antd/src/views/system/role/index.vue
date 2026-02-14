<script lang="ts" setup>
import { ref, onMounted } from 'vue';
import { Page } from '#/components/Page';
import {
  getRolePage,
  getRole,
  createRole,
  updateRole,
  deleteRole,
  getRolePermissions,
  putRolePermissions,
} from '#/api/system/role';
import { getPermissionTree } from '#/api/system/permission';
import type { RoleRecord, RolePageQuery, RoleCreateDto, RoleUpdateDto } from '#/api/system/role';
import type { PermissionTreeDto } from '#/api/system/permission';
import { usePermission } from '#/composables/usePermission';
import { message, Table, Button, Space, Modal, Form, Input, Select, Tag } from 'ant-design-vue';
import type { TreeProps } from 'ant-design-vue';

const { hasPermission: hasPerm } = usePermission();

const loading = ref(false);
const dataSource = ref<RoleRecord[]>([]);
const total = ref(0);
const page = ref(1);
const pageSize = ref(10);
const keyword = ref('');
const status = ref('');

const modalVisible = ref(false);
const modalTitle = ref('新增角色');
const editingRecord = ref<RoleRecord | null>(null);
const formState = ref<Partial<RoleCreateDto>>({});
const saveLoading = ref(false);
const formRef = ref();

const grantVisible = ref(false);
const grantRoleId = ref<number | null>(null);
const grantRoleName = ref('');
const permTreeData = ref<TreeProps['treeData']>([]);
const checkedKeys = ref<number[]>([]);
const grantLoading = ref(false);
const grantSaving = ref(false);

function toAntTree(nodes: PermissionTreeDto[] | undefined): TreeProps['treeData'] {
  if (!nodes?.length) return [];
  return nodes.map((n) => ({
    key: n.id,
    title: `${n.permName} (${n.permCode})`,
    children: toAntTree(n.children),
  }));
}

async function load() {
  loading.value = true;
  try {
    const res = await getRolePage({
      current: page.value,
      size: pageSize.value,
      keyword: keyword.value || undefined,
      status: status.value || undefined,
    } as RolePageQuery);
    const d = (res as any)?.data ?? res;
    dataSource.value = d?.records ?? [];
    total.value = d?.total ?? 0;
  } catch (e) {
    message.error('加载失败');
  } finally {
    loading.value = false;
  }
}

function openCreate() {
  modalTitle.value = '新增角色';
  editingRecord.value = null;
  formState.value = { roleCode: '', roleName: '', roleType: 'BUSINESS', status: 'ENABLED' };
  modalVisible.value = true;
}

async function openEdit(record: RoleRecord) {
  modalTitle.value = '编辑角色';
  editingRecord.value = record;
  formState.value = {
    roleCode: record.roleCode,
    roleName: record.roleName,
    roleType: record.roleType ?? 'BUSINESS',
    status: record.status,
    remark: record.remark,
  };
  modalVisible.value = true;
}

async function handleSave() {
  try {
    await formRef.value?.validate();
  } catch {
    return;
  }
  saveLoading.value = true;
  try {
    if (editingRecord.value?.id) {
      await updateRole(editingRecord.value.id, formState.value as RoleUpdateDto);
      message.success('更新成功');
    } else {
      await createRole(formState.value as RoleCreateDto);
      message.success('创建成功');
    }
    modalVisible.value = false;
    load();
  } catch (e: any) {
    message.error(e?.message || '操作失败');
  } finally {
    saveLoading.value = false;
  }
}

function handleDelete(record: RoleRecord) {
  Modal.confirm({
    title: '确认删除',
    content: `确认删除角色「${record.roleName}」吗？`,
    onOk: async () => {
      await deleteRole(record.id);
      message.success('删除成功');
      load();
    },
  });
}

async function openGrant(record: RoleRecord) {
  grantRoleId.value = record.id;
  grantRoleName.value = record.roleName;
  grantVisible.value = true;
  grantLoading.value = true;
  try {
    const [treeRes, permsRes] = await Promise.all([
      getPermissionTree('ALL'),
      getRolePermissions(record.id),
    ]);
    const tree = (treeRes as any)?.data ?? treeRes;
    const perms = (permsRes as any)?.data ?? permsRes;
    permTreeData.value = toAntTree(Array.isArray(tree) ? tree : []);
    checkedKeys.value = perms?.permIds ?? [];
  } finally {
    grantLoading.value = false;
  }
}

async function handleGrantSave() {
  if (grantRoleId.value == null) return;
  grantSaving.value = true;
  try {
    await putRolePermissions(grantRoleId.value, { permIds: checkedKeys.value });
    message.success('保存成功');
    grantVisible.value = false;
    load();
  } catch (e: any) {
    message.error(e?.message || '保存失败');
  } finally {
    grantSaving.value = false;
  }
}

function onTableChange(pag: any) {
  page.value = pag.current;
  pageSize.value = pag.pageSize;
  load();
}

onMounted(load);
</script>

<template>
  <Page title="角色管理">
    <div class="mb-4 flex flex-wrap gap-2">
      <Input v-model:value="keyword" placeholder="角色编码/名称" style="width: 160px" allow-clear @press-enter="load" />
      <Select v-model:value="status" placeholder="状态" style="width: 120px" allow-clear :options="[{ label: '启用', value: 'ENABLED' }, { label: '禁用', value: 'DISABLED' }]" />
      <Button type="primary" @click="load">查询</Button>
      <Button v-if="hasPerm('iam:role:create')" type="primary" @click="openCreate">新增角色</Button>
    </div>
    <Table
      :columns="[
        { title: '角色编码', dataIndex: 'roleCode', width: 120 },
        { title: '角色名称', dataIndex: 'roleName', width: 120 },
        { title: '类型', dataIndex: 'roleType', width: 90 },
        { title: '状态', dataIndex: 'status', width: 80 },
        { title: '备注', dataIndex: 'remark', ellipsis: true },
        { title: '操作', key: 'action', width: 200, fixed: 'right' },
      ]"
      :data-source="dataSource"
      :loading="loading"
      :pagination="{ current: page, pageSize, total, showSizeChanger: true, showTotal: (t: number) => `共 ${t} 条` }"
      row-key="id"
      @change="onTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'status'">
          <Tag :color="record.status === 'ENABLED' ? 'green' : 'red'">
            {{ record.status === 'ENABLED' ? '启用' : '禁用' }}
          </Tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <Space>
            <Button v-if="hasPerm('iam:role:update')" type="link" size="small" @click="openEdit(record)">编辑</Button>
            <Button v-if="hasPerm('iam:role:grant')" type="link" size="small" @click="openGrant(record)">分配权限</Button>
            <Button v-if="hasPerm('iam:role:delete')" type="link" danger size="small" @click="handleDelete(record)">删除</Button>
          </Space>
        </template>
      </template>
    </Table>
    <Modal v-model:open="modalVisible" :title="modalTitle" :confirm-loading="saveLoading" @ok="handleSave">
      <Form ref="formRef" :model="formState" layout="vertical">
        <Form.Item name="roleCode" label="角色编码" :rules="[{ required: true }]">
          <Input v-model:value="formState.roleCode" :disabled="!!editingRecord" placeholder="唯一编码" />
        </Form.Item>
        <Form.Item name="roleName" label="角色名称" :rules="[{ required: true }]">
          <Input v-model:value="formState.roleName" placeholder="名称" />
        </Form.Item>
        <Form.Item name="roleType" label="类型">
          <Select v-model:value="formState.roleType" :options="[{ label: '系统', value: 'SYSTEM' }, { label: '业务', value: 'BUSINESS' }]" style="width: 100%" />
        </Form.Item>
        <Form.Item name="status" label="状态">
          <Select v-model:value="formState.status" :options="[{ label: '启用', value: 'ENABLED' }, { label: '禁用', value: 'DISABLED' }]" style="width: 100%" />
        </Form.Item>
        <Form.Item name="remark" label="备注">
          <Input v-model:value="formState.remark" placeholder="选填" />
        </Form.Item>
      </Form>
    </Modal>
    <Modal
      v-model:open="grantVisible"
      :title="`分配权限：${grantRoleName}`"
      width="520"
      :confirm-loading="grantSaving"
      @ok="handleGrantSave"
    >
      <a-spin :spinning="grantLoading">
        <a-tree
          v-model:checkedKeys="checkedKeys"
          checkable
          :tree-data="permTreeData"
          :field-names="{ key: 'key', title: 'title', children: 'children' }"
        />
      </a-spin>
    </Modal>
  </Page>
</template>
