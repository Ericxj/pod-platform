<script lang="ts" setup>
import { Page } from '@vben/common-ui';
import {
  pageSku,
  getSku,
  createSku,
  updateSku,
  activateSku,
  deactivateSku,
  listBarcode,
  batchAddBarcode,
  deleteBarcode,
  getBomBySkuId,
  getBomItems,
  getRoutingBySkuId,
  getRoutingSteps,
  pageSkuMapping,
} from '#/api/prd';
import type { SkuRecord } from '#/api/prd';
import { message, Table, Button, Input, Modal, Form, Tabs, InputNumber, Space } from 'ant-design-vue';
import { ref, onMounted, watch } from 'vue';
import { usePermission } from '#/composables/usePermission';

const hasPerm = usePermission().hasPermission;
const list = ref<SkuRecord[]>([]);
const total = ref(0);
const loading = ref(false);
const current = ref(1);
const size = ref(10);
const keyword = ref('');
const spuId = ref<number | undefined>();

const detailVisible = ref(false);
const detailSkuId = ref<number | null>(null);
const detailSku = ref<SkuRecord | null>(null);
const activeTab = ref('base');
const barcodeList = ref<any[]>([]);
const bomData = ref<any>(null);
const bomItems = ref<any[]>([]);
const routingData = ref<any>(null);
const routingSteps = ref<any[]>([]);
const mappingList = ref<any[]>([]);
const mappingTotal = ref(0);

const createModalVisible = ref(false);
const createFormState = ref<Partial<SkuRecord> & { spuId?: number; skuCode?: string }>({});
const createLoading = ref(false);
const createFormRef = ref();

async function load() {
  loading.value = true;
  try {
    const res = await pageSku({
      current: current.value,
      size: size.value,
      keyword: keyword.value || undefined,
      spuId: spuId.value,
    });
    const d = res as any;
    list.value = d?.records ?? [];
    total.value = d?.total ?? 0;
  } catch (e: any) {
    message.error(e?.message || '加载失败');
  } finally {
    loading.value = false;
  }
}

async function loadDetail() {
  const id = detailSkuId.value;
  if (!id) return;
  try {
    detailSku.value = (await getSku(id)) as SkuRecord;
    if (activeTab.value === 'barcode') {
      const r = await listBarcode(id);
      barcodeList.value = (r as any) ?? [];
    }
    if (activeTab.value === 'bom') {
      const bomRes = await getBomBySkuId(id).catch(() => null);
      bomData.value = bomRes as any;
      if (bomData.value?.id) {
        const itemsRes = await getBomItems(bomData.value.id).catch(() => []);
        bomItems.value = (itemsRes as any) ?? [];
      } else {
        bomItems.value = [];
      }
    }
    if (activeTab.value === 'routing') {
      const routRes = await getRoutingBySkuId(id).catch(() => null);
      routingData.value = routRes as any;
      if (routingData.value?.id) {
        const stepsRes = await getRoutingSteps(routingData.value.id).catch(() => []);
        routingSteps.value = (stepsRes as any) ?? [];
      } else {
        routingSteps.value = [];
      }
    }
    if (activeTab.value === 'mapping') {
      const mapRes = await pageSkuMapping({ current: 1, size: 100, skuCode: detailSku.value?.skuCode });
      const d = mapRes as any;
      mappingList.value = d?.records ?? [];
      mappingTotal.value = d?.total ?? 0;
    }
  } catch (e: any) {
    message.error(e?.message || '加载详情失败');
  }
}

function openDetail(record: SkuRecord) {
  detailSkuId.value = record.id ?? null;
  detailSku.value = null;
  activeTab.value = 'base';
  detailVisible.value = true;
  loadDetail();
}

watch(activeTab, () => {
  if (detailVisible.value && detailSkuId.value) loadDetail();
});

function openCreate() {
  createFormState.value = { spuId: undefined, skuCode: '', skuName: '', price: undefined, weightG: undefined, attributesJson: '' };
  createModalVisible.value = true;
}

async function handleCreate() {
  try {
    await createFormRef.value?.validate();
  } catch {
    return;
  }
  if (!createFormState.value.spuId || !createFormState.value.skuCode) {
    message.error('请填写SPU与SKU编码');
    return;
  }
  createLoading.value = true;
  try {
    await createSku({
      spuId: createFormState.value.spuId,
      skuCode: createFormState.value.skuCode,
      skuName: createFormState.value.skuName,
      price: createFormState.value.price,
      weightG: createFormState.value.weightG,
      attributesJson: createFormState.value.attributesJson,
    });
    message.success('创建成功');
    createModalVisible.value = false;
    load();
  } catch (e: any) {
    message.error(e?.message || '创建失败');
  } finally {
    createLoading.value = false;
  }
}

async function doActivate(record: SkuRecord) {
  if (!record.id) return;
  try {
    await activateSku(record.id);
    message.success('已激活');
    load();
    if (detailSkuId.value === record.id) loadDetail();
  } catch (e: any) {
    message.error(e?.message || '激活失败');
  }
}

