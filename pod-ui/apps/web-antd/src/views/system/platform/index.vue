<script lang="ts" setup>
import { ref, onMounted } from 'vue';
import { Page } from '#/components/Page';
import {
  getPlatformPage,
  getPlatform,
  createPlatform,
  updatePlatform,
  enablePlatform,
  disablePlatform,
} from '#/api/system/sys';
import type { PlatPlatformRecord, PageResult } from '#/api/system/sys';
import { usePermission } from '#/composables/usePermission';
import { message, Table, Button, Space, Modal, Form, Input, Select } from 'ant-design-vue';

const { hasPermission: hasPerm } = usePermission();

const loading = ref(false);
const dataSource = ref<PlatPlatformRecord[]>([]);
const total = ref(0);
const page = ref(1);
const pageSize = ref(10);
const status = ref<string | undefined>();

const modalVisible = ref(false);
const editingRecord = ref<PlatPlatformRecord | null>(null);
const formState = ref<Partial<PlatPlatformRecord>>({});
const saveLoading = ref(false);
const formRef = ref();

async function load() {
  loading.value = true;
  try {
    const res = await getPlatformPage({
      current: page.value,
      size: pageSize.value,
      status: status.value,
    });
    const d = (res as unknown as PageResult<PlatPlatformRecord>) ?? {};
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
  formState.value = { platformCode: '', platformName: '', status: 'ENABLED' };
  modalVisible.value = true;
}

function openEdit(record: PlatPlatformRecord) {
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
      await updatePlatform(editingRecord.value.id, formState.value!);
      message.success('更新成功');
    } else {
      await createPlatform(formState.value!);
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

async function handleEnable(record: PlatPlatformRecord) {
  if (!record.id) return;
  try {
    await enablePlatform(record.id);
    message.success('已启用');
    load();
  } catch (e: unknown) {
    const err = e as { message?: string };
    message.error(err?.message || '操作失败');
  }
}

async function handleDisable(record: PlatPlatformRecord) {
  if (!record.id) return;
  try {
    await disablePlatform(record.id);
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
  <Page title="平台管理">
    <div class="mb-4 flex gap-2">
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
      <Button v-if="hasPerm('sys:platform:create')" type="primary" @click="openCreate">新增</Button>
    </div>
    <Table
      :columns="[
        { title: 'ID', dataIndex: 'id', width: 80 },
        { title: '平台编码', dataIndex: 'platformCode', width: 120 },
        { title: '平台名称', dataIndex: 'platformName' },
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
            <Button v-if="hasPerm('sys:platform:update')" type="link" size="small" @click="openEdit(record)">
              编辑
            </Button>
            <Button
              v-if="hasPerm('sys:platform:enable') && record.status === 'DISABLED'"
              type="link"
              size="small"
              @click="handleEnable(record)"
            >
              启用
            </Button>
            <Button
              v-if="hasPerm('sys:platform:disable') && record.status === 'ENABLED'"
              type="link"
              danger
              size="small"
              @click="handleDisable(record)"
            >
              禁用
            </Button>
          </Space>
        </template>
      </template>
    </Table>
    <Modal v-model:open="modalVisible" :title="editingRecord ? '编辑平台' : '新增平台'" @ok="handleSave">
      <Form ref="formRef" :model="formState" layout="vertical">
        <Form.Item name="platformCode" label="平台编码" :rules="[{ required: true }]">
          <Input
            v-model:value="formState.platformCode"
            :disabled="!!editingRecord?.id"
            placeholder="如 AMAZON/TEMU/SHEIN"
          />
        </Form.Item>
        <Form.Item name="platformName" label="平台名称" :rules="[{ required: true }]">
          <Input v-model:value="formState.platformName" placeholder="名称" />
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
      </Form>
    </Modal>
  </Page>
</template>
