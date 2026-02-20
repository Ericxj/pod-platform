<script lang="ts" setup>
import { Page } from '@vben/common-ui';
import { pageFulfillments, getFulfillment, retryReserveFulfillment, cancelFulfillment } from '#/api/oms';
import type { FulfillmentRecord } from '#/api/oms';
import { message, Table, Button, Input, Modal, Descriptions, Tag } from 'ant-design-vue';
import { ref, onMounted } from 'vue';
import { usePermission } from '#/composables/usePermission';

const hasPerm = usePermission().hasPermission;
const list = ref<FulfillmentRecord[]>([]);
const total = ref(0);
const loading = ref(false);
const current = ref(1);
const size = ref(10);
const status = ref('');
const fulfillmentNo = ref('');

const detailVisible = ref(false);
const detailRecord = ref<FulfillmentRecord | null>(null);
const detailLoading = ref(false);

async function load() {
  loading.value = true;
  try {
    const res = await pageFulfillments({
      current: current.value,
      size: size.value,
      status: status.value || undefined,
      fulfillmentNo: fulfillmentNo.value || undefined,
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

async function openDetail(record: FulfillmentRecord) {
  if (!record.id) return;
  detailVisible.value = true;
  detailRecord.value = null;
  detailLoading.value = true;
  try {
    detailRecord.value = (await getFulfillment(record.id)) as FulfillmentRecord;
  } catch (e: any) {
    message.error(e?.message || '加载详情失败');
  } finally {
    detailLoading.value = false;
  }
}

async function doRetryReserve(record: FulfillmentRecord) {
  if (!record.id) return;
  try {
    await retryReserveFulfillment(record.id);
    message.success('已提交重试预占');
    load();
    if (detailRecord.value?.id === record.id) openDetail(record);
  } catch (e: any) {
    message.error(e?.message || '重试预占失败');
  }
}

function doCancel(record: FulfillmentRecord) {
  if (!record.id) return;
  Modal.confirm({
    title: '确认取消履约',
    content: `确定取消履约单 ${record.fulfillmentNo} 吗？已预占库存将释放。`,
    onOk: async () => {
      try {
        await cancelFulfillment(record.id!);
        message.success('已取消');
        load();
        detailVisible.value = false;
      } catch (e: any) {
        message.error(e?.message || '取消失败');
      }
    },
  });
}

onMounted(load);
</script>

<template>
  <Page title="履约单">
    <div class="mb-4 flex gap-2 flex-wrap">
      <Input v-model:value="fulfillmentNo" placeholder="履约单号" allow-clear style="width: 160px" />
      <Input v-model:value="status" placeholder="状态" allow-clear style="width: 120px" />
      <Button type="primary" @click="load">查询</Button>
    </div>
    <Table
      :data-source="list"
      :loading="loading"
      row-key="id"
      :pagination="{ current, pageSize: size, total, showSizeChanger: true, showTotal: (t: number) => `共 ${t} 条` }"
      @change="(p: any) => { current = p.current; size = p.pageSize; load(); }"
    >
      <Table.Column title="ID" data-index="id" width="80" />
      <Table.Column title="履约单号" data-index="fulfillmentNo" width="180" />
      <Table.Column title="统一订单ID" data-index="unifiedOrderId" width="100" />
      <Table.Column title="渠道" data-index="channel" width="90" />
      <Table.Column title="状态" data-index="status" width="120">
        <template #default="{ record }">
          <Tag v-if="record.status === 'CREATED'" color="blue">CREATED</Tag>
          <Tag v-else-if="record.status === 'RESERVED'" color="green">RESERVED</Tag>
          <Tag v-else-if="record.status === 'ART_READY'" color="cyan">ART_READY</Tag>
          <Tag v-else-if="record.status === 'READY_TO_SHIP'" color="purple">READY_TO_SHIP</Tag>
          <Tag v-else-if="record.status === 'SHIPPED'" color="green">SHIPPED</Tag>
          <Tag v-else-if="record.status === 'HOLD_INVENTORY'" color="orange">HOLD_INVENTORY</Tag>
          <Tag v-else-if="record.status === 'CANCELLED'" color="default">CANCELLED</Tag>
          <Tag v-else>{{ record.status }}</Tag>
        </template>
      </Table.Column>
      <Table.Column title="创建时间" data-index="createdAt" width="170" />
      <Table.Column title="操作" key="action" width="220" fixed="right">
        <template #default="{ record }">
          <Button v-if="hasPerm('ful:fulfillment:get')" type="link" size="small" @click="openDetail(record)">详情</Button>
          <Button v-if="hasPerm('ful:fulfillment:reserve-retry') && (record.status === 'CREATED' || record.status === 'HOLD_INVENTORY')" type="link" size="small" @click="doRetryReserve(record)">{{ record.status === 'HOLD_INVENTORY' ? '重试预占' : '预占' }}</Button>
          <Button v-if="hasPerm('ful:fulfillment:cancel') && ['CREATED','RESERVED','HOLD_INVENTORY'].includes(record.status)" type="link" size="small" danger @click="doCancel(record)">取消履约</Button>
        </template>
      </Table.Column>
    </Table>

    <Modal v-model:open="detailVisible" title="履约单详情" width="720" :footer="null" destroy-on-close>
      <div v-if="detailLoading">加载中...</div>
      <template v-else-if="detailRecord">
        <Descriptions :column="2" bordered size="small" class="mb-4">
          <Descriptions.Item label="履约单号">{{ detailRecord.fulfillmentNo }}</Descriptions.Item>
          <Descriptions.Item label="状态">{{ detailRecord.status }}</Descriptions.Item>
          <Descriptions.Item label="统一订单ID">{{ detailRecord.unifiedOrderId }}</Descriptions.Item>
          <Descriptions.Item label="渠道">{{ detailRecord.channel }}</Descriptions.Item>
          <Descriptions.Item label="外部订单ID">{{ detailRecord.externalOrderId }}</Descriptions.Item>
          <Descriptions.Item label="仓库ID">{{ detailRecord.warehouseId }}</Descriptions.Item>
        </Descriptions>
        <div class="font-medium mb-2">明细（预占状态）</div>
        <Table :data-source="detailRecord.items || []" row-key="id" size="small" :pagination="false">
          <Table.Column title="行号" data-index="lineNo" width="70" />
          <Table.Column title="SKU ID" data-index="skuId" width="90" />
          <Table.Column title="数量" data-index="qty" width="70" />
          <Table.Column title="已预占" data-index="reservedQty" width="80" />
          <Table.Column title="预占状态" data-index="reserveStatus" width="100" />
        </Table>
      </template>
    </Modal>
  </Page>
</template>