async function doDeactivate(record: SkuRecord) {
  if (!record.id) return;
  try {
    await deactivateSku(record.id);
    message.success('已停用');
    load();
    if (detailSkuId.value === record.id) loadDetail();
  } catch (e: any) {
    message.error(e?.message || '停用失败');
  }
}

onMounted(load);
</script>

<template>
  <Page title="SKU管理">
    <div class="mb-4 flex gap-2">
      <Input v-model:value="keyword" placeholder="SKU编码/名称" allow-clear style="width: 180px" />
      <InputNumber v-model:value="spuId" placeholder="SPU ID" allow-clear style="width: 120px" />
      <Button type="primary" @click="load">查询</Button>
      <Button v-if="hasPerm('prd:sku:create')" type="primary" @click="openCreate">新增SKU</Button>
    </div>
    <Table
      :data-source="list"
      :loading="loading"
      row-key="id"
      :pagination="{ current, pageSize: size, total, showSizeChanger: true, showTotal: (t: number) => `共 ${t} 条` }"
      @change="(p: any) => { current = p.current; size = p.pageSize; load(); }"
    >
      <Table.Column title="ID" data-index="id" width="80" />
      <Table.Column title="SPU ID" data-index="spuId" width="80" />
      <Table.Column title="SKU编码" data-index="skuCode" width="160" />
      <Table.Column title="SKU名称" data-index="skuName" />
      <Table.Column title="价格" data-index="price" width="90" />
      <Table.Column title="状态" data-index="status" width="90" />
      <Table.Column title="操作" key="action" width="220" fixed="right">
        <template #default="{ record }">
          <Space>
            <Button v-if="hasPerm('prd:sku:get')" type="link" size="small" @click="openDetail(record)">详情</Button>
            <Button v-if="hasPerm('prd:sku:activate') && record.status === 'DRAFT'" type="link" size="small" @click="doActivate(record)">激活</Button>
            <Button v-if="hasPerm('prd:sku:deactivate') && record.status === 'ACTIVE'" type="link" size="small" @click="doDeactivate(record)">停用</Button>
          </Space>
        </template>
      </Table.Column>
    </Table>

    <Modal v-model:open="detailVisible" title="SKU详情" width="720" :footer="null" destroy-on-close>
      <template v-if="detailSku">
        <Tabs v-model:activeKey="activeTab">
          <Tabs.TabPane key="base" tab="基础信息">
            <p>SKU编码：{{ detailSku.skuCode }}</p>
            <p>SKU名称：{{ detailSku.skuName }}</p>
            <p>价格：{{ detailSku.price }} 重量(g)：{{ detailSku.weightG }}</p>
            <p>状态：{{ detailSku.status }}</p>
          </Tabs.TabPane>
          <Tabs.TabPane key="barcode" tab="条码">
            <div v-if="barcodeList.length">条码列表：{{ barcodeList.map((b: any) => b.barcode).join(', ') }}</div>
            <div v-else>暂无条码</div>
          </Tabs.TabPane>
          <Tabs.TabPane key="bom" tab="BOM">
            <div v-if="bomData">版本：{{ bomData.versionNo }} 状态：{{ bomData.status }}</div>
            <div v-if="bomItems.length">明细 {{ bomItems.length }} 行</div>
            <div v-else>暂无BOM</div>
          </Tabs.TabPane>
          <Tabs.TabPane key="routing" tab="工艺路线">
            <div v-if="routingData">版本：{{ routingData.versionNo }} 状态：{{ routingData.status }}</div>
            <div v-if="routingSteps.length">工序 {{ routingSteps.length }} 步</div>
            <div v-else>暂无工艺路线</div>
          </Tabs.TabPane>
          <Tabs.TabPane key="mapping" tab="平台映射">
            <div v-if="mappingList.length">共 {{ mappingTotal }} 条映射</div>
            <div v-else>暂无平台映射</div>
          </Tabs.TabPane>
        </Tabs>
      </template>
    </Modal>

    <Modal v-model:open="createModalVisible" title="新增SKU" :confirm-loading="createLoading" @ok="handleCreate">
      <Form ref="createFormRef" :model="createFormState" layout="vertical">
        <Form.Item name="spuId" label="SPU ID" :rules="[{ required: true }]">
          <InputNumber v-model:value="createFormState.spuId" placeholder="SPU主键" style="width: 100%" />
        </Form.Item>
        <Form.Item name="skuCode" label="SKU编码" :rules="[{ required: true }]">
          <Input v-model:value="createFormState.skuCode" placeholder="唯一编码" />
        </Form.Item>
        <Form.Item name="skuName" label="SKU名称">
          <Input v-model:value="createFormState.skuName" placeholder="名称" />
        </Form.Item>
        <Form.Item name="price" label="价格">
          <InputNumber v-model:value="createFormState.price" placeholder="价格" style="width: 100%" />
        </Form.Item>
        <Form.Item name="weightG" label="重量(g)">
          <InputNumber v-model:value="createFormState.weightG" placeholder="克" style="width: 100%" />
        </Form.Item>
      </Form>
    </Modal>
  </Page>
</template>
