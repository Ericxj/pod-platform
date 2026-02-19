<!--
  库存预占 - GET /api/inv/reservations (current, size, bizType, bizNo, skuId, status)
  权限：inv:reservation:page 查询 | inv:reserve 新增预占 | inv:release 释放 | inv:consume 核销
  预占 -> POST /api/inv/reservations | 释放 -> POST /api/inv/reservations/release | 核销 -> POST /api/inv/consumeReserved
-->
<script lang="ts" setup>
import { Page } from '@vben/common-ui';
import { pageReservations } from '#/api/inv/query';
import { release } from '#/api/inv/commands';
import type { ReservationRecord } from '#/api/inv/query';
import { message, Table, Button, Input, InputNumber, Modal } from 'ant-design-vue';
import { ref, onMounted } from 'vue';
import { usePermission } from '#/composables/usePermission';
import InvReserveDrawer from '../components/inv-reserve-drawer.vue';
import InvConsumeDrawer from '../components/inv-consume-drawer.vue';
import { useVbenDrawer } from '@vben/common-ui';

const { hasPermission: hasPerm } = usePermission();

const list = ref<ReservationRecord[]>([]);
const total = ref(0);
const loading = ref(false);
const current = ref(1);
const size = ref(10);
const bizType = ref('');
const bizNo = ref('');
const skuId = ref<number | undefined>();
const status = ref('');

async function load() {
  loading.value = true;
  try {
    const res = await pageReservations({
      current: current.value,
      size: size.value,
      bizType: bizType.value || undefined,
      bizNo: bizNo.value || undefined,
      skuId: skuId.value,
      status: status.value || undefined,
    });
    list.value = (res as any)?.records ?? [];
    total.value = (res as any)?.total ?? 0;
  } catch (e: any) {
    message.error(e?.message || e?.msg || '加载失败');
  } finally {
    loading.value = false;
  }
}

function remaining(r: ReservationRecord) {
  return (r.qty ?? 0) - (r.consumedQty ?? 0);
}

function handleRelease(record: ReservationRecord) {
  Modal.confirm({
    title: '确认释放',
    content: `确定释放预占「${record.bizNo}」SKU ${record.skuId} 剩余数量 ${remaining(record)} 吗？`,
    onOk: async () => {
      try {
        await release({ reservationId: record.id });
        message.success('释放成功');
        load();
      } catch (e: any) {
        message.error(e?.message || e?.msg || '释放失败');
      }
    },
  });
}

const [ReserveDrawer, reserveDrawerApi] = useVbenDrawer({ connectedComponent: InvReserveDrawer });
const [ConsumeDrawer, consumeDrawerApi] = useVbenDrawer({ connectedComponent: InvConsumeDrawer });

function openReserve() {
  reserveDrawerApi.setData({});
  reserveDrawerApi.open();
}

function openConsume(record: ReservationRecord) {
  consumeDrawerApi.setData({
    reservationId: record.id,
    remainingQty: remaining(record),
  });
  consumeDrawerApi.open();
}

function onDrawerSuccess() {
  load();
}

onMounted(load);
</script>

<template>
  <Page title="库存预占">
    <div class="mb-4 flex gap-2">
      <Input v-model:value="bizType" placeholder="业务类型" allow-clear style="width: 120px" />
      <Input v-model:value="bizNo" placeholder="业务单号" allow-clear style="width: 140px" />
      <InputNumber v-model:value="skuId" placeholder="SKU ID" allow-clear style="width: 100px" />
      <Input v-model:value="status" placeholder="状态" allow-clear style="width: 100px" />
      <Button type="primary" @click="load">查询</Button>
      <Button v-if="hasPerm('inv:reserve')" type="primary" @click="openReserve">新增预占</Button>
    </div>
    <Table
      :data-source="list"
      :loading="loading"
      row-key="id"
      :pagination="{
        current,
        pageSize: size,
        total,
        showSizeChanger: true,
        showTotal: (t: number) => `共 ${t} 条`,
      }"
      @change="(p: any) => { current = p.current; size = p.pageSize; load(); }"
    >
      <Table.Column title="业务类型" data-index="bizType" width="100" />
      <Table.Column title="业务单号" data-index="bizNo" width="140" />
      <Table.Column title="行号" width="70">
        <template #default="{ record }">{{ record.id }}</template>
      </Table.Column>
      <Table.Column title="仓库ID" data-index="warehouseId" width="90" />
      <Table.Column title="SKU ID" data-index="skuId" width="90" />
      <Table.Column title="预占数量" data-index="qty" width="90" />
      <Table.Column title="已核销" data-index="consumedQty" width="80" />
      <Table.Column title="状态" data-index="status" width="90" />
      <Table.Column title="更新时间" data-index="updatedAt" width="170" />
      <Table.Column title="操作" key="action" width="160" fixed="right">
        <template #default="{ record }">
          <template v-if="record.status === 'RESERVED' && remaining(record) > 0">
            <Button v-if="hasPerm('inv:consume')" type="link" size="small" @click="openConsume(record)">核销</Button>
            <Button v-if="hasPerm('inv:release')" type="link" size="small" danger @click="handleRelease(record)">释放</Button>
          </template>
        </template>
      </Table.Column>
    </Table>
    <ReserveDrawer @success="onDrawerSuccess" />
    <ConsumeDrawer @success="onDrawerSuccess" />
  </Page>
</template>
