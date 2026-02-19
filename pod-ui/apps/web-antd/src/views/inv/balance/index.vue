<!--
  库存余额 - GET /api/inv/balances (current, size, warehouseId, skuId/keyword)
  权限：inv:balance:page 查询 | inv:adjust 调整
  调整 -> POST /api/inv/adjust
-->
<script lang="ts" setup>
import { Page } from '@vben/common-ui';
import { pageBalances } from '#/api/inv/query';
import type { BalanceRecord } from '#/api/inv/query';
import { message, Table, Button, Input, InputNumber } from 'ant-design-vue';
import { ref, onMounted } from 'vue';
import { usePermission } from '#/composables/usePermission';
import InvAdjustDrawer from '../components/inv-adjust-drawer.vue';
import { useVbenDrawer } from '@vben/common-ui';

const { hasPermission: hasPerm } = usePermission();

const list = ref<BalanceRecord[]>([]);
const total = ref(0);
const loading = ref(false);
const current = ref(1);
const size = ref(10);
const warehouseId = ref<number | undefined>();
const keyword = ref('');

async function load() {
  loading.value = true;
  try {
    const res = await pageBalances({
      current: current.value,
      size: size.value,
      warehouseId: warehouseId.value,
      skuId: keyword.value ? Number(keyword.value) : undefined,
      keyword: keyword.value || undefined,
    });
    list.value = (res as any)?.records ?? [];
    total.value = (res as any)?.total ?? 0;
  } catch (e: any) {
    message.error(e?.message || e?.msg || '加载失败');
  } finally {
    loading.value = false;
  }
}

const [Drawer, drawerApi] = useVbenDrawer({ connectedComponent: InvAdjustDrawer });

function openAdjust(record: BalanceRecord) {
  drawerApi.setData({ balanceId: record.id, skuId: record.skuId, warehouseId: record.warehouseId });
  drawerApi.open();
}

function onDrawerSuccess() {
  load();
}

onMounted(load);
</script>

<template>
  <Page title="库存余额">
    <div class="mb-4 flex gap-2">
      <InputNumber v-model:value="warehouseId" placeholder="仓库ID" allow-clear style="width: 120px" />
      <Input v-model:value="keyword" placeholder="SKU ID 或关键词" allow-clear style="width: 160px" />
      <Button type="primary" @click="load">查询</Button>
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
      <Table.Column title="仓库ID" data-index="warehouseId" width="90" />
      <Table.Column title="SKU ID" data-index="skuId" width="90" />
      <Table.Column title="现存(onHand)" data-index="onHandQty" width="100" />
      <Table.Column title="预占(reserved)" data-index="allocatedQty" width="100" />
      <Table.Column title="可用(available)" data-index="availableQty" width="100" />
      <Table.Column title="更新时间" data-index="updatedAt" width="170" />
      <Table.Column title="操作" key="action" width="100" fixed="right">
        <template #default="{ record }">
          <Button v-if="hasPerm('inv:adjust')" type="link" size="small" @click="openAdjust(record)">调整库存</Button>
        </template>
      </Table.Column>
    </Table>
    <Drawer @success="onDrawerSuccess" />
  </Page>
</template>
