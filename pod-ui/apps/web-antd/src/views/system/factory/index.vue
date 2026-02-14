<script lang="ts" setup>
import { ref, onMounted } from 'vue';
import { Page } from '#/components/Page';
import { getFactoryPage, getFactoryAll, createFactory, updateFactory, deleteFactory } from '#/api/system/factories';
import type { FactoryRecord, FactoryPageQuery } from '#/api/system/factories';
import { usePermission } from '#/composables/usePermission';
import { message, Table, Button, Space, Modal, Form, Input, Select } from 'ant-design-vue';

const { hasPermission: hasPerm } = usePermission();

const loading = ref(false);
const dataSource = ref<FactoryRecord[]>([]);
const total = ref(0);
const page = ref(1);
const pageSize = ref(10);
const keyword = ref('');
const status = ref('');

const modalVisible = ref(false);
const editingRecord = ref<FactoryRecord | null>(null);
const formState = ref<Partial<FactoryRecord>>({});
const saveLoading = ref(false);
const formRef = ref();

async function load() {
  loading.value = true;
  try {
    const res = await getFactoryPage({
      current: page.value,
      size: pageSize.value,
      keyword: keyword.value || undefined,
      status: status.value || undefined,
    } as FactoryPageQuery);
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
  editingRecord.value = null;
  formState.value = { factoryCode: '', factoryName: '', status: 'ENABLED' };
  modalVisible.value = true;
}

function openEdit(record: FactoryRecord) {
  editingRecord.value = record;
  formState.value = { ...record };
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
      await updateFactory(editingRecord.value.id, formState.value!);
      message.success('更新成功');
    } else {
      await createFactory(formState.value!);
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

function handleDelete(record: FactoryRecord) {
  Modal.confirm({
    title: '确认删除',
    content: `确定删除工厂「${record.factoryName}」吗？`,
    onOk: async () => {
      await deleteFactory(record.id);
      message.success('删除成功');
      load();
    },
  });
}

onMounted(load);
</script>

<template>
  <Page title="工厂管理">
    <div class="mb-4 flex gap-2">
      <Input v-model:value="keyword" placeholder="关键词" style="width: 160px" allow-clear @press-enter="load" />
      <Select v-model:value="status" placeholder="状态" style="width: 120px" allow-clear :options="[{ label: '启用', value: 'ENABLED' }, { label: '禁用', value: 'DISABLED' }]" />
      <Button type="primary" @click="load">查询</Button>
      <Button v-if="hasPerm('iam:factory:create')" type="primary" @click="openCreate">新增</Button>
    </div>
    <Table
      :columns="[
        { title: 'ID', dataIndex: 'id', width: 80 },
        { title: '工厂编码', dataIndex: 'factoryCode' },
        { title: '工厂名称', dataIndex: 'factoryName' },
        { title: '状态', dataIndex: 'status', width: 90 },
        { title: '操作', key: 'action', width: 140, fixed: 'right' },
      ]"
      :data-source="dataSource"
      :loading="loading"
      :pagination="{ current: page, pageSize, total, showSizeChanger: true, showTotal: (t: number) => `共 ${t} 条` }"
      row-key="id"
      @change="(p: any) => { page = p.current; pageSize = p.pageSize; load(); }"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'action'">
          <Space>
            <Button v-if="hasPerm('iam:factory:update')" type="link" size="small" @click="openEdit(record)">编辑</Button>
            <Button v-if="hasPerm('iam:factory:delete')" type="link" danger size="small" @click="handleDelete(record)">删除</Button>
          </Space>
        </template>
      </template>
    </Table>
    <Modal v-model:open="modalVisible" :title="editingRecord ? '编辑工厂' : '新增工厂'" @ok="handleSave">
      <Form ref="formRef" :model="formState" layout="vertical">
        <Form.Item name="factoryCode" label="工厂编码" :rules="[{ required: true }]">
          <Input v-model:value="formState.factoryCode" :disabled="!!editingRecord" placeholder="租户内唯一" />
        </Form.Item>
        <Form.Item name="factoryName" label="工厂名称" :rules="[{ required: true }]">
          <Input v-model:value="formState.factoryName" placeholder="名称" />
        </Form.Item>
        <Form.Item name="status" label="状态">
          <Select v-model:value="formState.status" :options="[{ label: '启用', value: 'ENABLED' }, { label: '禁用', value: 'DISABLED' }]" style="width: 100%" />
        </Form.Item>
      </Form>
    </Modal>
  </Page>
</template>
