<script lang="ts" setup>
import { ref, onMounted } from 'vue';
import { Page } from '#/components/Page';
import {
  getSitePage,
  getSite,
  createSite,
  updateSite,
  enableSite,
  disableSite,
  getPlatformList,
} from '#/api/system/sys';
import type { PlatSiteRecord, PlatPlatformRecord, PageResult } from '#/api/system/sys';
import { usePermission } from '#/composables/usePermission';
import { message, Table, Button, Space, Modal, Form, Input, Select } from 'ant-design-vue';

const { hasPermission: hasPerm } = usePermission();

const loading = ref(false);
const dataSource = ref<PlatSiteRecord[]>([]);
const total = ref(0);
const page = ref(1);
const pageSize = ref(10);
const platformCode = ref<string | undefined>();
const status = ref<string | undefined>();
const platformList = ref<PlatPlatformRecord[]>([]);

const modalVisible = ref(false);
const editingRecord = ref<PlatSiteRecord | null>(null);
const formState = ref<Partial<PlatSiteRecord>>({});
const saveLoading = ref(false);
const formRef = ref();

async function loadPlatforms() {
  try {
    const list = await getPlatformList('ENABLED');
    platformList.value = (list as PlatPlatformRecord[]) ?? [];
  } catch {
    platformList.value = [];
  }
}

async function load() {
  loading.value = true;
  try {
    const res = await getSitePage({
      current: page.value,
      size: pageSize.value,
      platformCode: platformCode.value,
      status: status.value,
    });
    const d = (res as unknown as PageResult<PlatSiteRecord>) ?? {};
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
  formState.value = {
    platformCode: platformCode.value || '',
    siteCode: '',
    siteName: '',
    countryCode: '',
    currency: '',
    timezone: '',
    status: 'ENABLED',
  };
  modalVisible.value = true;
}

function openEdit(record: PlatSiteRecord) {
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
      await updateSite(editingRecord.value.id, formState.value!);
      message.success('更新成功');
    } else {
      await createSite(formState.value!);
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

async function handleEnable(record: PlatSiteRecord) {
  if (!record.id) return;
  try {
    await enableSite(record.id);
    message.success('已启用');
    load();
  } catch (e: unknown) {
    const err = e as { message?: string };
    message.error(err?.message || '操作失败');
  }
}

async function handleDisable(record: PlatSiteRecord) {
  if (!record.id) return;
  try {
    await disableSite(record.id);
    message.success('已禁用');
    load();
  } catch (e: unknown) {
    const err = e as { message?: string };
    message.error(err?.message || '操作失败');
  }
}

onMounted(() => {
  loadPlatforms();
  load();
});
</script>

<template>
  <Page title="站点管理">
    <div class="mb-4 flex gap-2">
      <Select
        v-model:value="platformCode"
        placeholder="平台"
        style="width: 140px"
        allow-clear
        :options="platformList.map((p) => ({ label: p.platformName || p.platformCode, value: p.platformCode }))"
      />
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
      <Button v-if="hasPerm('sys:site:create')" type="primary" @click="openCreate">新增</Button>
    </div>
    <Table
      :columns="[
        { title: 'ID', dataIndex: 'id', width: 80 },
        { title: '平台', dataIndex: 'platformCode', width: 100 },
        { title: '站点编码', dataIndex: 'siteCode', width: 80 },
        { title: '站点名称', dataIndex: 'siteName' },
        { title: '国家', dataIndex: 'countryCode', width: 80 },
        { title: '币种', dataIndex: 'currency', width: 80 },
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
            <Button v-if="hasPerm('sys:site:update')" type="link" size="small" @click="openEdit(record)">
              编辑
            </Button>
            <Button
              v-if="hasPerm('sys:site:enable') && record.status === 'DISABLED'"
              type="link"
              size="small"
              @click="handleEnable(record)"
            >
              启用
            </Button>
            <Button
              v-if="hasPerm('sys:site:disable') && record.status === 'ENABLED'"
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
    <Modal v-model:open="modalVisible" :title="editingRecord ? '编辑站点' : '新增站点'" @ok="handleSave">
      <Form ref="formRef" :model="formState" layout="vertical">
        <Form.Item name="platformCode" label="平台编码" :rules="[{ required: true }]">
          <Input v-model:value="formState.platformCode" :disabled="!!editingRecord?.id" placeholder="AMAZON/TEMU/SHEIN" />
        </Form.Item>
        <Form.Item name="siteCode" label="站点编码" :rules="[{ required: true }]">
          <Input v-model:value="formState.siteCode" :disabled="!!editingRecord?.id" placeholder="US/DE/UK" />
        </Form.Item>
        <Form.Item name="siteName" label="站点名称" :rules="[{ required: true }]">
          <Input v-model:value="formState.siteName" placeholder="名称" />
        </Form.Item>
        <Form.Item name="countryCode" label="国家代码">
          <Input v-model:value="formState.countryCode" placeholder="如 US" />
        </Form.Item>
        <Form.Item name="currency" label="币种">
          <Input v-model:value="formState.currency" placeholder="如 USD" />
        </Form.Item>
        <Form.Item name="timezone" label="时区">
          <Input v-model:value="formState.timezone" placeholder="如 America/New_York" />
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
