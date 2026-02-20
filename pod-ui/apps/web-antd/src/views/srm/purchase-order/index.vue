<script lang="ts" setup>
import { ref, onMounted, computed } from 'vue';
import { Page } from '#/components/Page';
import {
  getPurchaseOrderPage,
  getPurchaseOrder,
  createPurchaseOrder,
  addPurchaseOrderLine,
  updatePurchaseOrderLine,
  submitPurchaseOrder,
  approvePurchaseOrder,
  cancelPurchaseOrder,
  closePurchaseOrder,
} from '#/api/srm';
import { getSupplierList } from '#/api/srm';
import { pageSku } from '#/api/prd';
import type { PurchaseOrderRecord, PurchaseOrderLineRecord, SupplierRecord, PageResult } from '#/api/srm';
import type { SkuRecord } from '#/api/prd';
import { usePermission } from '#/composables/usePermission';
import { message, Table, Button, Space, Modal, Form, Input, Select, DatePicker } from 'ant-design-vue';
import dayjs from 'dayjs';

const { hasPermission: hasPerm } = usePermission();

const loading = ref(false);
const dataSource = ref<PurchaseOrderRecord[]>([]);
const total = ref(0);
const page = ref(1);
const pageSize = ref(10);
const status = ref<string | undefined>();
const supplierId = ref<number | undefined>();

const createModalVisible = ref(false);
const createLoading = ref(false);
const createForm = ref<{ supplierId?: number; currency: string; expectedArriveDate?: string }>({ currency: 'CNY' });
const createFormRef = ref();
const supplierOptions = ref<SupplierRecord[]>([]);

const detailVisible = ref(false);
const detailPo = ref<PurchaseOrderRecord | null>(null);
const detailLoading = ref(false);

const addLineModalVisible = ref(false);
const addLinePoId = ref<number | null>(null);
const skuKeyword = ref('');
const skuOptions = ref<Array<{ id: number; skuCode: string; skuName?: string }>>([]);
const selectedSkuId = ref<number | undefined>();
const addLineForm = ref<{ qtyOrdered: number; unitPrice: number }>({ qtyOrdered: 1, unitPrice: 0 });
const addLineLoading = ref(false);
const addLineFormRef = ref();

const editLineModalVisible = ref(false);
const editLinePoId = ref<number | null>(null);
const editLineRecord = ref<PurchaseOrderLineRecord | null>(null);
const editLineForm = ref<{ qtyOrdered: number; unitPrice: number }>({ qtyOrdered: 1, unitPrice: 0 });
const editLineLoading = ref(false);
const editLineFormRef = ref();

const actionLoading = ref(false);

const statusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '已提交', value: 'SUBMITTED' },
  { label: '已审批', value: 'APPROVED' },
  { label: '已关闭', value: 'CLOSED' },
  { label: '已取消', value: 'CANCELED' },
];

const supplierMap = computed(() => {
  const m: Record<number, string> = {};
  supplierOptions.value.forEach((s) => {
    if (s.id != null) m[s.id] = s.supplierName || s.supplierCode || String(s.id);
  });
  return m;
});

async function loadSuppliers() {
  try {
    const list = await getSupplierList('ENABLED');
    supplierOptions.value = (list as SupplierRecord[]) ?? [];
  } catch {
    supplierOptions.value = [];
  }
}

async function load() {
  loading.value = true;
  try {
    const res = await getPurchaseOrderPage({
      current: page.value,
      size: pageSize.value,
      supplierId: supplierId.value,
      status: status.value,
    });
    const d = (res as unknown as PageResult<PurchaseOrderRecord>) ?? {};
    dataSource.value = d.records ?? [];
    total.value = d.total ?? 0;
  } catch (e) {
    message.error('加载失败');
  } finally {
    loading.value = false;
  }
}

function openCreate() {
  createForm.value = { supplierId: undefined, currency: 'CNY', expectedArriveDate: undefined };
  createModalVisible.value = true;
}

async function handleCreate() {
  try {
    await createFormRef.value?.validate();
  } catch {
    return;
  }
  if (!createForm.value.supplierId) {
    message.warning('请选择供应商');
    return;
  }
  createLoading.value = true;
  try {
    const exp = createForm.value.expectedArriveDate;
    const expStr =
      exp == null
        ? undefined
        : typeof exp === 'string'
          ? exp
          : dayjs(exp).format('YYYY-MM-DD');
    await createPurchaseOrder({
      supplierId: createForm.value.supplierId,
      currency: createForm.value.currency || 'CNY',
      expectedArriveDate: expStr,
    });
    message.success('创建成功');
    createModalVisible.value = false;
    load();
  } catch (e: unknown) {
    const err = e as { message?: string };
    message.error(err?.message || '创建失败');
  } finally {
    createLoading.value = false;
  }
}

