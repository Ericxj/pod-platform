<script lang="ts" setup>
import { Page } from '@vben/common-ui';
import {
  pageOutbounds,
  getOutboundDetail,
  createOutboundFromFulfillment,
  startPicking,
  confirmPicking,
  packOutbound,
  shipOutbound,
  cancelOutbound,
} from '#/api/wms';
import type { OutboundRecord, OutboundLineRecord, PickingLineDto } from '#/api/wms';
import { message, Table, Button, Input, Modal, Descriptions, Tag, InputNumber } from 'ant-design-vue';
import { ref, onMounted } from 'vue';
import { usePermission } from '#/composables/usePermission';

const hasPerm = usePermission().hasPermission;
const list = ref<OutboundRecord[]>([]);
const total = ref(0);
const loading = ref(false);
const current = ref(1);
const size = ref(10);
const status = ref('');

const detailVisible = ref(false);
const detailRecord = ref<OutboundRecord | null>(null);
const detailLoading = ref(false);

const pickingVisible = ref(false);
const pickingOutboundId = ref<number | null>(null);
const pickingLines = ref<{ lineId: number; lineNo?: number; qty: number; pickedQty: number; add: number }[]>([]);
const pickingSubmitting = ref(false);

const shipVisible = ref(false);
const shipOutboundId = ref<number | null>(null);
const shipForm = ref({ carrierCode: '', trackingNo: '' });
const shipSubmitting = ref(false);

const createFulfillmentId = ref<number | string>('');
const createSubmitting = ref(false);

