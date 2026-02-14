<script lang="ts" setup>
import { ref, onMounted } from 'vue';
import { Page } from '#/components/Page';
import {
  getUserPage,
  getUser,
  createUser,
  updateUser,
  deleteUser,
  resetPassword,
  getUserRoles,
  putUserRoles,
} from '#/api/system/user';
import { getRolePage } from '#/api/system/role';
import type { UserDto, UserPageQuery } from '#/api/system/user';
import type { RoleRecord } from '#/api/system/role';
import { usePermission } from '#/composables/usePermission';
import { message, Table, Button, Space, Modal, Form, Input, Select, Tag } from 'ant-design-vue';

const { hasPermission: hasPerm } = usePermission();

const loading = ref(false);
const dataSource = ref<UserDto[]>([]);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);
const keyword = ref('');
const status = ref('');

const modalVisible = ref(false);
const modalTitle = ref('新增用户');
const editingRecord = ref<UserDto | null>(null);
const formState = ref<Partial<UserDto>>({});
const saveLoading = ref(false);
const formRef = ref();
const roleOptions = ref<RoleRecord[]>([]);

const factoryScopeModalRef = ref<{ open: (userId: number, name: string) => void } | null>(null);

async function loadRoles() {
  try {
    const res = await getRolePage({ current: 1, size: 500 });
    const d = (res as any)?.data ?? res;
    roleOptions.value = d?.records ?? [];
  } catch {
    roleOptions.value = [];
  }
}

async function load() {
  loading.value = true;
  try {
    const res = await getUserPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      username: keyword.value || undefined,
      realName: keyword.value || undefined,
      status: status.value || undefined,
    } as UserPageQuery);
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
  modalTitle.value = '新增用户';
  editingRecord.value = null;
  formState.value = { username: '', realName: '', status: 'ENABLED', roleIds: [] };
  modalVisible.value = true;
}

async function openEdit(record: UserDto) {
  modalTitle.value = '编辑用户';
  editingRecord.value = record;
  try {
    const detail = await getUser(record.id!);
    const d = (detail as any)?.data ?? detail;
    formState.value = { ...d, roleIds: d?.roleIds ?? [] };
  } catch {
    formState.value = { ...record, roleIds: [] };
  }
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
      await updateUser({ ...formState.value!, id: editingRecord.value.id });
      message.success('更新成功');
    } else {
      await createUser(formState.value as UserDto);
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

function handleDelete(row: UserDto) {
  Modal.confirm({
    title: '确认删除',
    content: `确认删除用户 ${row.username} 吗？`,
    onOk: async () => {
      await deleteUser(row.id!);
      message.success('删除成功');
      load();
    },
  });
}

function handleResetPassword(row: UserDto) {
  Modal.confirm({
    title: '重置密码',
    content: `确认重置用户 ${row.username} 的密码为 123456 吗？`,
    onOk: async () => {
      await resetPassword(row.id!, '123456');
      message.success('重置成功');
    },
  });
}

function handleFactoryScope(row: UserDto) {
  factoryScopeModalRef.value?.open(row.id!, row.realName || row.username);
}

function onTableChange(pag: any) {
  pageNum.value = pag.current;
  pageSize.value = pag.pageSize;
  load();
}

onMounted(() => {
  loadRoles();
  load();
});
</script>

<template>
  <Page title="用户管理">
    <div class="mb-4 flex flex-wrap gap-2">
      <Input v-model:value="keyword" placeholder="用户名/姓名" style="width: 160px" allow-clear @press-enter="load" />
      <Select v-model:value="status" placeholder="状态" style="width: 120px" allow-clear :options="[{ label: '启用', value: 'ENABLED' }, { label: '禁用', value: 'DISABLED' }]" />
      <Button type="primary" @click="load">查询</Button>
      <Button v-if="hasPerm('iam:user:create')" type="primary" @click="openCreate">新增用户</Button>
    </div>
    <Table
      :columns="[
        { title: '用户名', dataIndex: 'username', width: 120 },
        { title: '真实姓名', dataIndex: 'realName', width: 100 },
        { title: '手机号', dataIndex: 'phone', width: 120 },
        { title: '状态', dataIndex: 'status', width: 80 },
        { title: '创建时间', dataIndex: 'createdAt', width: 170 },
        { title: '操作', key: 'action', width: 260, fixed: 'right' },
      ]"
      :data-source="dataSource"
      :loading="loading"
      :pagination="{ current: pageNum, pageSize, total, showSizeChanger: true, showTotal: (t: number) => `共 ${t} 条` }"
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
            <Button v-if="hasPerm('iam:user:update')" type="link" size="small" @click="openEdit(record)">编辑</Button>
            <Button v-if="hasPerm('iam:user:update')" type="link" size="small" @click="handleFactoryScope(record)">工厂范围</Button>
            <Button v-if="hasPerm('iam:user:reset_pwd')" type="link" size="small" @click="handleResetPassword(record)">重置密码</Button>
            <Button v-if="hasPerm('iam:user:delete')" type="link" danger size="small" @click="handleDelete(record)">删除</Button>
          </Space>
        </template>
      </template>
    </Table>
    <Modal v-model:open="modalVisible" :title="modalTitle" :confirm-loading="saveLoading" @ok="handleSave">
      <Form ref="formRef" :model="formState" layout="vertical">
        <Form.Item name="username" label="用户名" :rules="[{ required: true }]">
          <Input v-model:value="formState.username" :disabled="!!editingRecord" placeholder="登录名" />
        </Form.Item>
        <Form.Item name="realName" label="真实姓名" :rules="[{ required: true }]">
          <Input v-model:value="formState.realName" placeholder="姓名" />
        </Form.Item>
        <Form.Item name="phone" label="手机号">
          <Input v-model:value="formState.phone" placeholder="选填" />
        </Form.Item>
        <Form.Item name="email" label="邮箱">
          <Input v-model:value="formState.email" placeholder="选填" />
        </Form.Item>
        <Form.Item name="status" label="状态">
          <Select v-model:value="formState.status" :options="[{ label: '启用', value: 'ENABLED' }, { label: '禁用', value: 'DISABLED' }]" style="width: 100%" />
        </Form.Item>
        <Form.Item v-if="!editingRecord" name="password" label="初始密码">
          <Input v-model:value="formState.password" type="password" placeholder="不填默认 123456" />
        </Form.Item>
        <Form.Item name="roleIds" label="角色">
          <Select
            v-model:value="formState.roleIds"
            mode="multiple"
            placeholder="选择角色"
            :options="roleOptions.map(r => ({ label: r.roleName, value: r.id }))"
            style="width: 100%"
          />
        </Form.Item>
      </Form>
    </Modal>
    <FactoryScopeModal ref="factoryScopeModalRef" @success="load" />
  </Page>
</template>