async function openDetail(record: PurchaseOrderRecord) {
  if (!record.id) return;
  detailPo.value = null;
  detailVisible.value = true;
  detailLoading.value = true;
  try {
    const po = await getPurchaseOrder(record.id);
    detailPo.value = po as PurchaseOrderRecord;
  } catch (e) {
    message.error('加载详情失败');
  } finally {
    detailLoading.value = false;
  }
}

function openAddLine(poId: number) {
  addLinePoId.value = poId;
  selectedSkuId.value = undefined;
  skuKeyword.value = '';
  skuOptions.value = [];
  addLineForm.value = { qtyOrdered: 1, unitPrice: 0 };
  addLineModalVisible.value = true;
}

async function searchSku() {
  if (!skuKeyword.value.trim()) {
    skuOptions.value = [];
    return;
  }
  try {
    const res = await pageSku({ current: 1, size: 30, keyword: skuKeyword.value, status: 'ACTIVE' });
    const d = res as { records?: SkuRecord[] };
    skuOptions.value = (d?.records ?? []).map((r) => ({ id: r.id!, skuCode: r.skuCode || '', skuName: r.skuName }));
  } catch {
    skuOptions.value = [];
  }
}

async function handleAddLine() {
  try {
    await addLineFormRef.value?.validate();
  } catch {
    return;
  }
  if (addLinePoId.value == null || selectedSkuId.value == null) {
    message.warning('请选择 SKU');
    return;
  }
  addLineLoading.value = true;
  try {
    await addPurchaseOrderLine(addLinePoId.value, {
      skuId: selectedSkuId.value,
      qtyOrdered: addLineForm.value.qtyOrdered,
      unitPrice: addLineForm.value.unitPrice,
    });
    message.success('添加行成功');
    addLineModalVisible.value = false;
    if (detailPo.value?.id === addLinePoId.value) {
      const po = await getPurchaseOrder(addLinePoId.value);
      detailPo.value = po as PurchaseOrderRecord;
    }
    load();
  } catch (e: unknown) {
    const err = e as { message?: string };
    message.error(err?.message || '添加失败');
  } finally {
    addLineLoading.value = false;
  }
}

function openEditLine(record: PurchaseOrderLineRecord, poId: number) {
  editLinePoId.value = poId;
  editLineRecord.value = record;
  editLineForm.value = {
    qtyOrdered: record.qtyOrdered ?? 1,
    unitPrice: record.unitPrice ?? 0,
  };
  editLineModalVisible.value = true;
}

async function handleUpdateLine() {
  try {
    await editLineFormRef.value?.validate();
  } catch {
    return;
  }
  if (editLinePoId.value == null || !editLineRecord.value?.id) return;
  editLineLoading.value = true;
  try {
    await updatePurchaseOrderLine(editLinePoId.value, editLineRecord.value.id, {
      qtyOrdered: editLineForm.value.qtyOrdered,
      unitPrice: editLineForm.value.unitPrice,
    });
    message.success('更新成功');
    editLineModalVisible.value = false;
    if (detailPo.value?.id === editLinePoId.value) {
      const po = await getPurchaseOrder(editLinePoId.value);
      detailPo.value = po as PurchaseOrderRecord;
    }
    load();
  } catch (e: unknown) {
    const err = e as { message?: string };
    message.error(err?.message || '更新失败');
  } finally {
    editLineLoading.value = false;
  }
}

async function doSubmit() {
  if (!detailPo.value?.id) return;
  actionLoading.value = true;
  try {
    await submitPurchaseOrder(detailPo.value.id);
    message.success('提交成功');
    const po = await getPurchaseOrder(detailPo.value.id);
    detailPo.value = po as PurchaseOrderRecord;
    load();
  } catch (e: unknown) {
    const err = e as { message?: string };
    message.error(err?.message || '提交失败');
  } finally {
    actionLoading.value = false;
  }
}