async function load() {
  loading.value = true;
  try {
    const res = await pageOutbounds({
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

async function openDetail(record: OutboundRecord) {
  if (!record.id) return;
  detailVisible.value = true;
  detailRecord.value = null;
  detailLoading.value = true;
  try {
    detailRecord.value = (await getOutboundDetail(record.id)) as OutboundRecord;
  } catch (e: any) {
    message.error(e?.message || '加载详情失败');
  } finally {
    detailLoading.value = false;
  }
}

async function doStartPicking(record: OutboundRecord) {
  if (!record.id) return;
  try {
    await startPicking(record.id);
    message.success('已开始拣货');
    load();
    if (detailRecord.value?.id === record.id) openDetail(record);
  } catch (e: any) {
    message.error(e?.message || '操作失败');
  }
}

function openPickingConfirm(record: OutboundRecord) {
  if (!record.id || !record.lines?.length) return;
  pickingOutboundId.value = record.id;
  pickingLines.value = record.lines.map((l) => ({
    lineId: l.id!,
    lineNo: l.lineNo ?? 0,
    qty: l.qty ?? 0,
    pickedQty: l.qtyPicked ?? 0,
    add: 0,
  }));
  pickingVisible.value = true;
}

async function submitPicking() {
  if (!pickingOutboundId.value) return;
  const body: PickingLineDto[] = pickingLines.value.filter((r) => r.add > 0).map((r) => ({ lineId: r.lineId, pickedQty: r.add }));
  if (body.length === 0) {
    message.warning('请填写拣货数量');
    return;
  }
  pickingSubmitting.value = true;
  try {
    await confirmPicking(pickingOutboundId.value, body);
    message.success('拣货确认成功');
    pickingVisible.value = false;
    load();
    if (detailRecord.value?.id === pickingOutboundId.value) openDetail(detailRecord.value);
  } catch (e: any) {
    message.error(e?.message || '确认失败');
  } finally {
    pickingSubmitting.value = false;
  }
}

async function doPack(record: OutboundRecord) {
  if (!record.id) return;
  try {
    await packOutbound(record.id);
    message.success('打包成功');
    load();
    if (detailRecord.value?.id === record.id) openDetail(record);
  } catch (e: any) {
    message.error(e?.message || '打包失败');
  }
}

function openShip(record: OutboundRecord) {
  if (!record.id) return;
  shipOutboundId.value = record.id;
  shipForm.value = { carrierCode: '', trackingNo: '' };
  shipVisible.value = true;
}

async function submitShip() {
  if (!shipOutboundId.value) return;
  shipSubmitting.value = true;
  try {
    await shipOutbound(shipOutboundId.value, { carrierCode: shipForm.value.carrierCode, trackingNo: shipForm.value.trackingNo });
    message.success('发货成功');
    shipVisible.value = false;
    load();
    detailVisible.value = false;
  } catch (e: any) {
    message.error(e?.message || '发货失败');
  } finally {
    shipSubmitting.value = false;
  }
}

function doCancel(record: OutboundRecord) {
  if (!record.id) return;
  Modal.confirm({
    title: '确认取消出库单',
    content: `确定取消出库单 ${record.outboundNo} 吗？将释放预占库存。`,
    onOk: async () => {
      try {
        await cancelOutbound(record.id!);
        message.success('已取消');
        load();
        detailVisible.value = false;
      } catch (e: any) {
        message.error(e?.message || '取消失败');
      }
    },
  });
}

async function doCreateFromFulfillment() {
  const id = typeof createFulfillmentId.value === 'string' ? Number(createFulfillmentId.value) : createFulfillmentId.value;
  if (!id || Number.isNaN(id)) {
    message.warning('请输入履约单ID');
    return;
  }
  createSubmitting.value = true;
  try {
    await createOutboundFromFulfillment(id);
    message.success('出库单已创建');
    createFulfillmentId.value = '';
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
    PICKING: 'cyan',
    PICKED: 'processing',
    PACKED: 'green',
    SHIPPED: 'default',
    CANCELLED: 'default',
  };
  return { color: map[s] || 'default', text: s || '-' };
}

onMounted(load);
</script>

<template>
  <Page title="出库单">
    <div class="mb-4 flex gap-2 flex-wrap items-center">
      <Input v-model:value="status" placeholder="状态" allow-clear style="width: 120px" />
      <Button type="primary" @click="load">查询</Button>
      <span class="ml-4">按履约单创建：</span>
      <Input v-model:value="createFulfillmentId" placeholder="履约单ID" style="width: 120px" />
      <Button v-if="hasPerm('wms:outbound:create')" :loading="createSubmitting" @click="doCreateFromFulfillment">创建出库单</Button>
    </div>
    <Table
      :data-source="list"
      :loading="loading"
      row-key="id"
      :pagination="{ current, pageSize: size, total, showSizeChanger: true, showTotal: (t: number) => `共 ${t} 条` }"
      @change="(p: any) => { current = p.current; size = p.pageSize; load(); }"
    >
      <Table.Column title="ID" data-index="id" width="80" />
      <Table.Column title="出库单号" data-index="outboundNo" width="180" />
      <Table.Column title="履约单ID" data-index="fulfillmentId" width="100" />
      <Table.Column title="来源单号" data-index="sourceNo" width="150" />
      <Table.Column title="状态" data-index="status" width="100">
        <template #default="{ record }">
          <Tag :color="statusTag(record.status).color">{{ statusTag(record.status).text }}</Tag>
        </template>
      </Table.Column>
      <Table.Column title="创建时间" data-index="createdAt" width="170" />
      <Table.Column title="操作" key="action" width="380" fixed="right">
        <template #default="{ record }">
          <Button v-if="hasPerm('wms:outbound:get')" type="link" size="small" @click="openDetail(record)">详情</Button>
          <Button v-if="hasPerm('wms:outbound:picking:start') && record.status === 'CREATED'" type="link" size="small" @click="doStartPicking(record)">开始拣货</Button>
          <Button v-if="hasPerm('wms:outbound:picking:confirm') && record.status === 'PICKING'" type="link" size="small" @click="openPickingConfirm(record)">确认拣货</Button>
          <Button v-if="hasPerm('wms:outbound:pack') && (record.status === 'PICKING' || record.status === 'PICKED')" type="link" size="small" @click="doPack(record)">打包</Button>
          <Button v-if="hasPerm('wms:outbound:ship') && record.status === 'PACKED'" type="link" size="small" @click="openShip(record)">发货</Button>
          <Button v-if="hasPerm('wms:outbound:cancel') && !['SHIPPED','CANCELLED'].includes(record.status)" type="link" size="small" danger @click="doCancel(record)">取消</Button>
        </template>
      </Table.Column>
    </Table>

    <Modal v-model:open="detailVisible" title="出库单详情" width="720" :footer="null" destroy-on-close>
      <div v-if="detailLoading">加载中...</div>
      <template v-else-if="detailRecord">
        <Descriptions :column="2" bordered size="small" class="mb-4">
          <Descriptions.Item label="出库单号">{{ detailRecord.outboundNo }}</Descriptions.Item>
          <Descriptions.Item label="状态"><Tag :color="statusTag(detailRecord.status).color">{{ detailRecord.status }}</Tag></Descriptions.Item>
          <Descriptions.Item label="履约单ID">{{ detailRecord.fulfillmentId }}</Descriptions.Item>
          <Descriptions.Item label="来源">{{ detailRecord.sourceNo }}</Descriptions.Item>
        </Descriptions>
        <div class="font-medium mb-2">明细</div>
        <Table :data-source="detailRecord.lines || []" row-key="id" size="small" :pagination="false">
          <Table.Column title="行号" data-index="lineNo" width="70" />
          <Table.Column title="SKU ID" data-index="skuId" width="90" />
          <Table.Column title="数量" data-index="qty" width="80" />
          <Table.Column title="已拣" data-index="qtyPicked" width="80" />
          <Table.Column title="已打包" data-index="packedQty" width="80" />
        </Table>
        <div class="mt-4 flex gap-2">
          <Button v-if="detailRecord.status === 'CREATED'" type="primary" @click="doStartPicking(detailRecord)">开始拣货</Button>
          <Button v-if="detailRecord.status === 'PICKING'" @click="openPickingConfirm(detailRecord)">确认拣货</Button>
          <Button v-if="['PICKING','PICKED'].includes(detailRecord.status)" @click="doPack(detailRecord)">打包</Button>
          <Button v-if="detailRecord.status === 'PACKED'" type="primary" @click="openShip(detailRecord)">发货</Button>
          <Button v-if="!['SHIPPED','CANCELLED'].includes(detailRecord.status)" danger @click="doCancel(detailRecord)">取消</Button>
        </div>
      </template>
    </Modal>

    <Modal v-model:open="pickingVisible" title="确认拣货" width="480" :footer="null" destroy-on-close>
      <Table :data-source="pickingLines" row-key="lineId" size="small" :pagination="false" class="mb-4">
        <Table.Column title="行号" data-index="lineNo" width="60" />
        <Table.Column title="计划数量" data-index="qty" width="80" />
        <Table.Column title="已拣" data-index="pickedQty" width="70" />
        <Table.Column title="本次拣货数量">
          <template #default="{ record }">
            <InputNumber v-model:value="record.add" :min="0" :max="record.qty" style="width: 100px" />
          </template>
        </Table.Column>
      </Table>
      <Button type="primary" :loading="pickingSubmitting" @click="submitPicking">提交</Button>
    </Modal>

    <Modal v-model:open="shipVisible" title="发货" width="400" :footer="null" destroy-on-close>
      <div class="mb-2">
        <span class="mr-2">承运商:</span>
        <Input v-model:value="shipForm.carrierCode" placeholder="如 SF" style="width: 200px" />
      </div>
      <div class="mb-4">
        <span class="mr-2">运单号:</span>
        <Input v-model:value="shipForm.trackingNo" placeholder="运单号" style="width: 200px" />
      </div>
      <Button type="primary" :loading="shipSubmitting" @click="submitShip">确认发货</Button>
    </Modal>
  </Page>
</template>
