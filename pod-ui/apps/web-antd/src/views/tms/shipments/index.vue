<script lang="ts" setup>
import { Page } from '@vben/common-ui';
import {
  pageShipments,
  getShipmentDetail,
  createShipmentFromOutbound,
  createLabel,
  syncToChannel,
} from '#/api/tms';
import type { ShipmentRecord } from '#/api/tms';
import { message, Table, Button, Input, Modal, Descriptions, Tag } from 'ant-design-vue';
import { ref, onMounted } from 'vue';
import { usePermission } from '#/composables/usePermission';

const hasPerm = usePermission().hasPermission;
const list = ref<ShipmentRecord[]>([]);
const total = ref(0);
const loading = ref(false);
const current = ref(1);
const size = ref(10);
const status = ref('');

const detailVisible = ref(false);
const detailRecord = ref<ShipmentRecord | null>(null);
const detailLoading = ref(false);

const createOutboundId = ref<number | string>('');
const createSubmitting = ref(false);

async function load() {
  loading.value = true;
  try {
    const res = await pageShipments({
      current: current.value,
      size: size.value,
      status: status.value || undefined,
    });
    const d = (res as any)?.data ?? res;
    list.value = d?.records ?? [];
    total.value = d?.total ?? 0;
  } catch (e: any) {
    message.error(e?.message || '加载失败');
  } finally {
    loading.value = false;
  }
}

async function openDetail(record: ShipmentRecord) {
  if (!record.id) return;
  detailVisible.value = true;
  detailRecord.value = null;
  detailLoading.value = true;
  try {
    detailRecord.value = (await getShipmentDetail(record.id)) as ShipmentRecord;
  } catch (e: any) {
    message.error(e?.message || '加载详情失败');
  } finally {
    detailLoading.value = false;
  }
}

async function doCreateLabel(record: ShipmentRecord) {
  if (!record.id) return;
  try {
    await createLabel(record.id);
    message.success('面单生成成功');
    load();
    if (detailRecord.value?.id === record.id) openDetail(record);
  } catch (e: any) {
    message.error(e?.message || '生成面单失败');
  }
}

async function doSyncToChannel(record: ShipmentRecord) {
  if (!record.id) return;
  try {
    await syncToChannel(record.id);
    message.success('已回传平台');
    load();
    if (detailRecord.value?.id === record.id) openDetail(record);
  } catch (e: any) {
    message.error(e?.message || '回传平台失败');
  }
}

async function doCreateFromOutbound() {
  const id = typeof createOutboundId.value === 'string' ? Number(createOutboundId.value) : createOutboundId.value;
  if (!id || Number.isNaN(id)) {
    message.warning('请输入出库单ID');
    return;
  }
  createSubmitting.value = true;
  try {
    await createShipmentFromOutbound(id);
    message.success('发货单已创建');
    createOutboundId.value = '';
    load();
  } catch (e: any) {
    message.error(e?.message || '创建失败');
  } finally {
    createSubmitting.value = false;
  }
}

function statusTag(s: string) {
  const map: Record<string, string> = {
    CREATED: 'blue',
    LABEL_CREATED: 'cyan',
    HANDED_OVER: 'processing',
    TRACKING_SYNCED: 'green',
    FAILED: 'red',
    LABELED: 'cyan',
    SHIPPED: 'default',
    DELIVERED: 'default',
  };
  return { color: map[s] || 'default', text: s || '-' };
}

onMounted(load);
</script>

<template>
  <Page title="发货单（Shipment）">
    <div class="mb-4 flex gap-2 flex-wrap items-center">
      <Input v-model:value="status" placeholder="状态" allow-clear style="width: 140px" />
      <Button type="primary" @click="load">查询</Button>
      <span class="ml-4">从出库单创建：</span>
      <Input v-model:value="createOutboundId" placeholder="出库单ID（WMS 已发货）" style="width: 160px" />
      <Button v-if="hasPerm('tms:shipment:create')" :loading="createSubmitting" @click="doCreateFromOutbound">创建发货单</Button>
    </div>
    <Table
      :data-source="list"
      :loading="loading"
      row-key="id"
      :pagination="{ current, pageSize: size, total, showSizeChanger: true, showTotal: (t: number) => `共 ${t} 条` }"
      @change="(p: any) => { current = p.current; size = p.pageSize; load(); }"
    >
      <Table.Column title="ID" data-index="id" width="80" />
      <Table.Column title="发货单号" data-index="shipmentNo" width="200" />
      <Table.Column title="来源单号" data-index="sourceNo" width="140" />
      <Table.Column title="承运商" data-index="carrierCode" width="100" />
      <Table.Column title="运单号" data-index="trackingNo" width="160" />
      <Table.Column title="状态" data-index="status" width="120">
        <template #default="{ record }">
          <Tag :color="statusTag(record.status).color">{{ statusTag(record.status).text }}</Tag>
        </template>
      </Table.Column>
      <Table.Column title="创建时间" data-index="createdAt" width="170" />
      <Table.Column title="操作" key="action" width="260" fixed="right">
        <template #default="{ record }">
          <Button v-if="hasPerm('tms:shipment:get')" type="link" size="small" @click="openDetail(record)">详情</Button>
          <Button v-if="hasPerm('tms:shipment:label') && (record.status === 'CREATED' || record.status === 'FAILED')" type="link" size="small" @click="doCreateLabel(record)">生成面单</Button>
          <Button v-if="hasPerm('tms:shipment:sync') && (record.status === 'LABEL_CREATED' || record.status === 'HANDED_OVER')" type="link" size="small" @click="doSyncToChannel(record)">回传平台</Button>
        </template>
      </Table.Column>
    </Table>

    <Modal v-model:open="detailVisible" title="发货单详情" width="680" :footer="null" destroy-on-close>
      <div v-if="detailLoading">加载中...</div>
      <template v-else-if="detailRecord">
        <Descriptions :column="2" bordered size="small" class="mb-4">
          <Descriptions.Item label="发货单号">{{ detailRecord.shipmentNo }}</Descriptions.Item>
          <Descriptions.Item label="状态"><Tag :color="statusTag(detailRecord.status).color">{{ detailRecord.status }}</Tag></Descriptions.Item>
          <Descriptions.Item label="来源">{{ detailRecord.sourceNo }}</Descriptions.Item>
          <Descriptions.Item label="出库单ID">{{ detailRecord.outboundId }}</Descriptions.Item>
          <Descriptions.Item label="承运商">{{ detailRecord.carrierCode }}</Descriptions.Item>
          <Descriptions.Item label="服务">{{ detailRecord.serviceCode }}</Descriptions.Item>
          <Descriptions.Item label="运单号">{{ detailRecord.trackingNo }}</Descriptions.Item>
          <Descriptions.Item label="面单">{{ detailRecord.labelUrl ? (detailRecord.labelUrl.length > 50 ? detailRecord.labelUrl.slice(0, 50) + '...' : detailRecord.labelUrl) : '-' }}</Descriptions.Item>
          <Descriptions.Item v-if="detailRecord.failReason" label="失败原因" :span="2">{{ detailRecord.failReason }}</Descriptions.Item>
        </Descriptions>
        <div class="mt-4 flex gap-2">
          <Button v-if="['CREATED','FAILED'].includes(detailRecord.status)" type="primary" @click="doCreateLabel(detailRecord)">生成面单</Button>
          <Button v-if="['LABEL_CREATED','HANDED_OVER'].includes(detailRecord.status)" @click="doSyncToChannel(detailRecord)">回传平台</Button>
        </div>
      </template>
    </Modal>
  </Page>
</template>
