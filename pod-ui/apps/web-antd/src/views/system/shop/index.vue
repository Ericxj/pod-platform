<script lang="ts" setup>
import { ref, onMounted, watch } from 'vue';
import { Page } from '#/components/Page';
import {
  getShopPage,
  getShop,
  createShop,
  updateShop,
  enableShop,
  disableShop,
  getPlatformList,
  getSiteList,
} from '#/api/system/sys';
import type { PlatShopRecord, PlatPlatformRecord, PlatSiteRecord, PageResult } from '#/api/system/sys';
import { usePermission } from '#/composables/usePermission';
import { message, Table, Button, Space, Modal, Form, Input, Select } from 'ant-design-vue';

const { hasPermission: hasPerm } = usePermission();

const loading = ref(false);
const dataSource = ref<PlatShopRecord[]>([]);
const total = ref(0);
const page = ref(1);
const pageSize = ref(10);
const platformCode = ref<string | undefined>();
const siteCode = ref<string | undefined>();
const status = ref<string | undefined>();
const platformList = ref<PlatPlatformRecord[]>([]);
const siteList = ref<PlatSiteRecord[]>([]);

const modalVisible = ref(false);
const editingRecord = ref<PlatShopRecord | null>(null);
const formState = ref<Partial<PlatShopRecord>>({});
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

async function loadSites(platform?: string) {
  if (!platform) {
    siteList.value = [];
    return;
  }
  try {
    const list = await getSiteList(platform, 'ENABLED');
    siteList.value = (list as PlatSiteRecord[]) ?? [];
  } catch {
    siteList.value = [];
  }
}

watch(
  () => formState.value?.platformCode,
  (code) => {
    if (code) loadSites(code);
    else siteList.value = [];
  },
);

async function load() {
  loading.value = true;
  try {
    const res = await getShopPage({
      current: page.value,
      size: pageSize.value,
      platformCode: platformCode.value,
      siteCode: siteCode.value,
      status: status.value,
    });
    const d = (res as unknown as PageResult<PlatShopRecord>) ?? {};
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
    shopCode: '',
    shopName: '',
    siteCode: siteCode.value || undefined,
    currency: '',
    status: 'ENABLED',
  };
  if (formState.value.platformCode) loadSites(formState.value.platformCode);
  modalVisible.value = true;
}

function openEdit(record: PlatShopRecord) {
  editingRecord.value = record;
  formState.value = { ...record };
  if (record.platformCode) loadSites(record.platformCode);
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
      await updateShop(editingRecord.value.id, formState.value!);
      message.success('更新成功');
    } else {
      await createShop(formState.value!);
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

async function handleEnable(record: PlatShopRecord) {
  if (!record.id) return;
  try {
    await enableShop(record.id);
    message.success('已启用');
    load();
  } catch (e: unknown) {
    const err = e as { message?: string };
    message.error(err?.message || '操作失败');
  }
}

async function handleDisable(record: PlatShopRecord) {
  if (!record.id) return;
  try {
    await disableShop(record.id);
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
  <Page title="店铺管理">
    <div class="mb-4 flex gap-2">
      <Select
        v-model:value="platformCode"
        placeholder="平台"
        style="width: 140px"
        allow-clear
        :options="platformList.map((p) => ({ label: p.platformName || p.platformCode, value: p.platformCode }))"
      />
      <Select
        v-model:value="siteCode"
        placeholder="站点"
        style="width: 120px"
        allow-clear
        :options="siteList.map((s) => ({ label: s.siteName || s.siteCode, value: s.siteCode }))"
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
      <Button v-if="hasPerm('sys:shop:create')" type="primary" @click="openCreate">新增</Button>
    </div>
    <Table
      :columns="[
        { title: 'ID', dataIndex: 'id', width: 80 },
        { title: '平台', dataIndex: 'platformCode', width: 100 },
        { title: '店铺编码', dataIndex: 'shopCode', width: 120 },
        { title: '店铺名称', dataIndex: 'shopName' },
        { title: '站点', dataIndex: 'siteCode', width: 80 },
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
            <Button v-if="hasPerm('sys:shop:update')" type="link" size="small" @click="openEdit(record)">
              编辑
            </Button>
            <Button
              v-if="hasPerm('sys:shop:enable') && record.status === 'DISABLED'"
              type="link"
              size="small"
              @click="handleEnable(record)"
            >
              启用
            </Button>
            <Button
              v-if="hasPerm('sys:shop:disable') && record.status === 'ENABLED'"
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
    <Modal v-model:open="modalVisible" :title="editingRecord ? '编辑店铺' : '新增店铺'" @ok="handleSave">
      <Form ref="formRef" :model="formState" layout="vertical">
        <Form.Item name="platformCode" label="平台编码" :rules="[{ required: true }]">
          <Select
            v-model:value="formState.platformCode"
            :disabled="!!editingRecord?.id"
            placeholder="请选择平台"
            :options="platformList.map((p) => ({ label: p.platformName || p.platformCode, value: p.platformCode }))"
            style="width: 100%"
            @change="() => { formState.siteCode = undefined; loadSites(formState.platformCode); }"
          />
        </Form.Item>
        <Form.Item name="shopCode" label="店铺编码" :rules="[{ required: true }]">
          <Input v-model:value="formState.shopCode" :disabled="!!editingRecord?.id" placeholder="租户+平台内唯一" />
        </Form.Item>
        <Form.Item name="shopName" label="店铺名称" :rules="[{ required: true }]">
          <Input v-model:value="formState.shopName" placeholder="名称" />
        </Form.Item>
        <Form.Item name="siteCode" label="站点（可选）">
          <Select
            v-model:value="formState.siteCode"
            placeholder="可选，从站点表选择"
            allow-clear
            :options="siteList.map((s) => ({ label: s.siteCode + ' - ' + s.siteName, value: s.siteCode }))"
            style="width: 100%"
          />
        </Form.Item>
        <Form.Item name="currency" label="币种">
          <Input v-model:value="formState.currency" placeholder="如 USD" />
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