async function doApprove() {
  if (!detailPo.value?.id) return;
  actionLoading.value = true;
  try {
    await approvePurchaseOrder(detailPo.value.id);
    message.success('审批成功');
    const po = await getPurchaseOrder(detailPo.value.id);
    detailPo.value = po as PurchaseOrderRecord;
    load();
  } catch (e: unknown) {
    const err = e as { message?: string };
    message.error(err?.message || '审批失败');
  } finally {
    actionLoading.value = false;
  }
}

async function doCancel() {
  if (!detailPo.value?.id) return;
  actionLoading.value = true;
  try {
    await cancelPurchaseOrder(detailPo.value.id);
    message.success('已取消');
    const po = await getPurchaseOrder(detailPo.value.id);
    detailPo.value = po as PurchaseOrderRecord;
    load();
  } catch (e: unknown) {
    const err = e as { message?: string };
    message.error(err?.message || '取消失败');
  } finally {
    actionLoading.value = false;
  }
}

async function doClose() {
  if (!detailPo.value?.id) return;
  actionLoading.value = true;
  try {
    await closePurchaseOrder(detailPo.value.id);
    message.success('已关闭');
    const po = await getPurchaseOrder(detailPo.value.id);
    detailPo.value = po as PurchaseOrderRecord;
    load();
  } catch (e: unknown) {
    const err = e as { message?: string };
    message.error(err?.message || '关闭失败');
  } finally {
    actionLoading.value = false;
  }
}

onMounted(() => {
  loadSuppliers();
  load();
});
</script>

