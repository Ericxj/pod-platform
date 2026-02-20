<script lang="ts" setup>
import { Page } from '@vben/common-ui';
import {
  pageWorkOrders,
  getWorkOrder,
  getWorkOrderLines,
  getWorkOrderOps,
  getWorkOrderReports,
  releaseWorkOrder,
  startWorkOrder,
  reportWorkOrder,
  cancelWorkOrder,
  createWorkOrderFromFulfillment,
} from '#/api/mes';
import type { WorkOrderRecord, WorkOrderLineRecord, WorkOrderOpRecord, MesReportRecord } from '#/api/mes';
import { message, Table, Button, Input, Modal, Descriptions, Tag, Form, InputNumber } from 'ant-design-vue';
import { ref, onMounted } from 'vue';
import { usePermission } from '#/composables/usePermission';

const hasPerm = usePermission().hasPermission;
const list = ref<WorkOrderRecord[]>([]);
const total = ref(0);
const loading = ref(false);
const current = ref(1);
const size = ref(10);
const status = ref('');

const detailVisible = ref(false);
const detailRecord = ref<WorkOrderRecord | null>(null);
const detailLines = ref<WorkOrderLineRecord[]>([]);
const detailOps = ref<WorkOrderOpRecord[]>([]);
const detailReports = ref<MesReportRecord[]>([]);
const detailLoading = ref(false);

const reportVisible = ref(false);
const reportWoId = ref<number | null>(null);
const reportForm = ref({ lineId: undefined as number | undefined, goodQty: 0, scrapQty: 0, opCode: 'REPORT' });
const reportSubmitting = ref(false);

const createFulfillmentId = ref<number | string>('');
const createSubmitting = ref(false);

