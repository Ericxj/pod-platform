<script lang="ts" setup>
import { ref, onMounted, watch } from 'vue';
import { Page } from '#/components/Page';
import {
  getCredentialPage,
  getCredential,
  createCredential,
  updateCredential,
  enableCredential,
  disableCredential,
  testCredential,
  getPlatformList,
  getShopList,
} from '#/api/system/sys';
import type {
  CredentialRecord,
  CredentialCreateDto,
  PlatPlatformRecord,
  PlatShopRecord,
  PageResult,
} from '#/api/system/sys';
import { usePermission } from '#/composables/usePermission';
import { message, Table, Button, Space, Modal, Form, Input, Select } from 'ant-design-vue';

const { hasPermission: hasPerm } = usePermission();

const loading = ref(false);
const dataSource = ref<CredentialRecord[]>([]);
const total = ref(0);
const page = ref(1);
const pageSize = ref(10);
const platformCode = ref<string | undefined>();
const shopId = ref<number | undefined>();
const platformList = ref<PlatPlatformRecord[]>([]);
const shopList = ref<PlatShopRecord[]>([]);

const modalVisible = ref(false);
const editingRecord = ref<CredentialRecord | null>(null);
const formState = ref<Partial<CredentialCreateDto>>({});
const saveLoading = ref(false);
const testLoading = ref(false);
const formRef = ref();

async function loadPlatforms() {
  try {
    const list = await getPlatformList('ENABLED');
    platformList.value = (list as PlatPlatformRecord[]) ?? [];
  } catch {
    platformList.value = [];
  }
}

async function loadShops(platform?: string) {
  if (!platform) {
    shopList.value = [];
    return;
  }
  try {
    const list = await getShopList(platform, undefined, 'ENABLED');
    shopList.value = (list as PlatShopRecord[]) ?? [];
  } catch {
    shopList.value = [];
  }
}

async function load() {
  loading.value = true;
  try {
    const res = await getCredentialPage({
      current: page.value,
      size: pageSize.value,
      platformCode: platformCode.value,
      shopId: shopId.value,
    });
    const d = (res as unknown as PageResult<CredentialRecord>) ?? {};
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
    shopId: shopId.value ?? undefined,
    authType: 'OAUTH',
    credentialName: '',
    payloadPlainJson: '{}',
    status: 'ENABLED',
  };
  if (formState.value.platformCode) loadShops(formState.value.platformCode);
  modalVisible.value = true;
}

async function openEdit(record: CredentialRecord) {
  editingRecord.value = record;
  formState.value = {
    platformCode: record.platformCode,
    shopId: record.shopId,
    authType: record.authType,
    credentialName: record.credentialName,
    payloadPlainJson: '{}',
    status: record.status,
  };
  if (record.platformCode) loadShops(record.platformCode);
  modalVisible.value = true;
}