<template>
  <Page title="采购单管理">
    <div class="mb-4 flex gap-2">
      <Select
        v-model:value="supplierId"
        placeholder="供应商"
        style="width: 180px"
        allow-clear
        :options="supplierOptions.map((s) => ({ label: s.supplierName || s.supplierCode, value: s.id }))"
      />
      <Select v-model:value="status" placeholder="状态" style="width: 120px" allow-clear :options="statusOptions" />
      <Button type="primary" @click="load">查询</Button>
      <Button v-if="hasPerm('srm:po:create')" type="primary" @click="openCreate">新建采购单</Button>
    </div>
    <Table
      :columns="[
        { title: 'ID', dataIndex: 'id', width: 80 },
        { title: '采购单号', dataIndex: 'poNo', width: 140 },
        { title: '供应商', key: 'supplier', width: 140 },
        { title: '币种', dataIndex: 'currency', width: 80 },
        { title: '状态', dataIndex: 'status', width: 90 },
        { title: '总数量', dataIndex: 'totalQty', width: 90 },
        { title: '总金额', dataIndex: 'totalAmount', width: 100 },
        { title: '预计到货', dataIndex: 'expectedArriveDate', width: 120 },
        { title: '操作', key: 'action', width: 120, fixed: 'right' },
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
        <template v-if="column.key === 'supplier'">
          {{ supplierMap[record.supplierId] ?? record.supplierId }}
        </template>
        <template v-if="column.key === 'action'">
          <Button type="link" size="small" @click="openDetail(record)">查看</Button>
        </template>
      </template>
    </Table>

    <!-- 新建采购单 -->
    <Modal v-model:open="createModalVisible" title="新建采购单" @ok="handleCreate">
      <Form ref="createFormRef" :model="createForm" layout="vertical">
        <Form.Item name="supplierId" label="供应商" :rules="[{ required: true }]">
          <Select
            v-model:value="createForm.supplierId"
            placeholder="请选择"
            :options="supplierOptions.map((s) => ({ label: `${s.supplierCode} - ${s.supplierName}`, value: s.id }))"
          />
        </Form.Item>
        <Form.Item name="currency" label="币种">
          <Input v-model:value="createForm.currency" />
        </Form.Item>
        <Form.Item name="expectedArriveDate" label="预计到货日期">
          <DatePicker v-model:value="createForm.expectedArriveDate" value-format="YYYY-MM-DD" style="width: 100%" />
        </Form.Item>
      </Form>
    </Modal>

    <!-- 详情（含行与操作） -->
    <Modal
      v-model:open="detailVisible"
      title="采购单详情"
      width="800px"
      :footer="null"
      destroy-on-close
    >
      <div v-if="detailLoading" class="py-8 text-center">加载中...</div>
      <template v-else-if="detailPo">
        <div class="mb-4 flex flex-wrap gap-2">
          <span><strong>单号：</strong>{{ detailPo.poNo }}</span>
          <span><strong>供应商：</strong>{{ supplierMap[detailPo.supplierId!] ?? detailPo.supplierId }}</span>
          <span><strong>状态：</strong>{{ detailPo.status }}</span>
          <span><strong>总数量：</strong>{{ detailPo.totalQty }}</span>
          <span><strong>总金额：</strong>{{ detailPo.totalAmount }}</span>
          <span><strong>预计到货：</strong>{{ detailPo.expectedArriveDate || '-' }}</span>
        </div>
        <div class="mb-2 flex gap-2">
          <Button
            v-if="detailPo.status === 'DRAFT' && hasPerm('srm:po:addLine')"
            type="primary"
            size="small"
            @click="openAddLine(detailPo.id!)"
          >
            添加行
          </Button>
          <Button
            v-if="detailPo.status === 'DRAFT' && hasPerm('srm:po:submit')"
            type="primary"
            size="small"
            :loading="actionLoading"
            @click="doSubmit"
          >
            提交
          </Button>
          <Button
            v-if="detailPo.status === 'SUBMITTED' && hasPerm('srm:po:approve')"
            type="primary"
            size="small"
            :loading="actionLoading"
            @click="doApprove"
          >
            审批
          </Button>
          <Button
            v-if="(detailPo.status === 'DRAFT' || detailPo.status === 'SUBMITTED') && hasPerm('srm:po:cancel')"
            size="small"
            :loading="actionLoading"
            @click="doCancel"
          >
            取消
          </Button>
          <Button
            v-if="detailPo.status === 'APPROVED' && hasPerm('srm:po:close')"
            size="small"
            :loading="actionLoading"
            @click="doClose"
          >
            关闭
          </Button>
        </div>
        <Table
          :columns="[
            { title: '行号', dataIndex: 'lineNo', width: 70 },
            { title: 'SKU编码', dataIndex: 'skuCode', width: 120 },
            { title: 'SKU名称', dataIndex: 'skuName' },
            { title: '数量', dataIndex: 'qtyOrdered', width: 80 },
            { title: '单价', dataIndex: 'unitPrice', width: 90 },
            { title: '已收', dataIndex: 'qtyReceived', width: 70 },
            { title: '操作', key: 'lineAction', width: 80 },
          ]"
          :data-source="detailPo.lines ?? []"
          row-key="id"
          :pagination="false"
          size="small"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'lineAction'">
              <Button
                v-if="detailPo!.status === 'DRAFT' && hasPerm('srm:po:updateLine')"
                type="link"
                size="small"
                @click="openEditLine(record, detailPo!.id!)"
              >
                编辑
              </Button>
            </template>
          </template>
        </Table>
      </template>
    </Modal>

    <!-- 添加行 -->
    <Modal v-model:open="addLineModalVisible" title="添加行" @ok="handleAddLine">
      <Form ref="addLineFormRef" :model="addLineForm" layout="vertical">
        <Form.Item label="SKU">
          <div class="flex gap-2">
            <Input v-model:value="skuKeyword" placeholder="输入编码/名称搜索" @change="searchSku" />
            <Button @click="searchSku">搜索</Button>
          </div>
          <Select
            v-model:value="selectedSkuId"
            placeholder="选择 SKU"
            style="width: 100%; margin-top: 8px"
            :options="skuOptions.map((s) => ({ label: `${s.skuCode} ${s.skuName || ''}`, value: s.id }))"
            show-search
            :filter-option="false"
          />
        </Form.Item>
        <Form.Item name="qtyOrdered" label="数量" :rules="[{ required: true, type: 'number', min: 1 }]">
          <Input v-model:value="addLineForm.qtyOrdered" type="number" />
        </Form.Item>
        <Form.Item name="unitPrice" label="单价" :rules="[{ required: true }]">
          <Input v-model:value="addLineForm.unitPrice" type="number" />
        </Form.Item>
      </Form>
    </Modal>

    <!-- 编辑行 -->
    <Modal v-model:open="editLineModalVisible" title="编辑行" @ok="handleUpdateLine">
      <Form ref="editLineFormRef" :model="editLineForm" layout="vertical">
        <Form.Item name="qtyOrdered" label="数量" :rules="[{ required: true, type: 'number', min: 1 }]">
          <Input v-model:value="editLineForm.qtyOrdered" type="number" />
        </Form.Item>
        <Form.Item name="unitPrice" label="单价" :rules="[{ required: true }]">
          <Input v-model:value="editLineForm.unitPrice" type="number" />
        </Form.Item>
      </Form>
    </Modal>
  </Page>
</template>