async function load() {
  loading.value = true;
  try {
    const res = await pageWorkOrders({
      current: current.value,
      size: size.value,
      status: status.value || undefined,
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

async function openDetail(record: WorkOrderRecord) {
  if (!record.id) return;
  detailVisible.value = true;
  detailRecord.value = null;
  detailLines.value = [];
  detailOps.value = [];
  detailReports.value = [];
  detailLoading.value = true;
  try {
    detailRecord.value = (await getWorkOrder(record.id)) as WorkOrderRecord;
    detailLines.value = (await getWorkOrderLines(record.id)) as WorkOrderLineRecord[];
    detailOps.value = (await getWorkOrderOps(record.id)) as WorkOrderOpRecord[];
    detailReports.value = (await getWorkOrderReports(record.id)) as MesReportRecord[];
  } catch (e: any) {
    message.error(e?.message || '加载详情失败');
  } finally {
    detailLoading.value = false;
  }
}

async function doRelease(record: WorkOrderRecord) {
  if (!record.id) return;
  try {
    await releaseWorkOrder(record.id);
    message.success('已释放');
    load();
    if (detailRecord.value?.id === record.id) openDetail(record);
  } catch (e: any) {
    message.error(e?.message || '释放失败');
  }
}

async function doStart(record: WorkOrderRecord) {
  if (!record.id) return;
  try {
    await startWorkOrder(record.id);
    message.success('已开始');
    load();
    if (detailRecord.value?.id === record.id) openDetail(record);
  } catch (e: any) {
    message.error(e?.message || '开始失败');
  }
}

function openReport(record: WorkOrderRecord) {
  if (!record.id) return;
  reportWoId.value = record.id;
  reportForm.value = { lineId: undefined, goodQty: 0, scrapQty: 0, opCode: 'REPORT' };
  reportVisible.value = true;
  if (detailRecord.value?.id === record.id && detailLines.value.length) {
    reportForm.value.lineId = detailLines.value[0].id;
  }
}

async function submitReport() {
  if (!reportWoId.value || reportForm.value.lineId == null) {
    message.warning('请选择报工行');
    return;
  }
  reportSubmitting.value = true;
  try {
    await reportWorkOrder(reportWoId.value, {
      lineId: reportForm.value.lineId,
      goodQty: reportForm.value.goodQty,
      scrapQty: reportForm.value.scrapQty,
      opCode: reportForm.value.opCode,
    });
    message.success('报工成功');
    reportVisible.value = false;
    load();
    if (detailRecord.value?.id === reportWoId.value) openDetail(detailRecord.value);
  } catch (e: any) {
    message.error(e?.message || '报工失败');
  } finally {
    reportSubmitting.value = false;
  }
}

function doCancel(record: WorkOrderRecord) {
  if (!record.id) return;
  Modal.confirm({
    title: '确认取消工单',
    content: `确定取消工单 ${record.workOrderNo} 吗？`,
    onOk: async () => {
      try {
        await cancelWorkOrder(record.id!);
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
    await createWorkOrderFromFulfillment(id);
    message.success('工单已创建');
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
    RELEASED: 'cyan',
    IN_PROGRESS: 'processing',
    DONE: 'green',
    CANCELED: 'default',
  };
  return { color: map[s] || 'default', text: s || '-' };
}

onMounted(load);
</script>

<template>
  <Page title="工单">
    <div class="mb-4 flex gap-2 flex-wrap items-center">
      <Input v-model:value="status" placeholder="状态" allow-clear style="width: 120px" />
      <Button type="primary" @click="load">查询</Button>
      <span class="ml-4">按履约单创建：</span>
      <Input v-model:value="createFulfillmentId" placeholder="履约单ID" style="width: 120px" />
      <Button v-if="hasPerm('mes:work-order:create')" :loading="createSubmitting" @click="doCreateFromFulfillment">创建工单</Button>
    </div>
    <Table
      :data-source="list"
      :loading="loading"
      row-key="id"
      :pagination="{ current, pageSize: size, total, showSizeChanger: true, showTotal: (t: number) => `共 ${t} 条` }"
      @change="(p: any) => { current = p.current; size = p.pageSize; load(); }"
    >
      <Table.Column title="ID" data-index="id" width="80" />
      <Table.Column title="工单号" data-index="workOrderNo" width="180" />
      <Table.Column title="履约单ID" data-index="fulfillmentId" width="100" />
      <Table.Column title="来源单号" data-index="sourceNo" width="150" />
      <Table.Column title="状态" data-index="status" width="120">
        <template #default="{ record }">
          <Tag :color="statusTag(record.status).color">{{ statusTag(record.status).text }}</Tag>
        </template>
      </Table.Column>
      <Table.Column title="创建时间" data-index="createdAt" width="170" />
      <Table.Column title="操作" key="action" width="320" fixed="right">
        <template #default="{ record }">
          <Button v-if="hasPerm('mes:work-order:get')" type="link" size="small" @click="openDetail(record)">详情</Button>
          <Button v-if="hasPerm('mes:work-order:release') && record.status === 'CREATED'" type="link" size="small" @click="doRelease(record)">释放</Button>
          <Button v-if="hasPerm('mes:work-order:start') && (record.status === 'RELEASED' || record.status === 'SCHEDULED')" type="link" size="small" @click="doStart(record)">开始</Button>
          <Button v-if="hasPerm('mes:work-order:report') && ['RELEASED','IN_PROGRESS','RUNNING'].includes(record.status)" type="link" size="small" @click="openReport(record)">报工</Button>
          <Button v-if="hasPerm('mes:work-order:cancel') && !['DONE','CANCELED','FINISHED'].includes(record.status)" type="link" size="small" danger @click="doCancel(record)">取消</Button>
        </template>
      </Table.Column>
    </Table>

    <Modal v-model:open="detailVisible" title="工单详情" width="800" :footer="null" destroy-on-close>
      <div v-if="detailLoading">加载中...</div>
      <template v-else-if="detailRecord">
        <Descriptions :column="2" bordered size="small" class="mb-4">
          <Descriptions.Item label="工单号">{{ detailRecord.workOrderNo }}</Descriptions.Item>
          <Descriptions.Item label="状态"><Tag :color="statusTag(detailRecord.status).color">{{ detailRecord.status }}</Tag></Descriptions.Item>
          <Descriptions.Item label="履约单ID">{{ detailRecord.fulfillmentId }}</Descriptions.Item>
          <Descriptions.Item label="来源">{{ detailRecord.sourceType }} / {{ detailRecord.sourceNo }}</Descriptions.Item>
        </Descriptions>
        <div class="font-medium mb-2">工序</div>
        <Table :data-source="detailOps" row-key="id" size="small" :pagination="false" class="mb-4">
          <Table.Column title="序号" data-index="stepNo" width="70" />
          <Table.Column title="工序编码" data-index="opCode" width="100" />
          <Table.Column title="状态" data-index="status" width="100" />
          <Table.Column title="开始" data-index="startAt" width="160" />
          <Table.Column title="结束" data-index="endAt" width="160" />
        </Table>
        <div class="font-medium mb-2">工单行（产量）</div>
        <Table :data-source="detailLines" row-key="id" size="small" :pagination="false" class="mb-4">
          <Table.Column title="行号" data-index="lineNo" width="70" />
          <Table.Column title="SKU ID" data-index="skuId" width="90" />
          <Table.Column title="计划数量" data-index="qty" width="90" />
          <Table.Column title="已产" data-index="producedQty" width="80" />
          <Table.Column title="报废" data-index="scrapQty" width="80" />
        </Table>
        <div class="font-medium mb-2">报工记录</div>
        <Table :data-source="detailReports" row-key="id" size="small" :pagination="false">
          <Table.Column title="工序" data-index="opCode" width="80" />
          <Table.Column title="良品" data-index="goodQty" width="70" />
          <Table.Column title="报废" data-index="scrapQty" width="70" />
          <Table.Column title="时间" data-index="createdAt" width="170" />
        </Table>
        <div v-if="!detailRecord.status || !['DONE','CANCELED','FINISHED'].includes(detailRecord.status)" class="mt-4">
          <Button v-if="hasPerm('mes:work-order:report')" type="primary" @click="openReport(detailRecord)">报工</Button>
        </div>
      </template>
    </Modal>

    <Modal v-model:open="reportVisible" title="报工" width="400" :footer="null" destroy-on-close>
      <Form layout="vertical">
        <Form.Item label="报工行（工单行ID）">
          <InputNumber v-model:value="reportForm.lineId" placeholder="工单行ID" style="width: 100%" />
        </Form.Item>
        <Form.Item label="良品数量">
          <InputNumber v-model:value="reportForm.goodQty" min="0" style="width: 100%" />
        </Form.Item>
        <Form.Item label="报废数量">
          <InputNumber v-model:value="reportForm.scrapQty" min="0" style="width: 100%" />
        </Form.Item>
        <Form.Item label="工序编码">
          <Input v-model:value="reportForm.opCode" placeholder="如 REPORT" />
        </Form.Item>
        <Button type="primary" :loading="reportSubmitting" @click="submitReport">提交</Button>
      </Form>
    </Modal>
  </Page>
</template>
