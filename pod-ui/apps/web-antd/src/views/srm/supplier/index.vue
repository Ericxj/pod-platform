<script lang="ts" setup>
import { ref, onMounted } from 'vue';
import { Page } from '#/components/Page';
import {
  getSupplierPage,
  getSupplier,
  createSupplier,
  updateSupplier,
  enableSupplier,
  disableSupplier,
} from '#/api/srm';
import type { SupplierRecord, PageResult } from '#/api/srm';
import { usePermission } from '#/composables/usePermission';
import { message, Table, Button, Space, Modal, Form, Input, Select } from 'ant-design-vue';

const { hasPermission: hasPerm } = usePermission();

const loading = ref(false);
const dataSource = ref<SupplierRecord[]>([]);
const total = ref(0);
const page = ref(1);
const pageSize = ref(10);
const keyword = ref('');
const status = ref<string | undefined>();

const modalVisible = ref(false);
const editingRecord = ref<SupplierRecord | null>(null);
const formState = ref<Partial<SupplierRecord>>({});
const saveLoading = ref(false);
const formRef = ref();

async function load() {
  loading.value = true;
  try {
    const res = await getSupplierPage({
      current: page.value,
      size: pageSize.value,
      keyword: keyword.value || undefined,
      status: status.value,
    });
    const d = (res as unknown as PageResult<SupplierRecord>) ?? {};
    dataSource.value = d.records ?? [];
    total.value = d.total ?? 0;
  } catch (e) {
    message.error('加载失败');
  } finally {
    loading.value = false;
  }
}

function openCreate() {
  editingRecord.value = null;
  formState.value = { supplierCode: '', supplierName: '', status: 'ENABLED' };
  modalVisible.value = true;
}

function openEdit(record: SupplierRecord) {
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
      await updateSupplier(editingRecord.value.id, formState.value!);
      message.success('更新成功');
    } else {
      await createSupplier(formState.value!);
      message.success('创建成功');
    }
    modalVisible.value = false;
    load();
  } catch (e: unknown) {
    const err = e as { message?: string };
    message.error(err?.message || '操作失败');
  } finally {
    saveLoading.value = false;
  }
}

async function handleEnable(record: SupplierRecord) {
  if (!record.id) return;
  try {
    await enableSupplier(record.id);
    message.success('已启用');
    load();
  } catch (e: unknown) {
    const err = e as { message?: string };
    message.error(err?.message || '操作失败');
  }
}

async function handleDisable(record: SupplierRecord) {
  if (!record.id) return;
  try {
    await disableSupplier(record.id);
    message.success('已禁用');
    load();
  } catch (e: unknown) {
    const err = e as { message?: string };
    message.error(err?.message || '操作失败');
  }
}

onMounted(load);
</script>

<template>
  <Page title="供应商管理">
    <div class="mb-4 flex gap-2">
      <Input v-model:value="keyword" placeholder="编码/名称" style="width: 160px" allow-clear @press-enter="load" />
      <Select
        v-model:value="status"
        placeholder="状态"
        style="width: 120px"
        allow-clear
        :options="[
          { label: '启用', value: 'ENABLED' },
          { label: '禁用', value: 'DISABLED' },
        ]"
      />
      <Button type="primary" @click="load">查询</Button>
      <Button v-if="hasPerm('srm:supplier:create')" type="primary" @click="openCreate">新增</Button>
    </div>
    <Table
      :columns="[
        { title: 'ID', dataIndex: 'id', width: 80 },
        { title: '供应商编码', dataIndex: 'supplierCode', width: 120 },
        { title: '供应商名称', dataIndex: 'supplierName' },
        { title: '联系人', dataIndex: 'contactName', width: 100 },
        { title: '电话', dataIndex: 'phone', width: 120 },
        { title: '状态', dataIndex: 'status', width: 90 },
        { title: '操作', key: 'action', width: 200, fixed: 'right' },
      ]"
      :data-source="dataSource"
      :loading="loading"
      :pagination="{
        current: page,
        pageSize,
        total,
        showSizeChanger: true,
        showTotal: (t: number) => `共 ${t} 条`,
      }"
      row-key="id"
      @change="(p: { current: number; pageSize: number }) => {
        page = p.current;
        pageSize = p.pageSize;
        load();
      }"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'action'">
          <Space>
            <Button v-if="hasPerm('srm:supplier:update')" type="link" size="small" @click="openEdit(record)">编辑</Button>
            <Button v-if="hasPerm('srm:supplier:enable') && record.status === 'DISABLED'" type="link" size="small" @click="handleEnable(record)">启用</Button>
            <Button v-if="hasPerm('srm:supplier:disable') && record.status === 'ENABLED'" type="link" danger size="small" @click="handleDisable(record)">禁用</Button>
          </Space>
        </template>
      </template>
    </Table>
    <Modal v-model:open="modalVisible" :title="editingRecord ? '编辑供应商' : '新增供应商'" @ok="handleSave">
      <Form ref="formRef" :model="formState" layout="vertical">
        <Form.Item name="supplierCode" label="供应商编码" :rules="[{ required: true }]">
          <Input v-model:value="formState.supplierCode" :disabled="!!editingRecord?.id" placeholder="唯一" />
        </Form.Item>
        <Form.Item name="supplierName" label="供应商名称" :rules="[{ required: true }]">
          <Input v-model:value="formState.supplierName" placeholder="名称" />
        </Form.Item>
        <Form.Item name="contactName" label="联系人">
          <Input v-model:value="formState.contactName" />
        </Form.Item>
        <Form.Item name="phone" label="电话">
          <Input v-model:value="formState.phone" />
        </Form.Item>
        <Form.Item name="email" label="邮箱">
          <Input v-model:value="formState.email" />
        </Form.Item>
        <Form.Item name="address" label="地址">
          <Input.TextArea v-model:value="formState.address" :rows="2" />
        </Form.Item>
        <Form.Item name="status" label="状态">
          <Select
            v-model:value="formState.status"
            :options="[
              { label: '启用', value: 'ENABLED' },
              { label: '禁用', value: 'DISABLED' },
            ]"
            style="width: 100%"
          />
        </Form.Item>
        <Form.Item name="remark" label="备注">
          <Input.TextArea v-model:value="formState.remark" :rows="2" />
        </Form.Item>
      </Form>
    </Modal>
  </Page>
</template>
