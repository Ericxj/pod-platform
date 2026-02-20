<script lang="ts" setup>
import { ref, onMounted } from 'vue';
import { Page } from '#/components/Page';
import {
  getFxRatePage,
  getFxRate,
  createFxRate,
  updateFxRate,
  enableFxRate,
  disableFxRate,
  getFxQuote,
} from '#/api/system/sys';
import type { FxRateRecord, PageResult } from '#/api/system/sys';
import { usePermission } from '#/composables/usePermission';
import { message, Table, Button, Space, Modal, Form, Input, Select, Card } from 'ant-design-vue';

const { hasPermission: hasPerm } = usePermission();

const loading = ref(false);
const dataSource = ref<FxRateRecord[]>([]);
const total = ref(0);
const page = ref(1);
const pageSize = ref(10);
const baseCurrency = ref<string | undefined>();
const quoteCurrency = ref<string | undefined>();
const status = ref<string | undefined>();

const modalVisible = ref(false);
const editingRecord = ref<FxRateRecord | null>(null);
const formState = ref<Partial<FxRateRecord>>({});
const saveLoading = ref(false);
const formRef = ref();

const quoteBase = ref('USD');
const quoteQuote = ref('CNY');
const quoteDate = ref('');
const quoteResult = ref<number | null>(null);
const quoteLoading = ref(false);

async function load() {
  loading.value = true;
  try {
    const res = await getFxRatePage({
      current: page.value,
      size: pageSize.value,
      baseCurrency: baseCurrency.value,
      quoteCurrency: quoteCurrency.value,
      status: status.value,
    });
    const d = (res as unknown as PageResult<FxRateRecord>) ?? {};
    dataSource.value = d.records ?? [];
    total.value = d.total ?? 0;
  } catch (e) {
    message.error('加载失败');
  } finally {
    loading.value = false;
  }
}

async function handleQuote() {
  if (!quoteBase.value || !quoteQuote.value) {
    message.warning('请填写基准币种与报价币种');
    return;
  }
  quoteLoading.value = true;
  quoteResult.value = null;
  try {
    const rate = await getFxQuote(quoteBase.value, quoteQuote.value, quoteDate.value || undefined);
    quoteResult.value = rate as number;
  } catch (e: unknown) {
    const err = e as { message?: string };
    message.error(err?.message || '查询失败');
  } finally {
    quoteLoading.value = false;
  }
}

function openCreate() {
  editingRecord.value = null;
  formState.value = {
    baseCurrency: 'USD',
    quoteCurrency: 'CNY',
    rate: undefined,
    effectiveDate: new Date().toISOString().slice(0, 10),
    source: 'MANUAL',
    status: 'ENABLED',
  };
  modalVisible.value = true;
}

function openEdit(record: FxRateRecord) {
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
      await updateFxRate(editingRecord.value.id, formState.value!);
      message.success('更新成功');
    } else {
      await createFxRate(formState.value!);
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

async function handleEnable(record: FxRateRecord) {
  if (!record.id) return;
  try {
    await enableFxRate(record.id);
    message.success('已启用');
    load();
  } catch (e: unknown) {
    const err = e as { message?: string };
    message.error(err?.message || '操作失败');
  }
}

async function handleDisable(record: FxRateRecord) {
  if (!record.id) return;
  try {
    await disableFxRate(record.id);
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
  <Page title="汇率管理">
    <Card v-if="hasPerm('sys:fx:get')" class="mb-4" title="查询有效汇率">
      <div class="flex flex-wrap items-end gap-2">
        <Form layout="inline" class="flex flex-wrap gap-2">
          <Form.Item label="基准币种">
            <Input v-model:value="quoteBase" placeholder="USD" style="width: 80px" />
          </Form.Item>
          <Form.Item label="报价币种">
            <Input v-model:value="quoteQuote" placeholder="CNY" style="width: 80px" />
          </Form.Item>
          <Form.Item label="日期（空为今日）">
            <Input v-model:value="quoteDate" type="date" style="width: 140px" />
          </Form.Item>
          <Button type="primary" :loading="quoteLoading" @click="handleQuote">查询</Button>
        </Form>
        <span v-if="quoteResult != null" class="text-gray-6"> 1 {{ quoteBase }} = {{ quoteResult }} {{ quoteQuote }} </span>
      </div>
    </Card>
    <div class="mb-4 flex gap-2">
      <Input v-model:value="baseCurrency" placeholder="基准币种" style="width: 100px" allow-clear />
      <Input v-model:value="quoteCurrency" placeholder="报价币种" style="width: 100px" allow-clear />
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
      <Button v-if="hasPerm('sys:fx:create')" type="primary" @click="openCreate">新增</Button>
    </div>
    <Table
      :columns="[
        { title: 'ID', dataIndex: 'id', width: 80 },
        { title: '基准币种', dataIndex: 'baseCurrency', width: 100 },
        { title: '报价币种', dataIndex: 'quoteCurrency', width: 100 },
        { title: '汇率', dataIndex: 'rate', width: 120 },
        { title: '生效日期', dataIndex: 'effectiveDate', width: 120 },
        { title: '来源', dataIndex: 'source', width: 90 },
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
            <Button v-if="hasPerm('sys:fx:update')" type="link" size="small" @click="openEdit(record)">
              编辑
            </Button>
            <Button
              v-if="hasPerm('sys:fx:enable') && record.status === 'DISABLED'"
              type="link"
              size="small"
              @click="handleEnable(record)"
            >
              启用
            </Button>
            <Button
              v-if="hasPerm('sys:fx:disable') && record.status === 'ENABLED'"
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
    <Modal v-model:open="modalVisible" :title="editingRecord ? '编辑汇率' : '新增汇率'" @ok="handleSave">
      <Form ref="formRef" :model="formState" layout="vertical">
        <Form.Item name="baseCurrency" label="基准币种" :rules="[{ required: true }]">
          <Input v-model:value="formState.baseCurrency" :disabled="!!editingRecord?.id" placeholder="如 USD" />
        </Form.Item>
        <Form.Item name="quoteCurrency" label="报价币种" :rules="[{ required: true }]">
          <Input v-model:value="formState.quoteCurrency" :disabled="!!editingRecord?.id" placeholder="如 CNY" />
        </Form.Item>
        <Form.Item name="rate" label="汇率" :rules="[{ required: true }]">
          <Input v-model:value="formState.rate" type="number" step="0.00000001" placeholder="如 7.2" />
        </Form.Item>
        <Form.Item name="effectiveDate" label="生效日期" :rules="[{ required: true }]">
          <Input v-model:value="formState.effectiveDate" type="date" />
        </Form.Item>
        <Form.Item name="source" label="来源">
          <Select
            v-model:value="formState.source"
            :options="[
              { label: '手工', value: 'MANUAL' },
              { label: '导入', value: 'IMPORT' },
              { label: 'API', value: 'API' },
            ]"
            style="width: 100%"
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