async function handleSave() {
  try {
    await formRef.value?.validate();
  } catch {
    return;
  }
  if (!formState.value.platformCode || !formState.value.shopId) {
    message.error('请选择平台和店铺');
    return;
  }
  saveLoading.value = true;
  try {
    if (editingRecord.value?.id) {
      await updateCredential(editingRecord.value.id, {
        credentialName: formState.value.credentialName,
        payloadPlainJson: formState.value.payloadPlainJson || '{}',
        status: formState.value.status,
      });
      message.success('更新成功');
    } else {
      await createCredential({
        platformCode: formState.value.platformCode,
        shopId: formState.value.shopId,
        authType: formState.value.authType || 'OAUTH',
        credentialName: formState.value.credentialName,
        payloadPlainJson: formState.value.payloadPlainJson || '{}',
        status: formState.value.status,
      });
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

async function handleEnable(record: CredentialRecord) {
  if (!record.id) return;
  try {
    await enableCredential(record.id);
    message.success('已启用');
    load();
  } catch (e: unknown) {
    const err = e as { message?: string };
    message.error(err?.message || '操作失败');
  }
}

async function handleDisable(record: CredentialRecord) {
  if (!record.id) return;
  try {
    await disableCredential(record.id);
    message.success('已禁用');
    load();
  } catch (e: unknown) {
    const err = e as { message?: string };
    message.error(err?.message || '操作失败');
  }
}

async function handleTest(record: CredentialRecord) {
  if (!record.id) return;
  testLoading.value = true;
  try {
    const ok = await testCredential(record.id);
    if (ok) message.success('连接成功');
    else message.warning('连接失败');
  } catch (e: unknown) {
    const err = e as { message?: string };
    message.error(err?.message || '测试失败');
  } finally {
    testLoading.value = false;
  }
}

watch(platformCode, (v) => {
  if (v) loadShops(v);
  else {
    shopList.value = [];
    shopId.value = undefined;
  }
});

onMounted(() => {
  loadPlatforms();
  load();
});
</script>

<template>
  <Page title="平台授权">
    <div class="mb-4 flex gap-2">
      <Select
        v-model:value="platformCode"
        placeholder="平台"
        style="width: 140px"
        allow-clear
        :options="platformList.map((p) => ({ label: p.platformName || p.platformCode, value: p.platformCode }))"
      />
      <Select
        v-model:value="shopId"
        placeholder="店铺"
        style="width: 180px"
        allow-clear
        :options="shopList.map((s) => ({ label: `${s.shopCode} - ${s.shopName}`, value: s.id }))"
      />
      <Button type="primary" @click="load">查询</Button>
      <Button v-if="hasPerm('sys:credential:create')" type="primary" @click="openCreate">新增</Button>
    </div>
    <Table
      :columns="[
        { title: 'ID', dataIndex: 'id', width: 80 },
        { title: '平台', dataIndex: 'platformCode', width: 100 },
        { title: '店铺ID', dataIndex: 'shopId', width: 90 },
        { title: '授权名称', dataIndex: 'credentialName' },
        { title: '认证类型', dataIndex: 'authType', width: 90 },
        { title: '凭证', dataIndex: 'payloadMasked', width: 120, ellipsis: true },
        { title: '状态', dataIndex: 'status', width: 90 },
        { title: '操作', key: 'action', width: 260, fixed: 'right' },
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
            <Button v-if="hasPerm('sys:credential:update')" type="link" size="small" @click="openEdit(record)">
              编辑
            </Button>
            <Button
              v-if="hasPerm('sys:credential:test')"
              type="link"
              size="small"
              :loading="testLoading"
              @click="handleTest(record)"
            >
              测试连接
            </Button>
            <Button
              v-if="hasPerm('sys:credential:enable') && record.status === 'DISABLED'"
              type="link"
              size="small"
              @click="handleEnable(record)"
            >
              启用
            </Button>
            <Button
              v-if="hasPerm('sys:credential:disable') && record.status === 'ENABLED'"
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
    <Modal v-model:open="modalVisible" :title="editingRecord ? '编辑授权' : '新增授权'" @ok="handleSave">
      <Form ref="formRef" :model="formState" layout="vertical">
        <Form.Item name="platformCode" label="平台" :rules="[{ required: true }]">
          <Select
            v-model:value="formState.platformCode"
            :disabled="!!editingRecord?.id"
            placeholder="请选择平台"
            :options="platformList.map((p) => ({ label: p.platformName || p.platformCode, value: p.platformCode }))"
            style="width: 100%"
            @change="(v: string) => { formState.shopId = undefined; loadShops(v); }"
          />
        </Form.Item>
        <Form.Item name="shopId" label="店铺" :rules="[{ required: true }]">
          <Select
            v-model:value="formState.shopId"
            :disabled="!!editingRecord?.id"
            placeholder="请选择店铺"
            :options="shopList.map((s) => ({ label: `${s.shopCode} - ${s.shopName}`, value: s.id }))"
            style="width: 100%"
          />
        </Form.Item>
        <Form.Item name="authType" label="认证类型">
          <Select
            v-model:value="formState.authType"
            :options="[
              { label: 'OAuth', value: 'OAUTH' },
              { label: 'Token', value: 'TOKEN' },
            ]"
            style="width: 100%"
          />
        </Form.Item>
        <Form.Item name="credentialName" label="授权名称（展示用）">
          <Input v-model:value="formState.credentialName" placeholder="如：主账号授权" />
        </Form.Item>
        <Form.Item name="payloadPlainJson" label="凭证 JSON（明文，提交后后端加密存储）" :rules="[{ required: true }]">
          <Input.TextArea
            v-model:value="formState.payloadPlainJson"
            placeholder='如 {"access_token":"xxx","refresh_token":"yyy"}，编辑时不回显敏感内容'
            :rows="4"
          />
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
