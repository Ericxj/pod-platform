<script lang="ts" setup>
import { Page } from '@vben/common-ui';
import { pageAcks, getAckDetail, retryAck, createAckFromOutbound } from '#/api/tms';
import type { ChannelAckRecord } from '#/api/tms';
import { message, Table, Button, Input, Modal, Descriptions, Tag, Collapse } from 'ant-design-vue';
import { ref, onMounted } from 'vue';
import { usePermission } from '#/composables/usePermission';

const hasPerm = usePermission().hasPermission;
const list = ref<ChannelAckRecord[]>([]);
const total = ref(0);
const loading = ref(false);
const current = ref(1);
const size = ref(10);
const channel = ref('AMAZON');
const status = ref('');
const orderId = ref('');
const trackingNo = ref('');

const detailVisible = ref(false);
const detailRecord = ref<ChannelAckRecord | null>(null);
const detailLoading = ref(false);

const createOutboundId = ref<number | string>('');
const createSubmitting = ref(false);

async function load() {
  loading.value = true;
  try {
    const res = await pageAcks({
      current: current.value,
      size: size.value,
      channel: channel.value || undefined,
      status: status.value || undefined,
      orderId: orderId.value || undefined,
      trackingNo: trackingNo.value || undefined,
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

async function openDetail(record: ChannelAckRecord) {
  if (!record.id) return;
  detailVisible.value = true;
  detailRecord.value = null;
  detailLoading.value = true;
  try {
    detailRecord.value = (await getAckDetail(record.id)) as ChannelAckRecord;
  } catch (e: any) {
    message.error(e?.message || '加载详情失败');
  } finally {
    detailLoading.value = false;
  }
}

async function doRetry(record: ChannelAckRecord) {
  if (!record.id) return;
  try {
    await retryAck(record.id);
    message.success('已提交重试');
    load();
    if (detailRecord.value?.id === record.id) openDetail(record);
  } catch (e: any) {
    message.error(e?.message || '重试失败');
  }
}

async function doCreateFromOutbound() {
  const id = typeof createOutboundId.value === 'string' ? Number(createOutboundId.value) : createOutboundId.value;
  if (!id || Number.isNaN(id)) {
    message.warning('请输入出库单ID（WMS 已发货）');
    return;
  }
  createSubmitting.value = true;
  try {
    await createAckFromOutbound(id);
    message.success('回传任务已创建');
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
    SENDING: 'processing',
    SUCCESS: 'green',
    FAILED_RETRYABLE: 'orange',
    FAILED_MANUAL: 'red',
  };
  return { color: map[s] || 'default', text: s || '-' };
}

function formatOrderItemsJson(json: string | undefined) {
  if (!json || !json.trim()) return '-';
  try {
    const arr = JSON.parse(json) as Array<{ orderItemId?: string; quantity?: number }>;
    return arr.map((x) => `${x.orderItemId ?? '-'}: qty ${x.quantity ?? 0}`).join('\n');
  } catch {
    return json;
  }
}

onMounted(load);
</script>

<template>
  <Page title="回传任务">
    <div class="mb-4 flex gap-2 flex-wrap items-center">
      <Input v-model:value="channel" placeholder="渠道" allow-clear style="width: 100px" />
      <Input v-model:value="status" placeholder="状态" allow-clear style="width: 140px" />
      <Input v-model:value="orderId" placeholder="订单号" allow-clear style="width: 160px" />
      <Input v-model:value="trackingNo" placeholder="运单号" allow-clear style="width: 140px" />
      <Button type="primary" @click="load">查询</Button>
      <span class="ml-4">从出库单创建：</span>
      <Input v-model:value="createOutboundId" placeholder="出库单ID" style="width: 120px" />
      <Button v-if="hasPerm('tms:ack:create')" :loading="createSubmitting" @click="doCreateFromOutbound">创建回传任务</Button>
    </div>
    <Table
      :data-source="list"
      :loading="loading"
      row-key="id"
      :pagination="{ current, pageSize: size, total, showSizeChanger: true, showTotal: (t: number) => `共 ${t} 条` }"
      @change="(p: any) => { current = p.current; size = p.pageSize; load(); }"
    >
      <Table.Column title="ID" data-index="id" width="80" />
      <Table.Column title="订单号" data-index="amazonOrderId" width="160" />
      <Table.Column title="包裹号" data-index="packageReferenceId" width="80" />
      <Table.Column title="运单号" data-index="trackingNo" width="140" />
      <Table.Column title="承运商" data-index="carrierCode" width="90" />
      <Table.Column title="发货时间(UTC)" data-index="shipDateUtc" width="170" />
      <Table.Column title="状态" data-index="status" width="130">
        <template #default="{ record }">
          <Tag :color="statusTag(record.status).color">{{ statusTag(record.status).text }}</Tag>
        </template>
      </Table.Column>
      <Table.Column title="重试次数" data-index="retryCount" width="90" />
      <Table.Column title="下次重试" data-index="nextRetryAt" width="170" />
      <Table.Column title="自愈" key="selfHeal" width="100">
        <template #default="{ record }">
          <span v-if="record.selfHealAttempted">{{ record.selfHealAction || 'Y' }}</span>
          <span v-else>-</span>
        </template>
      </Table.Column>
      <Table.Column title="最后错误" data-index="errorMessage" ellipsis />
      <Table.Column title="操作" key="action" width="120" fixed="right">
        <template #default="{ record }">
          <Button v-if="hasPerm('tms:ack:get')" type="link" size="small" @click="openDetail(record)">详情</Button>
          <Button v-if="hasPerm('tms:ack:retry') && (record.status === 'CREATED' || record.status === 'FAILED_RETRYABLE')" type="link" size="small" @click="doRetry(record)">重试</Button>
        </template>
      </Table.Column>
    </Table>

    <Modal v-model:open="detailVisible" title="回传任务详情" width="720" :footer="null" destroy-on-close>
      <div v-if="detailLoading">加载中...</div>
      <template v-else-if="detailRecord">
        <Descriptions :column="2" bordered size="small" class="mb-4">
          <Descriptions.Item label="订单号">{{ detailRecord.amazonOrderId }}</Descriptions.Item>
          <Descriptions.Item label="包裹号">{{ detailRecord.packageReferenceId ?? '-' }}</Descriptions.Item>
          <Descriptions.Item label="状态"><Tag :color="statusTag(detailRecord.status).color">{{ detailRecord.status }}</Tag></Descriptions.Item>
          <Descriptions.Item label="运单号">{{ detailRecord.trackingNo }}</Descriptions.Item>
          <Descriptions.Item label="承运商">{{ detailRecord.carrierCode }} / {{ detailRecord.carrierName }}</Descriptions.Item>
          <Descriptions.Item label="发货时间(UTC)">{{ detailRecord.shipDateUtc }}</Descriptions.Item>
          <Descriptions.Item label="重试次数">{{ detailRecord.retryCount }}</Descriptions.Item>
          <Descriptions.Item label="404重试">{{ detailRecord.retry404Count ?? 0 }}</Descriptions.Item>
          <Descriptions.Item label="下次重试">{{ detailRecord.nextRetryAt }}</Descriptions.Item>
          <Descriptions.Item label="最后尝试">{{ detailRecord.lastAttemptAt }}</Descriptions.Item>
          <Descriptions.Item label="自愈尝试">{{ detailRecord.selfHealAttempted ? '是' : '否' }}</Descriptions.Item>
          <Descriptions.Item label="自愈动作">{{ detailRecord.selfHealAction ?? '-' }}</Descriptions.Item>
          <Descriptions.Item label="自愈时间">{{ detailRecord.selfHealAt ?? '-' }}</Descriptions.Item>
          <Descriptions.Item label="响应码">{{ detailRecord.responseCode }}</Descriptions.Item>
          <Descriptions.Item label="错误码">{{ detailRecord.errorCode }}</Descriptions.Item>
          <Descriptions.Item label="错误信息" :span="2">{{ detailRecord.errorMessage }}</Descriptions.Item>
        </Descriptions>
        <div v-if="detailRecord.orderItemsJson" class="mb-4">
          <div class="text-sm font-medium mb-1">包裹内 item 分摊</div>
          <pre class="text-xs overflow-auto max-h-32 bg-gray-50 p-2 rounded">{{ formatOrderItemsJson(detailRecord.orderItemsJson) }}</pre>
        </div>
        <Collapse v-if="detailRecord.requestPayloadJson" class="mb-2">
          <Collapse.Panel key="payload" header="请求 payload">
            <pre class="text-xs overflow-auto max-h-40">{{ detailRecord.requestPayloadJson }}</pre>
          </Collapse.Panel>
        </Collapse>
        <Collapse v-if="detailRecord.responseBody" class="mb-2">
          <Collapse.Panel key="response" header="响应 body">
            <pre class="text-xs overflow-auto max-h-40">{{ detailRecord.responseBody }}</pre>
          </Collapse.Panel>
        </Collapse>
        <div class="mt-4">
          <Button v-if="['CREATED','FAILED_RETRYABLE'].includes(detailRecord.status)" type="primary" @click="doRetry(detailRecord)">手动重试</Button>
        </div>
      </template>
    </Modal>
  </Page>
</template>
